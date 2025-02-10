package com.dxgabalt.matrixcell

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.dxgabalt.matrixcell.model.UnlockRequestPayload
import com.dxgabalt.matrixcell.network.ApiService
import com.dxgabalt.matrixcell.network.HttpClientProvider
import com.dxgabalt.matrixcell.network.SocketManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private val ADMIN_REQUEST = 1
    private val deviceManager = DeviceManager()


    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

        // Verificar si ya somos administradores del dispositivo
        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            requestAdminPrivileges()
        } else {
            // Configurar políticas de seguridad para evitar la desinstalación
            setUninstallBlocked(true)
        }

        // Obtener referencias de los elementos del layout
        val androidIdTextView: TextView = findViewById(R.id.androidIdTextView)
        val codigoIdInput: EditText = findViewById(R.id.codigoIdInput)
        val voucherPagoInput: EditText = findViewById(R.id.voucherPagoInput)
        val submitButton: Button = findViewById(R.id.submitButton)

        // Obtener el Android ID real
        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        androidIdTextView.text = "Android ID: $androidId"
        val socketManager = SocketManager(applicationContext) // Instancia de SocketManager
        socketManager.initSocket("https://matrixcell.onrender.com", androidId) // Iniciar socket con los parámetros
        // Manejo del evento de clic en el botón
        submitButton.setOnClickListener {
            val codigoId = codigoIdInput.text.toString()
            val voucherPago = voucherPagoInput.text.toString()
            val requestData = UnlockRequestPayload(codigoId, voucherPago, androidId, "")
            sendPostRequest(requestData)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun sendPostRequest(requestData: UnlockRequestPayload) {
        if (deviceManager.getInternetConnection(applicationContext)) {
            lifecycleScope.launch {
                try {
                    val apiService = HttpClientProvider.getClient().create(ApiService::class.java)
                    val response = apiService.unlockRequest(requestData)

                    if (response.isSuccessful) {
                        startActivity(Intent(applicationContext, BlockAppActivity::class.java))
                    } else {
                        Toast.makeText(applicationContext, "Error: ${response.message()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Excepción: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(applicationContext, "No hay conexión a internet", Toast.LENGTH_LONG).show()
        }
    }

    private fun requestAdminPrivileges() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Se requieren permisos de administrador para esta aplicación.")
        }
        startActivityForResult(intent, ADMIN_REQUEST)
    }

    private fun setUninstallBlocked(block: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            devicePolicyManager.setUninstallBlocked(adminComponent, packageName, block)
        }
    }
}
