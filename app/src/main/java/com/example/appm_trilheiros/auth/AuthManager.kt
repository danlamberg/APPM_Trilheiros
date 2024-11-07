package com.example.appm_trilheiros.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Verifica se o usuário está autenticado
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Retorna o usuário atual
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Função de login
    fun signIn(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // Função de cadastro
    fun signUp(email: String, password: String, onResult: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                onResult(task.isSuccessful)
            }
    }

    // Função de logout
    fun signOut() {
        auth.signOut()
    }
}
