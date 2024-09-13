package com.example.appm_trilheiros

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuthException
import java.util.regex.Pattern

// Função para validar o e-mail
private fun isEmailValid(email: String): Boolean {
    val emailPattern = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )
    return emailPattern.matcher(email).matches()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastro(navController: NavHostController) {
    var nome by remember { mutableStateOf("") }
    var sobrenome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val auth: FirebaseAuth = Firebase.auth

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cadastro") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = sobrenome,
                    onValueChange = { sobrenome = it },
                    label = { Text("Sobrenome") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it.trim() },  // Remove espaços extras
                    label = { Text("Email") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                )
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        when {
                            nome.isEmpty() || sobrenome.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                                errorMessage = "Por favor, preencha todos os campos."
                                successMessage = null
                            }
                            !isEmailValid(email) -> {
                                errorMessage = "O e-mail fornecido não é válido."
                                successMessage = null
                            }
                            else -> {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            successMessage = "Cadastro bem-sucedido!"
                                            errorMessage = null
                                            // Navegar para a tela principal após o cadastro bem-sucedido
                                            navController.navigate("tela_principal") {
                                                // Limpar a pilha de navegação
                                                popUpTo("cadastro") { inclusive = true }
                                            }
                                        } else {
                                            val exception = task.exception
                                            errorMessage = when (exception) {
                                                is FirebaseAuthException -> {
                                                    when (exception.errorCode) {
                                                        "ERROR_EMAIL_ALREADY_IN_USE" -> "O e-mail já está cadastrado. Tente fazer login ou use um e-mail diferente."
                                                        "ERROR_WEAK_PASSWORD" -> "A senha fornecida é muito fraca. Tente uma senha mais forte."
                                                        else -> "Erro: ${exception.message}"
                                                    }
                                                }
                                                else -> "Erro: ${exception?.message ?: "Desconhecido"}"
                                            }
                                            successMessage = null
                                        }
                                    }
                            }
                        }
                    }
                ) {
                    Text("Cadastrar")
                }

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                successMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    )
}
