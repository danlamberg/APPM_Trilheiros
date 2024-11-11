package com.example.appm_trilheiros.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object ConnectionUtil {

    // Função para verificar se o dispositivo está online
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    // Função para verificar se a rede está disponível (pode ser um alias para isOnline)
    fun isNetworkAvailable(context: Context): Boolean {
        return isOnline(context) // Reutilizando a lógica de isOnline
    }
}
