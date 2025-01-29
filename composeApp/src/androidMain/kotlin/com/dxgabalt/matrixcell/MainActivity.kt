package com.dxgabalt.matrixcell

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
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

        // Verificar si la app es Device Owner
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            devicePolicyManager.setLockTaskPackages(componentName, arrayOf(packageName)) // Permitir solo esta app en modo kiosco
            if (!activityManager.isInLockTaskMode) {
                startLockTask() // Iniciar el modo kiosco
            }
        } else {
            Toast.makeText(this, "Debe configurar la app como Device Owner para bloquear el dispositivo.", Toast.LENGTH_LONG).show()
            requestDeviceAdmin()
        }

        setContent {
            App()
        }
    }

    private fun requestDeviceAdmin() {
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Esta aplicación necesita permisos administrativos para funcionar correctamente.")
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
        // Bloquear botones físicos de navegación y encendido
        when (event.keyCode) {
            KeyEvent.KEYCODE_BACK,
            KeyEvent.KEYCODE_HOME,
            KeyEvent.KEYCODE_APP_SWITCH,
            KeyEvent.KEYCODE_POWER -> {
                Toast.makeText(this, "Acción bloqueada.", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
