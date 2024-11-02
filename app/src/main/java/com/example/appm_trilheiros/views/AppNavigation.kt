package com.example.appm_trilheiros.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appm_trilheiros.models.ItemDao
import com.example.appm_trilheiros.views.TelaHome
import com.example.appm_trilheiros.views.TelaEditarPerfil

@Composable
fun AppNavigation(
    navController: NavHostController,
    onLogout: () -> Unit,
    itemDao: ItemDao // Passar itemDao para a TelaHome
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            TelaHome(onLogout = onLogout, itemDao = itemDao, navController = navController)
        }
        composable("editar_perfil") {
            TelaEditarPerfil(onBack = { navController.popBackStack() })
        }
    }
}
