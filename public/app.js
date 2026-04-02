const blocklyDiv = document.getElementById('blocklyDiv');
const commandOutput = document.getElementById('commandOutput');
const logOutput = document.getElementById('logOutput');
const convertBtn = document.getElementById('convertBtn');
const executeBtn = document.getElementById('executeBtn');
const emergencyBtn = document.getElementById('emergencyBtn');

Blockly.defineBlocksWithJsonArray([
  {
    type: 'tello_takeoff',
    message0: '離陸 takeoff',
    previousStatement: null,
    nextStatement: null,
    colour: 200
  },
  {
    type: 'tello_land',
    message0: '着陸 land',
    previousStatement: null,
    nextStatement: null,
    colour: 200
  },
  {
    type: 'tello_rotate',
    message0: '回転 %1 角度 %2 度',
    args0: [
      {
        type: 'field_dropdown',
        name: 'DIRECTION',
        options: [
          ['右(cw)', 'cw'],
          ['左(ccw)', 'ccw']
        ]
      },
      {
        type: 'field_number',
        name: 'DEGREE',
        value: 90,
        min: 1,
        max: 360
      }
    ],
    previousStatement: null,
    nextStatement: null,
    colour: 220
  },
  {
    type: 'tello_move',
    message0: '移動 %1 距離 %2 cm',
    args0: [
      {
        type: 'field_dropdown',
        name: 'DIRECTION',
        options: [
          ['前', 'forward'],
          ['後', 'back'],
          ['左', 'left'],
          ['右', 'right'],
          ['上', 'up'],
          ['下', 'down']
        ]
      },
      {
        type: 'field_number',
        name: 'DISTANCE',
        value: 50,
        min: 20,
        max: 500
      }
    ],
    previousStatement: null,
    nextStatement: null,
    colour: 240
  },
  {
    type: 'tello_wait',
    message0: '待機 %1 秒',
    args0: [
      {
        type: 'field_number',
        name: 'SECONDS',
        value: 3,
        min: 1,
        max: 30
      }
    ],
    previousStatement: null,
    nextStatement: null,
    colour: 260
  }
]);

const workspace = Blockly.inject(blocklyDiv, {
  toolbox: document.getElementById('toolbox'),
  media: '/vendor/blockly/media/',
  trashcan: true,
  move: {
    scrollbars: true,
    drag: true,
    wheel: true
  },
  zoom: {
    controls: true,
    wheel: true,
    startScale: 0.8,
    maxScale: 1.8,
    minScale: 0.3,
    scaleSpeed: 1.1
  }
});

function collectCommands() {
  const topBlocks = workspace
    .getTopBlocks(true)
    .slice()
    .sort((a, b) => a.getRelativeToSurfaceXY().y - b.getRelativeToSurfaceXY().y);

  const commands = [];
  for (const top of topBlocks) {
    let current = top;
    while (current) {
      if (current.type === 'tello_takeoff') {
        commands.push('takeoff');
      } else if (current.type === 'tello_land') {
        commands.push('land');
      } else if (current.type === 'tello_rotate') {
        const direction = current.getFieldValue('DIRECTION');
        const degree = Number(current.getFieldValue('DEGREE'));
        commands.push(`${direction} ${degree}`);
      } else if (current.type === 'tello_move') {
        const direction = current.getFieldValue('DIRECTION');
        const distance = Number(current.getFieldValue('DISTANCE'));
        commands.push(`${direction} ${distance}`);
      } else if (current.type === 'tello_wait') {
        const seconds = Number(current.getFieldValue('SECONDS'));
        commands.push(`wait ${seconds}`);
      }
      current = current.getNextBlock();
    }
  }

  return commands;
}

function setCommands(commands) {
  commandOutput.value = commands.join('\n');
}

function appendLog(message) {
  const timestamp = new Date().toLocaleTimeString();
  logOutput.textContent += `[${timestamp}] ${message}\n`;
  logOutput.scrollTop = logOutput.scrollHeight;
}

const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
const ws = new WebSocket(`${protocol}://${location.host}/ws`);

ws.addEventListener('open', () => appendLog('WebSocket接続成功'));
ws.addEventListener('close', () => appendLog('WebSocket切断'));
ws.addEventListener('message', (event) => {
  try {
    const data = JSON.parse(event.data);
    if (data.type === 'status') {
      appendLog(`状態: ${data.message}`);
    } else if (data.type === 'result') {
      for (const item of data.results) {
        appendLog(`${item.command} => ${item.response}`);
      }
    } else if (data.type === 'error') {
      appendLog(`エラー: ${data.message}`);
    }
  } catch {
    appendLog(`受信: ${event.data}`);
  }
});

async function triggerEmergencyStop() {
  if (ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({
      type: 'execute',
      commands: ['emergency']
    }));
    appendLog('緊急停止送信(WebSocket)');
    return;
  }

  const response = await fetch('/api/emergency', {
    method: 'POST'
  });

  if (!response.ok) {
    let detail = '';
    try {
      const errorData = await response.json();
      detail = errorData?.message ? `: ${errorData.message}` : '';
    } catch {
      detail = response.statusText ? `: ${response.statusText}` : '';
    }
    throw new Error(`HTTP ${response.status}${detail}`);
  }

  const data = await response.json();
  appendLog(`緊急停止送信(HTTP): ${data.response}`);
}

convertBtn.addEventListener('click', () => {
  const commands = collectCommands();
  setCommands(commands);
  appendLog(`コマンド変換: ${commands.length} 件`);
});

executeBtn.addEventListener('click', () => {
  const commands = collectCommands();
  setCommands(commands);

  if (commands.length === 0) {
    appendLog('実行するコマンドがありません');
    return;
  }

  if (ws.readyState !== WebSocket.OPEN) {
    appendLog('WebSocket未接続のため実行できません');
    return;
  }

  ws.send(JSON.stringify({
    type: 'execute',
    commands
  }));
  appendLog(`実行送信: ${commands.length} 件`);
});

emergencyBtn.addEventListener('click', async () => {
  try {
    await triggerEmergencyStop();
  } catch (error) {
    appendLog(`緊急停止失敗: ${error instanceof Error ? error.message : 'unknown error'}`);
  }
});
