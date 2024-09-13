package com.example.appm_trilheiros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appm_trilheiros.model.Produto
import com.example.appm_trilheiros.ui.theme.APPM_TrilheirosTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            APPM_TrilheirosTheme {
                val navController = rememberNavController()
                AppContent(navController = navController)
            }
        }
    }
}

@Composable
fun AppContent(navController: NavHostController) {
    var isSignedIn by remember { mutableStateOf(false) }

    NavHost(navController = navController, startDestination = if (isSignedIn) "tela_principal" else "login") {
        composable("login") {
            TelaLogin(
                navController = navController,
                onSignIn = {
                    isSignedIn = true
                }
            )
        }
        composable("cadastro") {
            TelaCadastro(
                navController = navController,
                onSignUp = {
                    navController.navigate("login")
                }
            )
        }
        composable("tela_principal") {
            TelaHome(onLogout = {
                isSignedIn = false
                navController.navigate("login") {
                    // Limpar a pilha de navegação
                    popUpTo("login") { inclusive = true }
                }
            })
        }
    }
}

@Composable
fun TelaLogin(navController: NavHostController, onSignIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarErro by remember { mutableStateOf(false) }

    val auth: FirebaseAuth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = senha,
            onValueChange = { senha = it },
            label = { Text("Senha") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onSignIn()
                        } else {
                            mostrarErro = true
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Entrar")
        }

        if (mostrarErro) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Falha na autenticação.",
                color = Color.Red
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Não tem uma conta? Cadastre-se",
            color = Color.Blue,
            modifier = Modifier.clickable {
                navController.navigate("cadastro")
            }
        )
    }
}

@Composable
fun TelaCadastro(navController: NavHostController, onSignUp: () -> Unit) {
    var nome by remember { mutableStateOf("") }
    var sobrenome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val auth: FirebaseAuth = Firebase.auth

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White)
    ) {
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = sobrenome,
            onValueChange = { sobrenome = it },
            label = { Text("Sobrenome") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (nome.isNotEmpty() && sobrenome.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                successMessage = "Cadastro bem-sucedido!"
                                errorMessage = null
                                onSignUp()
                            } else {
                                errorMessage = task.exception?.message
                                successMessage = null
                            }
                        }
                } else {
                    errorMessage = "Por favor, preencha todos os campos."
                }
            }
        ) {
            Text("Cadastrar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        errorMessage?.let {
            Text(text = it, color = Color.Red)
        }
        successMessage?.let {
            Text(text = it, color = Color.Green)
        }
    }
}