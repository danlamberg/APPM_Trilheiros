import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.appm_trilheiros.ui.theme.Orange
import com.google.firebase.auth.FirebaseAuth
import android.util.Patterns

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaCadastro(navController: NavHostController) {
    var nome by remember { mutableStateOf("") }
    var sobrenome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val auth = FirebaseAuth.getInstance()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Close keyboard when navigating
    LaunchedEffect(Unit) {
        keyboardController?.hide()
    }

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
                // Campo de Nome
                TextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                keyboardController?.hide()
                            }
                        }
                        .focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de Sobrenome
                TextField(
                    value = sobrenome,
                    onValueChange = { sobrenome = it },
                    label = { Text("Sobrenome") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                keyboardController?.hide()
                            }
                        }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de Email
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    isError = !Patterns.EMAIL_ADDRESS.matcher(email).matches(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                keyboardController?.hide()
                            }
                        }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de Senha
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                keyboardController?.hide()
                            }
                        }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Botão de Cadastro
                Button(
                    onClick = {
                        keyboardController?.hide()

                        if (nome.isEmpty() || sobrenome.isEmpty() || email.isEmpty() || password.isEmpty()) {
                            errorMessage = "Por favor, preencha todos os campos."
                        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            errorMessage = "Formato de e-mail inválido."
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        successMessage = "Cadastro bem-sucedido!"
                                        errorMessage = null


                                        navController.navigate("tela_principal") {
                                            popUpTo("cadastro") { inclusive = true }
                                        }
                                    } else {
                                        errorMessage = "Falha no cadastro. ${task.exception?.localizedMessage}"
                                    }
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text("Cadastrar")
                }

                // Mensagens de erro ou sucesso
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
