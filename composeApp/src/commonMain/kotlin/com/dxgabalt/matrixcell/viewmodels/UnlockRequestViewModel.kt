package com.dxgabalt.matrixcell.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.navigator.Navigator
import com.dxgabalt.matrixcell.ApiClient
import com.dxgabalt.matrixcell.BlockAppScreen
import com.dxgabalt.matrixcell.network.DeviceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UnlockRequestViewModel(
    private val deviceRepository: DeviceRepository
) : ViewModel()  {

    private val apiClient = ApiClient()

    private val _errorMessage = MutableStateFlow("")
    private val _webSocketMessages = MutableStateFlow("")
    val webSocketMessages: StateFlow<String> get() = _webSocketMessages
    private val _isEmergencyEnabled = MutableStateFlow(false)

    fun handleSubmit(
        codigoId: String,
        voucherPago: String,
        imei:String,
        navigator: Navigator,
    ) {
        if (codigoId.isBlank() || voucherPago.isBlank()) {
            _errorMessage.value = "Por favor, complete todos los campos."
            return
        }

        // Usa el viewModelScope para manejar el ciclo de vida
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ip = ""      // Dinámico
                val response = apiClient.unlockRequest(codigoId, voucherPago, imei, ip)
                if (response.status == "success") {
                    navigator.push(BlockAppScreen())
                } else {
                    _errorMessage.value = response.message
                    _isEmergencyEnabled.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al conectar con el servidor. Inténtelo de nuevo."
                _isEmergencyEnabled.value = true
            }
        }
    }
    fun handleValidationSubmit(
        codigo: String,
        navigator: Navigator,
    ) {
        if (codigo.isBlank() ) {
            _errorMessage.value = "Por favor, complete todos los campos."
            return
        }

        // Usa el viewModelScope para manejar el ciclo de vida
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imei = "123456789012345" // Dinámico
                val ip = "192.168.0.1"      // Dinámico
                val response = apiClient.unlockValidate(codigo, imei)
                if (response.status == "success") {
                    navigator.push(BlockAppScreen())
                } else {
                    _errorMessage.value = response.message
                    _isEmergencyEnabled.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al conectar con el servidor. Inténtelo de nuevo."
                _isEmergencyEnabled.value = true
            }
        }
    }

    fun handleEmergencyCode(emergencyCode: String, navigator: Navigator) {
        if (emergencyCode == "Matrixcell2025") {
            navigator.push(BlockAppScreen())
        } else {
            _errorMessage.value = "Código de emergencia incorrecto."
        }
    }

    fun connectToSocket(serverUrl: String, androidId: String) {
        viewModelScope.launch {
            deviceRepository.connectToSocket(serverUrl, androidId)
        }
    }

    override fun onCleared() {
        deviceRepository.disconnectSocket()
        super.onCleared()
    }


}
