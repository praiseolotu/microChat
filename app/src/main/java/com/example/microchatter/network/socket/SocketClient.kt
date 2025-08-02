package com.example.microchatter.network.socket

import kotlinx.coroutines.*
import java.io.PrintWriter
import java.net.InetAddress
import java.net.Socket

class SocketClient(private val host: InetAddress, private val port: Int = 8888) {

    fun send(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val socket = Socket(host, port)
                val writer = PrintWriter(socket.getOutputStream(), true)
                writer.println(message)
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}