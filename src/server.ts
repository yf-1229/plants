import dgram from 'node:dgram';
import path from 'node:path';

import express from 'express';
import { WebSocketServer, WebSocket } from 'ws';

const app = express();
const port = Number(process.env.PORT ?? 3000);

const telloHost = process.env.TELLO_HOST ?? '192.168.10.1';
const telloPort = Number(process.env.TELLO_PORT ?? 8889);
const localUdpPort = Number(process.env.LOCAL_UDP_PORT ?? 9000);

const udpSocket = dgram.createSocket('udp4');
udpSocket.bind(localUdpPort);

const allowedPattern = /^(command|takeoff|land|stop|emergency|cw\s\d+|ccw\s\d+|forward\s\d+|back\s\d+|left\s\d+|right\s\d+|up\s\d+|down\s\d+|flip\s[lrfb])$/;

function isCommandAllowed(command: string): boolean {
  return allowedPattern.test(command.trim());
}

function sendUdpCommand(command: string, timeoutMs = 5000): Promise<string> {
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

app.use(express.static(path.join(__dirname, '..', 'public')));

const server = app.listen(port, '0.0.0.0', () => {
  console.log(`Server started: http://0.0.0.0:${port}`);
  console.log(`Tello target: ${telloHost}:${telloPort}`);
});

const wss = new WebSocketServer({ server, path: '/ws' });

wss.on('connection', (ws: WebSocket) => {
  ws.send(JSON.stringify({ type: 'status', message: 'connected' }));

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

      for (const command of executionList) {
        const response = await sendUdpCommand(command);
        results.push({ command, response });
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
