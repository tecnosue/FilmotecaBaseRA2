package com.campusdigitalfp.filmotecav2.viewmodel

import android.app.Application
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = translateErrorFirebase(task.exception)
                    onResult(false, errorMessage)
                }
            }
    }

    fun loginUser(email: String?, password: String?, onResult: (Boolean, String?) -> Unit) {
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            onResult(false, "El correo y la contraseña no pueden estar vacíos.")
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = translateErrorFirebase(task.exception)
                    onResult(false, errorMessage)
                }
            }
    }

    fun signInAnonymously(onResult: (Boolean, String?) -> Unit) {
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    val errorMessage = translateErrorFirebase(task.exception)
                    onResult(false, errorMessage)
                }
            }
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        return GoogleSignIn.getClient(
            getApplication(),
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("709939143951-fce3p1r6vstajg5s3equggh52fqqmn55.apps.googleusercontent.com") // Reemplaza con tu Web client ID
                .requestEmail()
                .build()
        )
    }

    fun signInWithGoogle(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        val googleSignInClient = getGoogleSignInClient()
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }



    suspend fun handleGoogleSignInResult(data: Intent?): Boolean {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val googleIdToken = account.idToken

            if (googleIdToken != null) {
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(credential).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("AuthViewModel", "Google Sign-In failed: ${e.message}", e)
            false
        }
    }

    private fun translateErrorFirebase(exception: Exception?): String {
        return when ((exception as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo electrónico no tiene un formato válido."
            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta."
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo."
            "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada."
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos, intenta más tarde."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo ya está registrado."
            "ERROR_NETWORK_REQUEST_FAILED" -> "No se pudo conectar a la red."
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil."
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Este correo ya está registrado con otro método."
            else -> "Ocurrió un error desconocido. Intenta nuevamente."
        }
    }
}