package com.example.appm_trilheiros.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.appm_trilheiros.ui.theme.Orange
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun TelaEditarPerfil(onBack: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var feedbackMessage by remember { mutableStateOf("") } // Mensagem de feedback ao usuário

    val auth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text("Editar Perfil", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val user = auth.currentUser

                // Validação dos campos
                if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                    feedbackMessage = "Por favor, preencha todos os campos."
                    return@Button
                }

                // Atualizando email e senha
                user?.let {
                    it.updateEmail(email).addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            it.updatePassword(senha).addOnCompleteListener { passwordTask ->
                                if (passwordTask.isSuccessful) {
                                    feedbackMessage = "Perfil atualizado com sucesso!"
                                    onBack() // Retorna para a tela anterior
                                } else {
                                    feedbackMessage = "Erro ao atualizar a senha: ${passwordTask.exception?.message}"
                                }
                            }
                        } else {
                            feedbackMessage = "Erro ao atualizar o email: ${emailTask.exception?.message}"
                        }
                    }
                } ?: run {
                    feedbackMessage = "Usuário não autenticado."
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Orange)
        ) {
            Text("Atualizar Perfil")
        }

        Spacer(modifier = Modifier.height(8.dp))
        feedbackMessage.let {
            if (it.isNotEmpty()) {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
