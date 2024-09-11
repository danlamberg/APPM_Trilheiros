package com.example.appm_trilheiros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appm_trilheiros.TelaHome
import com.example.appm_trilheiros.ui.theme.APPM_TrilheirosTheme

// Importações do Firebase Authentication (comentadas)
// import com.google.firebase.auth.FirebaseAuth
// import com.google.firebase.auth.ktx.auth
// import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    // Firebase Authentication desativado temporariamente
    // private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inicialização do Firebase comentada
        // auth = Firebase.auth

        setContent {
            APPM_TrilheirosTheme {
                val navController = rememberNavController()

                // Passando o auth como parâmetro está desativado
                // AppContent(navController = navController, auth = auth)
                AppContent(navController = navController)
            }
        }
    }
}

@Composable
// Função preparada para reativar Firebase
// fun AppContent(navController: NavHostController, auth: FirebaseAuth) {
fun AppContent(navController: NavHostController) {
    // var isSignedIn by remember { mutableStateOf(auth.currentUser != null) }
    var isSignedIn by remember { mutableStateOf(false) }

    // Lógica para atualizar o estado de autenticação
    // LaunchedEffect(auth.currentUser) {
    //     isSignedIn = auth.currentUser != null
    // }

    if (isSignedIn) {
        TelaHome(onLogout = {
            // auth.signOut() // Lógica para logout com Firebase
            isSignedIn = false
        })
    } else {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                // TelaLogin(navController, auth) // Firebase desativado
                TelaLogin(navController)
            }
            composable("cadastro") {
                // TelaCadastro(navController, auth) // Firebase desativado
                TelaCadastro(navController)
            }
        }
    }
}

@Composable
// Função TelaLogin sem Firebase Authentication ativo
// fun TelaLogin(navController: NavHostController, auth: FirebaseAuth) {
fun TelaLogin(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarErro by remember { mutableStateOf(false) }

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
                // Lógica de autenticação com Firebase desativada
                // auth.signInWithEmailAndPassword(email, senha)
                //     .addOnCompleteListener { task ->
                //         if (task.isSuccessful) {
                //             mostrarErro = false
                //             navController.navigate("home")
                //         } else {
                //             mostrarErro = true
                //         }
                //     }
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
// Função TelaCadastro sem Firebase Authentication ativo
// fun TelaCadastro(navController: NavHostController, auth: FirebaseAuth) {
fun TelaCadastro(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarErro by remember { mutableStateOf(false) }
    var sucesso by remember { mutableStateOf(false) }

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
                // Lógica de cadastro com Firebase desativada
                // auth.createUserWithEmailAndPassword(email, senha)
                //     .addOnCompleteListener { task ->
                //         if (task.isSuccessful) {
                //             sucesso = true
                //             navController.navigate("home")
                //         } else {
                //             mostrarErro = true
                //         }
                //     }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cadastrar")
        }

        if (mostrarErro) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Falha no cadastro.",
                color = Color.Red
            )
        }

        if (sucesso) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cadastro realizado com sucesso!",
                color = Color.Green
            )
        }
    }
}
