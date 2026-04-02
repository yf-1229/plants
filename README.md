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

デフォルトでは `http://localhost:3000` で起動します。

## 使い方

1. ブラウザでアプリを開く
2. Blocklyワークスペースに「回転」「移動」「離陸」「着陸」ブロックを並べる
3. 「コマンド変換」でテキストコマンドを確認
4. 「実行」でコマンドを送信

## 通信方式

ブラウザから直接UDPは送れないため、以下の2段で送信します。

- ブラウザ → WebSocket (`/ws`)
- Nodeサーバー → UDP (`TELLO_HOST:TELLO_PORT`, 既定 `192.168.10.1:8889`)

環境変数:

- `PORT`: HTTP/WebSocket待受ポート (既定: `3000`)
- `TELLO_HOST`: Tello IP (既定: `192.168.10.1`)
- `TELLO_PORT`: Tello UDPポート (既定: `8889`)
- `LOCAL_UDP_PORT`: ローカルUDPバインドポート (既定: `9000`)
