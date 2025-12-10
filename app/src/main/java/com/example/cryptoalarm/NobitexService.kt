package com.example.cryptoalarm

import okhttp3.*
import okio.ByteString
import android.util.Log

object NobitexService {
    private const val WS_URL = "wss://ws.nobitex.ir/connection/websocket"
    private val client = OkHttpClient()
    private var ws: WebSocket? = null

    fun connect(onMessage: (String)->Unit) {
        try {
            val req = Request.Builder().url(WS_URL).build()
            ws = client.newWebSocket(req, object: WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) { Log.i("NobitexService","ws open") }
                override fun onMessage(webSocket: WebSocket, text: String) { onMessage(text) }
                override fun onMessage(webSocket: WebSocket, bytes: ByteString) { onMessage(bytes.utf8()) }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) { Log.e("NobitexService","ws fail: "+t.message) }
            })
        } catch (e: Exception) { Log.e("NobitexService","connect error: "+e.message) }
    }

    fun disconnect() { ws?.close(1000, "bye") }
}
