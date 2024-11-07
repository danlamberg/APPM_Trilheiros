package com.example.appm_trilheiros.views

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appm_trilheiros.R
import com.example.appm_trilheiros.dados.db.ItemDB
import com.example.appm_trilheiros.models.ItemDao
import com.example.appm_trilheiros.repositories.ItemLocalRepository
import com.example.appm_trilheiros.repositories.ItemRemoteRepository
import com.example.appm_trilheiros.ui.theme.APPM_TrilheirosTheme
import com.example.appm_trilheiros.ui.theme.Orange
import com.example.appm_trilheiros.viewmodels.ItensViewModel
import com.example.appm_trilheiros.viewmodels.ItensViewModelFactory
import com.example.appm_trilheiros.auth.AuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var itemDao: ItemDao
    private val authManager = AuthManager() // Instância do AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configura o Firestore com persistência offline
        configurarFirestore()

        val db = abrirBanco()
        itemDao = db.getItemDao()

        setContent {
            APPM_TrilheirosTheme {
                val navController = rememberNavController()
                AppContent(navController = navController, itemDao = itemDao, authManager = authManager)
            }
        }
    }

    // Método para configurar Firestore
    private fun configurarFirestore() {
        // Habilita a persistência offline
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings
    }

    private fun abrirBanco(): ItemDB {
        return ItemDB.abrirBanco(this)
    }
}

@Composable
fun AppContent(
    navController: NavHostController,
    itemDao: ItemDao,
    authManager: AuthManager
) {
    val currentUser = authManager.getCurrentUser()  // Obtém o usuário atual
    var isSignedIn by remember { mutableStateOf(currentUser != null) }

    LaunchedEffect(currentUser) {
        isSignedIn = currentUser != null
    }

    Scaffold(
        topBar = {
            TopBar()
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isSignedIn) "tela_principal" else "login",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                TelaLogin(
                    navController = navController,
                    onSignIn = {
                        isSignedIn = true
                        navController.navigate("tela_principal") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
            composable("cadastro") {
                TelaCadastro(
                    navController = navController,
                    onSignUp = {
                        isSignedIn = true
                        navController.navigate("tela_principal") {
                            popUpTo("cadastro") { inclusive = true }
                        }
                    }
                )
            }
            composable("tela_principal") {
                val localRepository = ItemLocalRepository(itemDao)
                val remoteRepository = ItemRemoteRepository(itemDao)

                val itensViewModel: ItensViewModel = viewModel(
                    factory = ItensViewModelFactory(localRepository, remoteRepository, LocalContext.current)
                )

                TelaHome(
                    onLogout = {
                        authManager.signOut()  // Usando AuthManager para logout
                        isSignedIn = false
                        navController.navigate("login") {
                            popUpTo("tela_principal") { inclusive = false }
                        }
                    },
                    itemDao = itemDao,
                    itensViewModel = itensViewModel,
                    navController = navController
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    val fontWeight = FontWeight(integerResource(id = R.integer.peso))

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(id = R.string.app_name),
                fontSize = 20.sp,
                fontWeight = fontWeight,
                color = Color.White
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Black
        ),
        modifier = Modifier.statusBarsPadding()
    )
}

@Composable
fun TelaLogin(navController: NavHostController, onSignIn: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var mostrarErro by remember { mutableStateOf(false) }

    val authManager = AuthManager()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
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
                authManager.signIn(email, senha) { isSuccess ->
                    if (isSuccess) {
                        onSignIn()
                    } else {
                        mostrarErro = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Orange)
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
            color = Orange,
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

    val authManager = AuthManager()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
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
                    authManager.signUp(email, password) { isSuccess ->
                        if (isSuccess) {
                            successMessage = "Cadastro bem-sucedido!"
                            errorMessage = null
                            onSignUp()
                        } else {
                            errorMessage = "Erro no cadastro. Tente novamente."
                            successMessage = null
                        }
                    }
                } else {
                    errorMessage = "Todos os campos são obrigatórios."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Orange)
        ) {
            Text("Cadastrar")
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage ?: "", color = Color.Red)
        }

        if (successMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = successMessage ?: "", color = Color.Green)
        }
    }
}
