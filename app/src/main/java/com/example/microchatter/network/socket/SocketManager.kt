package com.example.microchatter.network.socket

import java.net.InetAddress

class SocketManager {

    private var server: SocketServer? = null
    private var client: SocketClient? = null

    fun startServer(onMessage: (String) -> Unit) {
        server = SocketServer(onMessageReceived = onMessage)
        server?.start()
    }

    fun setClientTarget(ip: InetAddress) {
        client = SocketClient(host = ip)
    }

    fun send(message: String) {
        client?.send(message)
    }

    fun stopServer() {
        server?.stop()
    }
}