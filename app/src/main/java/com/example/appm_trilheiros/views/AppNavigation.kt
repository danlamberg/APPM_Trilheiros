package com.example.appm_trilheiros.views

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appm_trilheiros.models.ItemDao

// AppNavigation.kt
@Composable
fun AppNavigation(
    itemDao: ItemDao,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        // Tela principal (home)
        composable("home") {
            TelaHomeSimplificada( // Renomeando para evitar conflito
                onLogout = onLogout,
                itemDao = itemDao,
                navController = navController
            )
        }

        // Tela de editar perfil (editar_perfil)
        composable("editar_perfil") {
            TelaEditarPerfil(onBack = { navController.popBackStack() })
        }
    }
}

// Renomear a função no arquivo TelaHome.kt
@Composable
fun TelaHomeSimplificada(onLogout: () -> Unit, itemDao: ItemDao, navController: NavController) {
    // Exemplo de botão para navegação
    Button(onClick = { navController.navigate("editar_perfil") }) {
        Text("Editar Perfil")
    }
}
