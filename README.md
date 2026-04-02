# Telliyaki

スマートフォンのブラウザで完結する、Blockly風のTelloブロックプログラミングアプリです。  
ブロック操作で `cw 90` や `forward 50` などのTello SDKコマンドに変換し、実行ボタンでTelloに送信します。

## 前提

- スマホとこのサーバー実行端末が、Telloが作成する同一ローカルネットワーク内にあること
- Tello SDKコマンド仕様は以下を参考
  - https://python.joho.info/robot/tello-python-sdk/

## セットアップ

```bash
npm install
npm run build
npm start
```

`npm install` 時に Blockly をローカル依存として取得するため、実行時は外部CDN不要でオフライン動作します。

デフォルトでは `http://localhost:3000` で起動します。

## 使い方

1. ブラウザでアプリを開く（Tello Wi-Fi接続中でもインターネット不要）
2. Blocklyワークスペースに「回転」「移動」「離陸」「着陸」ブロックを並べる
   - 必要に応じて「待機」ブロックで任意秒数の待機を挿入可能
3. 「コマンド変換」でテキストコマンドを確認
4. 「実行」でコマンドを送信
5. 通信切断時は「緊急停止」でHTTPフォールバック経由の emergency 送信が可能
6. 通常コマンドの送信間隔はデフォルト3秒（`wait` ブロックで個別上書き可能）

## 通信方式

ブラウザから直接UDPは送れないため、以下の2段で送信します。

- ブラウザ → WebSocket (`/ws`)
- Nodeサーバー → UDP (`TELLO_HOST:TELLO_PORT`, 既定 `192.168.10.1:8889`)

環境変数:

- `PORT`: HTTP/WebSocket待受ポート (既定: `3000`)
- `TELLO_HOST`: Tello IP (既定: `192.168.10.1`)
- `TELLO_PORT`: Tello UDPポート (既定: `8889`)
- `LOCAL_UDP_PORT`: ローカルUDPバインドポート (既定: `9000`)
- `DEFAULT_COMMAND_INTERVAL_MS`: コマンド間待機ミリ秒 (既定: `3000`)
