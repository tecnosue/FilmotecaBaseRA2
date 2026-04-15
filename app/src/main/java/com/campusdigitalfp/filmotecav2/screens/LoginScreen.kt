package com.campusdigitalfp.filmotecav2.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.campusdigitalfp.filmotecav2.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.identity.Identity
@Composable
fun LoginScreen(navController: NavController, viewModel: AuthViewModel) {
// Variables de estado para almacenar el email, la contraseña y los mensajes de error
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
// Manejador del resultado del login con Google
    val googleSignInLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            android.util.Log.d("GoogleSignIn", "resultCode: ${result.resultCode}, data: ${result.data}")
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    val success = viewModel.handleGoogleSignInResult(result.data)
                    android.util.Log.d("GoogleSignIn", "handleResult success: $success")
                    isLoading = false
                    if (success) {
                        navController.navigate("list")
                    } else {
                        errorMessage = "Error al iniciar sesión con Google"
                    }
                }
            } else {
                isLoading = false
                errorMessage = "Inicio de sesión con Google cancelado (resultCode: ${result.resultCode})"
            }
        }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
// Título de la pantalla de inicio de sesión
        Text(text = "Iniciar Sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
// Campo de entrada de email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType =
                KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
// Campo de entrada de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType =
                KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
// Botón para iniciar sesión con email y contraseña
        Button(
            onClick = {
                isLoading = true
                viewModel.loginUser(email, password) { success, error ->
                    isLoading = false
                    if (success) {
                        navController.navigate("list")
                    } else {
                        errorMessage = error
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Iniciar Sesión")
        }
// Botón para iniciar sesión con Google
        Button(
            onClick = {
                isLoading = true
                viewModel.signInWithGoogle(googleSignInLauncher)
            }
        ) {
            Text("Iniciar sesión con Google")
        }

// Botón para iniciar sesión como invitado (anónimo)
        Button(
            onClick = {
                isLoading = true
                viewModel.signInAnonymously { success, error ->
                    isLoading = false
                    if (success) {
                        navController.navigate("list")
                    } else {
                        errorMessage = error
                    }
                }
            },

        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.secondary)){
                Text("Acceder como invitado")}
// Indicador de carga mientras se realiza el proceso de autenticación
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
// Mostrar mensaje de error en caso de fallo en la autenticación
                    errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier =
                    Modifier.padding(top = 8.dp))
            }
                    Spacer(modifier = Modifier.height(16.dp))
// Botón para ir a la pantalla de registro
                    TextButton(onClick = { navController.navigate("register") }) {
                Text("¿No tienes cuenta? Regístrate")
            }
    }
}
