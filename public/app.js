const blocklyDiv = document.getElementById('blocklyDiv');
const commandOutput = document.getElementById('commandOutput');
const logOutput = document.getElementById('logOutput');
const convertBtn = document.getElementById('convertBtn');
const executeBtn = document.getElementById('executeBtn');
const emergencyBtn = document.getElementById('emergencyBtn');
const telemetrySpeed = document.getElementById('telemetrySpeed');
const telemetryBattery = document.getElementById('telemetryBattery');
const telemetryTime = document.getElementById('telemetryTime');
const telemetryWifi = document.getElementById('telemetryWifi');
const locationStatus = document.getElementById('locationStatus');
const locationBtn = document.getElementById('locationBtn');
const locationCoords = document.getElementById('locationCoords');
const roadDescription = document.getElementById('roadDescription');
const roadImageInput = document.getElementById('roadImage');
const roadPostBtn = document.getElementById('roadPostBtn');
const roadLocationLabel = document.getElementById('roadLocationLabel');
const roadImagePreview = document.getElementById('roadImagePreview');
const roadStatus = document.getElementById('roadStatus');
const roadPostsContainer = document.getElementById('roadPosts');

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
    type: 'tello_repeat',
    message0: '繰り返し %1 回',
    args0: [
      {
        type: 'field_number',
        name: 'TIMES',
        value: 2,
        min: 1,
        max: 20
      }
    ],
    message1: '%1',
    args1: [
      {
        type: 'input_statement',
        name: 'DO'
      }
    ],
    previousStatement: null,
    nextStatement: null,
    colour: 180
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
    type: 'tello_left',
    message0: '左に %1 cm 移動',
    args0: [{ type: 'field_number', name: 'DISTANCE', value: 50, min: 20, max: 500 }],
    previousStatement: null,
    nextStatement: null,
    colour: 240
  },
  {
    type: 'tello_right',
    message0: '右に %1 cm 移動',
    args0: [{ type: 'field_number', name: 'DISTANCE', value: 50, min: 20, max: 500 }],
    previousStatement: null,
    nextStatement: null,
    colour: 240
  },
  {
    type: 'tello_forward',
    message0: '前に %1 cm 移動',
    args0: [{ type: 'field_number', name: 'DISTANCE', value: 50, min: 20, max: 500 }],
    previousStatement: null,
    nextStatement: null,
    colour: 240
  },
  {
    type: 'tello_back',
    message0: '後ろに %1 cm 移動',
    args0: [{ type: 'field_number', name: 'DISTANCE', value: 50, min: 20, max: 500 }],
    previousStatement: null,
    nextStatement: null,
    colour: 240
  },
  {
    type: 'tello_up',
    message0: '%1 cm 上昇',
    args0: [{ type: 'field_number', name: 'DISTANCE', value: 50, min: 20, max: 500 }],
    previousStatement: null,
    nextStatement: null,
    colour: 240
  },
  {
    type: 'tello_down',
    message0: '%1 cm 下降',
    args0: [{ type: 'field_number', name: 'DISTANCE', value: 50, min: 20, max: 500 }],
    previousStatement: null,
    nextStatement: null,
    colour: 240
  },
  {
    type: 'tello_flip',
    message0: '%1 方向に反転',
    args0: [
      {
        type: 'field_dropdown',
        name: 'DIRECTION',
        options: [
          ['左', 'l'],
          ['右', 'r'],
          ['前', 'f'],
          ['後ろ', 'b']
        ]
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
        value: 1,
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
    commands.push(...collectCommandsFromChain(top));
  }

  return commands;
}

function collectCommandsFromChain(startBlock) {
  const commands = [];
  let current = startBlock;
  while (current) {
    if (current.type === 'tello_takeoff') {
      commands.push('takeoff');
    } else if (current.type === 'tello_land') {
      commands.push('land');
    } else if (current.type === 'tello_rotate') {
      const direction = current.getFieldValue('DIRECTION');
      const degree = Number(current.getFieldValue('DEGREE'));
      commands.push(`${direction} ${degree}`);
    } else if (current.type === 'tello_left') {
      commands.push(`left ${Number(current.getFieldValue('DISTANCE'))}`);
    } else if (current.type === 'tello_right') {
      commands.push(`right ${Number(current.getFieldValue('DISTANCE'))}`);
    } else if (current.type === 'tello_forward') {
      commands.push(`forward ${Number(current.getFieldValue('DISTANCE'))}`);
    } else if (current.type === 'tello_back') {
      commands.push(`back ${Number(current.getFieldValue('DISTANCE'))}`);
    } else if (current.type === 'tello_up') {
      commands.push(`up ${Number(current.getFieldValue('DISTANCE'))}`);
    } else if (current.type === 'tello_down') {
      commands.push(`down ${Number(current.getFieldValue('DISTANCE'))}`);
    } else if (current.type === 'tello_flip') {
      commands.push(`flip ${current.getFieldValue('DIRECTION')}`);
    } else if (current.type === 'tello_wait') {
      const seconds = Number(current.getFieldValue('SECONDS'));
      commands.push(`wait ${seconds}`);
    } else if (current.type === 'tello_repeat') {
      const times = Number(current.getFieldValue('TIMES'));
      const statement = current.getInputTargetBlock('DO');
      if (statement) {
        const repeated = collectCommandsFromChain(statement);
        for (let i = 0; i < times; i += 1) {
          commands.push(...repeated);
        }
      }
    }
    current = current.getNextBlock();
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
    } else if (data.type === 'telemetry') {
      telemetrySpeed.textContent = data.telemetry?.speed ?? '--';
      telemetryBattery.textContent = data.telemetry?.battery ?? '--';
      telemetryTime.textContent = data.telemetry?.time ?? '--';
      telemetryWifi.textContent = data.telemetry?.wifi ?? '--';
    }
  } catch {
    appendLog(`受信: ${event.data}`);
  }
});

