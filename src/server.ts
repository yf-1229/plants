import dgram from 'node:dgram';
import path from 'node:path';

import express from 'express';
import { WebSocketServer, WebSocket } from 'ws';

const app = express();
const port = Number(process.env.PORT ?? 3000);

const telloHost = process.env.TELLO_HOST ?? '192.168.10.1';
const telloPort = Number(process.env.TELLO_PORT ?? 8889);
const localUdpPort = Number(process.env.LOCAL_UDP_PORT ?? 9000);
const defaultCommandIntervalMs = Number(process.env.DEFAULT_COMMAND_INTERVAL_MS ?? 1000);

const udpSocket = dgram.createSocket('udp4');
let udpQueue: Promise<void> = Promise.resolve();
let udpSocketRunning = false;
let udpSocketInitError: Error | null = null;
let resolveUdpReady: (() => void) | null = null;

const udpReadyPromise = new Promise<void>((resolve) => {
  resolveUdpReady = resolve;
});

udpSocket.once('listening', () => {
  udpSocketRunning = true;
  udpSocketInitError = null;
  resolveUdpReady?.();
  resolveUdpReady = null;
});

udpSocket.on('error', (error) => {
  udpSocketInitError = error;
  resolveUdpReady?.();
  resolveUdpReady = null;
});

udpSocket.on('close', () => {
  udpSocketRunning = false;
  udpSocketInitError = new Error('UDP socket has been closed');
});

udpSocket.bind(localUdpPort);

function getUdpUnavailableMessage(): string {
  if (udpSocketInitError) {
    return `UDP socket is closed or failed to initialize. Cannot send command: ${udpSocketInitError.message}`;
  }
  return 'UDP socket is closed or not initialized. Cannot send command.';
}

function sendUdpCommand(command: string, timeoutMs = 5000): Promise<string> {
  const result = udpQueue.then(async () => {
    await udpReadyPromise;
    if (!udpSocketRunning) {
      throw new Error(getUdpUnavailableMessage());
    }
    return sendUdpCommandInternal(command, timeoutMs);
  });
  udpQueue = result.then(() => undefined, () => undefined);
  return result;
}

const allowedPattern = /^(command|takeoff|land|stop|emergency|speed\?|battery\?|time\?|wifi\?|cw\s\d+|ccw\s\d+|forward\s\d+|back\s\d+|left\s\d+|right\s\d+|up\s\d+|down\s\d+|flip\s[lrfb]|wait\s\d+)$/;

function isCommandAllowed(command: string): boolean {
  return allowedPattern.test(command.trim());
}

function sendUdpCommandInternal(command: string, timeoutMs = 5000): Promise<string> {
  return new Promise((resolve, reject) => {
    const message = Buffer.from(command, 'utf-8');

    const onMessage = (response: Buffer) => {
      cleanup();
      resolve(response.toString('utf-8').trim());
    };

    const onTimeout = () => {
      cleanup();
      reject(new Error(`Timeout waiting response for: ${command}`));
    };

    const cleanup = () => {
      clearTimeout(timer);
      udpSocket.off('message', onMessage);
    };

    const timer = setTimeout(onTimeout, timeoutMs);
    udpSocket.on('message', onMessage);

    udpSocket.send(message, telloPort, telloHost, (err) => {
      if (err) {
        cleanup();
        reject(err);
      }
    });
  });
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function parseWaitSeconds(command: string): number | null {
  const match = /^wait\s+(\d+)$/.exec(command.trim());
  if (!match) {
    return null;
  }
  const seconds = Number(match[1]);
  return Number.isFinite(seconds) && seconds > 0 ? seconds : null;
}

app.use('/vendor/blockly', express.static(path.join(__dirname, '..', 'node_modules', 'blockly')));
app.use(express.static(path.join(__dirname, '..', 'public')));

app.post('/api/emergency', async (_req, res) => {
  try {
    const response = await sendUdpCommand('emergency');
    res.json({ ok: true, response });
  } catch (error) {
    res.status(500).json({
      ok: false,
      message: error instanceof Error ? error.message : 'Emergency stop failed'
    });
  }
});

const server = app.listen(port, '0.0.0.0', () => {
  console.log(`Server started: http://0.0.0.0:${port}`);
  console.log(`Tello target: ${telloHost}:${telloPort}`);
});

const wss = new WebSocketServer({ server, path: '/ws' });
const telemetryState: Record<string, string> = {
  speed: '--',
  battery: '--',
  time: '--',
  wifi: '--'
};
let activeExecutions = 0;

function broadcastTelemetry() {
  const message = JSON.stringify({ type: 'telemetry', telemetry: telemetryState });
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(message);
    }
  });
}

async function updateTelemetry() {
  if (activeExecutions > 0 || wss.clients.size === 0) {
    return;
  }
  const telemetryCommands: Array<{ key: string; command: string }> = [
    { key: 'speed', command: 'speed?' },
    { key: 'battery', command: 'battery?' },
    { key: 'time', command: 'time?' },
    { key: 'wifi', command: 'wifi?' }
  ];
  for (const { key, command } of telemetryCommands) {
    try {
      const response = await sendUdpCommand(command);
      telemetryState[key] = response;
    } catch {
      telemetryState[key] = '--';
    }
  }
  broadcastTelemetry();
}

setInterval(() => {
  void updateTelemetry();
}, 3000);

wss.on('connection', (ws: WebSocket) => {
  ws.send(JSON.stringify({ type: 'status', message: 'connected' }));
  ws.send(JSON.stringify({ type: 'telemetry', telemetry: telemetryState }));

  ws.on('message', async (raw) => {
    try {
      const payload = JSON.parse(raw.toString()) as { type?: string; commands?: string[] };
      if (payload.type !== 'execute' || !Array.isArray(payload.commands)) {
        ws.send(JSON.stringify({ type: 'error', message: 'Invalid payload' }));
        return;
      }

      const commands = payload.commands.map((c) => c.trim()).filter((c) => c.length > 0);
      for (const command of commands) {
        if (!isCommandAllowed(command)) {
          ws.send(JSON.stringify({ type: 'error', message: `Rejected command: ${command}` }));
          return;
        }
      }

      const executionList = commands[0] === 'command' ? commands : ['command', ...commands];
      const results: Array<{ command: string; response: string }> = [];
      activeExecutions += 1;

      try {
        for (let i = 0; i < executionList.length; i += 1) {
          const command = executionList[i];
          const waitSeconds = parseWaitSeconds(command);
          if (waitSeconds !== null) {
            await sleep(waitSeconds * 1000);
            results.push({ command, response: `ok (wait ${waitSeconds}s)` });
            continue;
          }

          try {
            const response = await sendUdpCommand(command);
            results.push({ command, response });
          } catch (error) {
            const message = error instanceof Error ? error.message : 'Command execution failed';
            results.push({ command, response: `error: ${message}` });
            if (command === 'emergency') {
              throw error;
            }
          }

          if (i < executionList.length - 1) {
            await sleep(defaultCommandIntervalMs);
          }
        }
      } finally {
        activeExecutions = Math.max(0, activeExecutions - 1);
      }

      ws.send(JSON.stringify({ type: 'result', results }));
    } catch (error) {
      ws.send(JSON.stringify({
        type: 'error',
        message: error instanceof Error ? error.message : 'Execution failed'
      }));
    }
  });
});

process.on('SIGINT', () => {
  udpSocket.close();
  server.close(() => process.exit(0));
});
