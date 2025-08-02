package com.example.microchatter.network.socket

import android.util.Log
import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket

class SocketServer(
    private val port: Int = 8888,
    private val onMessageReceived: (String) -> Unit
) {
    private var serverSocket: ServerSocket? = null

    fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(port)
                val socket: Socket = serverSocket!!.accept()
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (true) {
                    val message = reader.readLine() ?: break
                    onMessageReceived(message)
                }
            } catch (e: IOException) {
                Log.e("SocketServer", "Error: ${e.message}")
            } finally {
                serverSocket?.close()
            }
        }
    }

    fun stop() {
        serverSocket?.close()
    }
}