const MAX_ROAD_POSTS = 20;
let latestLocation = null;
let roadPosts = loadRoadPosts();

function setRoadStatus(message, isError = false) {
  roadStatus.textContent = message;
  roadStatus.classList.toggle('error', isError);
}

function formatLocation(location) {
  const latitude = Number(location.latitude);
  const longitude = Number(location.longitude);
  const accuracy = Number(location.accuracy);
  if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
    return '不明';
  }
  const accuracyLabel = Number.isFinite(accuracy) ? ` (±${Math.round(accuracy)}m)` : '';
  return `緯度 ${latitude.toFixed(6)}, 経度 ${longitude.toFixed(6)}${accuracyLabel}`;
}

function updateLocationDisplay() {
  if (latestLocation) {
    locationStatus.textContent = '取得済み';
    locationCoords.textContent = formatLocation(latestLocation);
    roadLocationLabel.textContent = formatLocation(latestLocation);
    return;
  }
  locationStatus.textContent = '未取得';
  locationCoords.textContent = '--';
  roadLocationLabel.textContent = '未取得';
}

function requestLocation() {
  if (!navigator.geolocation) {
    locationStatus.textContent = '未対応';
    locationCoords.textContent = 'この端末では利用できません';
    roadLocationLabel.textContent = '未対応';
    locationBtn.disabled = true;
    return;
  }
  locationStatus.textContent = '取得中...';
  locationCoords.textContent = '取得中...';
  setRoadStatus('');
  navigator.geolocation.getCurrentPosition(
    (position) => {
      latestLocation = {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        accuracy: position.coords.accuracy,
        timestamp: position.timestamp
      };
      updateLocationDisplay();
      setRoadStatus('現在地を更新しました');
    },
    (error) => {
      latestLocation = null;
      locationStatus.textContent = '取得失敗';
      locationCoords.textContent = error.message || '取得に失敗しました';
      roadLocationLabel.textContent = '未取得';
      setRoadStatus('現在地を取得できませんでした', true);
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  );
}

function loadRoadPosts() {
  try {
    const raw = localStorage.getItem('roadPosts');
    if (!raw) {
      return [];
    }
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

function saveRoadPosts() {
  try {
    const limitedPosts = roadPosts.slice(0, MAX_ROAD_POSTS);
    localStorage.setItem('roadPosts', JSON.stringify(limitedPosts));
  } catch {
    setRoadStatus('保存容量の都合でローカル保存できませんでした', true);
  }
}

function renderRoadPosts() {
  roadPostsContainer.textContent = '';
  if (roadPosts.length === 0) {
    const empty = document.createElement('p');
    empty.className = 'road-empty';
    empty.textContent = '投稿がまだありません';
    roadPostsContainer.appendChild(empty);
    return;
  }
  for (const post of roadPosts) {
    const card = document.createElement('article');
    card.className = 'road-post';

    const header = document.createElement('div');
    header.className = 'road-post-header';

    const timeLabel = document.createElement('div');
    const date = new Date(post.createdAt);
    timeLabel.textContent = Number.isNaN(date.getTime())
      ? '投稿日時不明'
      : date.toLocaleString();
    header.appendChild(timeLabel);

    if (post.location) {
      const locationLabel = document.createElement('div');
      locationLabel.textContent = formatLocation(post.location);
      header.appendChild(locationLabel);
    }

    card.appendChild(header);

    if (post.description) {
      const body = document.createElement('p');
      body.textContent = post.description;
      card.appendChild(body);
    }

    if (post.imageData) {
      const image = document.createElement('img');
      image.src = post.imageData;
      image.alt = '道路情報画像';
      card.appendChild(image);
    }

    roadPostsContainer.appendChild(card);
  }
}

function clearRoadForm() {
  roadDescription.value = '';
  roadImageInput.value = '';
  roadImagePreview.textContent = '';
}

function readImageFile(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = () => reject(new Error('画像の読み込みに失敗しました'));
    reader.readAsDataURL(file);
  });
}

async function updateImagePreview(file) {
  roadImagePreview.textContent = '';
  if (!file) {
    return;
  }
  if (!file.type.startsWith('image/')) {
    setRoadStatus('画像ファイルを選択してください', true);
    roadImageInput.value = '';
    return;
  }
  try {
    const imageData = await readImageFile(file);
    const image = document.createElement('img');
    image.src = imageData;
    image.alt = '選択した画像';
    roadImagePreview.appendChild(image);
  } catch {
    setRoadStatus('画像の読み込みに失敗しました', true);
  }
}

roadImageInput.addEventListener('change', () => {
  setRoadStatus('');
  const file = roadImageInput.files?.[0] ?? null;
  void updateImagePreview(file);
});

roadPostBtn.addEventListener('click', async () => {
  setRoadStatus('');
  const description = roadDescription.value.trim();
  const file = roadImageInput.files?.[0] ?? null;

  if (!description && !file) {
    setRoadStatus('本文または画像を入力してください', true);
    return;
  }

  if (file && !file.type.startsWith('image/')) {
    setRoadStatus('画像ファイルを選択してください', true);
    return;
  }

  roadPostBtn.disabled = true;
  let imageData = null;
  if (file) {
    try {
      imageData = await readImageFile(file);
    } catch {
      setRoadStatus('画像の読み込みに失敗しました', true);
      roadPostBtn.disabled = false;
      return;
    }
  }

  const post = {
    id: `${Date.now()}`,
    description,
    imageData,
    location: latestLocation ? { ...latestLocation } : null,
    createdAt: new Date().toISOString()
  };

  roadPosts = [post, ...roadPosts].slice(0, MAX_ROAD_POSTS);
  renderRoadPosts();
  saveRoadPosts();
  clearRoadForm();
  setRoadStatus('投稿しました');
  roadPostBtn.disabled = false;
});

locationBtn.addEventListener('click', () => {
  requestLocation();
});

if (!navigator.geolocation) {
  locationStatus.textContent = '未対応';
  locationCoords.textContent = 'この端末では利用できません';
  roadLocationLabel.textContent = '未対応';
  locationBtn.disabled = true;
} else {
  updateLocationDisplay();
}

renderRoadPosts();

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
