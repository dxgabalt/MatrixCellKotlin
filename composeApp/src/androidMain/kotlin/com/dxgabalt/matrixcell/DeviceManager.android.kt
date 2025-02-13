package com.dxgabalt.matrixcell

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Handler
import android.os.Looper

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DeviceManager {
    val context = AndroidContext.appContext
    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val componentName = ComponentName(context, MyDeviceAdminReceiver::class.java)
    actual fun blockDevice() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            runOnMainThread {
                Toast.makeText(context, "🔒 Dispositivo Bloqueado", Toast.LENGTH_SHORT).show()
            }
            devicePolicyManager.lockNow()
        } else {
            runOnMainThread {
                Toast.makeText(context, "❌ No tienes permisos de administrador", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            Handler(Looper.getMainLooper()).post { action() }
        }
    }

    actual fun unblockDevice() {
        if (devicePolicyManager.isAdminActive(componentName)) {
            try {
                devicePolicyManager.setLockTaskPackages(componentName, emptyArray())
                runOnMainThread {
                    Toast.makeText(context, "🔓 Modo kiosco desactivado.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                runOnMainThread {
                    Toast.makeText(context, "❌ Error al salir del modo kiosco: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            runOnMainThread {
                Toast.makeText(context, "❌ No tienes permisos de administrador para desbloquear.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    actual fun navigateToPayments(){
       // val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://matrix-cell.com/payments"))
      //  context.startActivity(intent)
    }
    actual fun checkInternetConnection(){
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isOnline = networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))

        val message = if (isOnline) "Conexión: Online" else "Conexión: Offline"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    actual fun getInternetConnection():Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val isOnline = networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))

       return isOnline;

    }
    actual fun callSupport(){
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+593987808614"))
        context.startActivity(intent)
    }
}