package com.dxgabalt.matrixcell

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.widget.Toast

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el contexto global
        AndroidContext.appContext = applicationContext

        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Verificar si la app es Device Owner y activar modo kiosco
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            devicePolicyManager.setLockTaskPackages(componentName, arrayOf(packageName))

            if (!activityManager.isInLockTaskMode) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    startLockTask() // Iniciar modo kiosco en versiones modernas
                    println("🔒 Modo Kiosco activado")
                }
            }
        } else {
            Toast.makeText(
                this,
                "Debe configurar la app como Device Owner para bloquear el dispositivo.",
                Toast.LENGTH_LONG
            ).show()
            requestDeviceAdmin()
        }

        setContent {
            App()  // Carga la UI de Compose
        }
    }

    private fun requestDeviceAdmin() {
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "Esta aplicación necesita permisos administrativos para funcionar correctamente."
        )
        startActivity(intent)
    }

    override fun onPause() {
         super.onPause()
         startLockTask() // Asegurar que la app no se minimice
    }

    override fun onBackPressed() {
        // Bloquear el botón "Atrás"
        Toast.makeText(this, "No puedes salir de la aplicación.", Toast.LENGTH_SHORT).show()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Interceptar las teclas físicas
        when (event.keyCode) {
            KeyEvent.KEYCODE_BACK,  // Botón "Atrás"
            KeyEvent.KEYCODE_HOME,  // Botón "Inicio"
            KeyEvent.KEYCODE_APP_SWITCH,  // Botón "Multitarea"
            KeyEvent.KEYCODE_POWER,  // Botón de encendido
            KeyEvent.KEYCODE_VOLUME_UP,  // Subir volumen
            KeyEvent.KEYCODE_VOLUME_DOWN -> {  // Bajar volumen
                Toast.makeText(this, "Acción bloqueada.", Toast.LENGTH_SHORT).show()
                return true  // Ignorar el evento de la tecla
            }
        }
        return super.dispatchKeyEvent(event)
    }

}
