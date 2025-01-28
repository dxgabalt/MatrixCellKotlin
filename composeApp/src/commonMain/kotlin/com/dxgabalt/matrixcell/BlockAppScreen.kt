package com.dxgabalt.matrixcell

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow

class BlockAppScreen:Screen{
    @Composable
    override fun Content() {
        val navigator:Navigator = LocalNavigator.currentOrThrow
        var unlockCode by remember { mutableStateOf("") }
        var emergencyCode by remember { mutableStateOf("") }
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Dispositivo Bloqueado", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Código de Desbloqueo
            OutlinedTextField(
                value = unlockCode,
                onValueChange = { unlockCode = it },
                label = { Text("Código de Desbloqueo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Código de Emergencia
            OutlinedTextField(
                value = emergencyCode,
                onValueChange = { emergencyCode = it },
                label = { Text("Código de Emergencia") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar error si existe
            /*if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colors.error)
            Spacer(modifier = Modifier.height(8.dp))
        }*/

            // Botones
            Button(
                onClick = {
                // viewModel.handleUnlock(unlockCode, onNavigateToUnlockRequestScreen)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Desbloquear")
            }
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { //viewModel.handleEmergencyCode(emergencyCode, onNavigateToUnlockRequestScreen)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Código de Emergencia")
            }
        }
    }
}