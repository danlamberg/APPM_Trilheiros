package com.example.appm_trilheiros.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.widget.Toast
import com.example.appm_trilheiros.viewmodels.ItensViewModel

class ReconnectionReceiver(
    private val context: Context,
    private val itensViewModel: ItensViewModel
) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Flag para evitar sincronização repetida
    private var isSyncing = false

    // Callback para monitorar mudanças de conectividade
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            // Quando a rede está disponível, sincroniza os itens não sincronizados
            if (!isSyncing) {
                isSyncing = true
                itensViewModel.sincronizarItensNaoSincronizados()
                Toast.makeText(context, "Sincronizando itens...", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            // Quando a rede é perdida, exibe uma mensagem de erro
            Toast.makeText(context, "Sem conexão com a internet", Toast.LENGTH_SHORT).show()
            isSyncing = false // Pode ser uma boa ideia resetar o estado da sincronização
        }
    }

    // Registra o callback para monitorar a conectividade
    fun registerReceiver() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    // Desregistra o callback
    fun unregisterReceiver() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
