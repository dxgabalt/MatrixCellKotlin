package com.dxgabalt.matrixcell.network

import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SocketManager {
    private var socket: Socket? = null
    private val _deviceStatus = MutableStateFlow<DeviceStatus>(DeviceStatus.Unknown)
    val deviceStatus: StateFlow<DeviceStatus> = _deviceStatus

    fun initSocket(serverUrl: String, androidId: String) {
        try {
            val options = IO.Options().apply {
                forceNew = true
                reconnection = true
            }

            socket = IO.socket(serverUrl, options)

            setupSocketEvents(androidId)
            socket?.connect()
        } catch (e: Exception) {
            println("Error initializing socket: ${e.message}")
        }
    }

    private fun setupSocketEvents(androidId: String) {
        socket?.apply {
            // Conectar al canal específico del dispositivo
            on(Socket.EVENT_CONNECT) {
                emit("join-device", androidId)
                println("Connected to socket and joined device channel")
            }

            // Manejar evento de bloqueo
            on("device-blocked") { args ->
                val data = args.firstOrNull()?.toString()
                _deviceStatus.value = DeviceStatus.Blocked(data ?: "Device blocked")
            }

            // Manejar evento de desbloqueo
            on("device-unblocked") { args ->
                val data = args.firstOrNull()?.toString()
                _deviceStatus.value = DeviceStatus.Unblocked(data ?: "Device unblocked")
            }

            // Manejar desconexión
            on(Socket.EVENT_DISCONNECT) {
                _deviceStatus.value = DeviceStatus.Disconnected
            }

            // Manejar errores
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()?.toString()
                _deviceStatus.value = DeviceStatus.Error(error ?: "Unknown error")
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
    }

    fun isConnected(): Boolean = socket?.connected() == true
}

sealed class DeviceStatus {
    object Unknown : DeviceStatus()
    object Disconnected : DeviceStatus()
    data class Blocked(val reason: String) : DeviceStatus()
    data class Unblocked(val message: String) : DeviceStatus()
    data class Error(val message: String) : DeviceStatus()
}