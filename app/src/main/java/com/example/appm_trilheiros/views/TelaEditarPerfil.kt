package com.example.appm_trilheiros.views

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import android.util.Patterns
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaEditarPerfil(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    var nome by remember { mutableStateOf(user?.displayName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var senha by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Editar Perfil", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                isError = !Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text("Nova Senha (opcional)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    keyboardController?.hide()

                    if (nome.isEmpty() || email.isEmpty()) {
                        errorMessage = "Por favor, preencha todos os campos obrigatórios."
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Formato de e-mail inválido."
                    } else {
                        coroutineScope.launch {
                            user?.let { currentUser ->
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(nome)
                                    .build()

                                // Atualizando o nome
                                val profileTask = currentUser.updateProfile(profileUpdates)
                                if (!profileTask.isSuccessful) {
                                    errorMessage = "Falha ao atualizar o nome: ${profileTask.exception?.localizedMessage}"
                                } else {
                                    successMessage = "Nome atualizado com sucesso!"
                                }

                                // Atualizando email, se necessário
                                if (currentUser.email != email) {
                                    val emailTask = currentUser.updateEmail(email)
                                    if (!emailTask.isSuccessful) {
                                        errorMessage = "Falha ao atualizar o email: ${emailTask.exception?.localizedMessage}"
                                    } else {
                                        successMessage = "Email atualizado com sucesso!"
                                    }
                                }

                                // Atualizando senha, se fornecida
                                if (senha.isNotEmpty()) {
                                    val passwordTask = currentUser.updatePassword(senha)
                                    if (!passwordTask.isSuccessful) {
                                        errorMessage = "Falha ao atualizar a senha: ${passwordTask.exception?.localizedMessage}"
                                    } else {
                                        successMessage = "Senha atualizada com sucesso!"
                                    }
                                }
                            } ?: run {
                                errorMessage = "Usuário não está logado."
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Alterações")
            }

            // Exibindo mensagens de sucesso ou erro
            Spacer(modifier = Modifier.height(16.dp))
            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar")
            }
        }
    }
}
