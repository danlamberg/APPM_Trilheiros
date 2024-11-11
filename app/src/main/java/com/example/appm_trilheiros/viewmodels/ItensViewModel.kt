package com.example.appm_trilheiros.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.repositories.ItemLocalRepository
import com.example.appm_trilheiros.repositories.ItemRemoteRepository
import com.example.appm_trilheiros.utils.ConnectionUtil
import com.example.appm_trilheiros.utils.ReconnectionReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ItensViewModel(
    private val localRepository: ItemLocalRepository,
    private val remoteRepository: ItemRemoteRepository,
    private val context: Context
) : ViewModel(), KoinComponent {

    private val _itens = MutableStateFlow<List<Item>>(emptyList())
    val itens: StateFlow<List<Item>> get() = _itens

    private val reconnectionReceiver: ReconnectionReceiver by lazy {
        ReconnectionReceiver(context, this)
    }

    init {
        // Registra o receiver de reconexão para monitorar a conectividade
        reconnectionReceiver.registerReceiver()

        // Monitora os itens no banco de dados local e evita duplicação
        viewModelScope.launch {
            localRepository.listarFlow().collectLatest { lista ->
                _itens.value = lista.distinctBy { it.id } // Garante que itens duplicados sejam removidos
            }
        }
    }

    // Função para sincronizar itens que ainda não foram sincronizados
    fun sincronizarItensNaoSincronizados() {
        viewModelScope.launch {
            try {
                val itensNaoSincronizados = localRepository.listarItensNaoSincronizados()
                for (item in itensNaoSincronizados) {
                    remoteRepository.gravar(item) // Grava os itens não sincronizados no Firestore
                }
                Log.d("ItensViewModel", "Itens não sincronizados foram gravados remotamente.")
            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao sincronizar itens: ${e.message}")
            }
        }
    }

    fun gravarItem(item: Item) {
        viewModelScope.launch {
            try {
                // Verifica se o item já existe no banco local pelo firestoreId ou pela descrição
                val existingItem = localRepository.buscarPorFirestoreId(item.firestoreId)
                    ?: localRepository.buscarPorDescricao(item.descricao)

                // Se o item já existe, não faz nada
                if (existingItem != null) {
                    Log.d("ItensViewModel", "Item já existe localmente, não será gravado novamente.")
                    return@launch
                }

                // Grava o item no banco local e no Firestore
                //localRepository.gravar(item)
                remoteRepository.gravar(item)  // Grava no Firestore
                Log.d("ItensViewModel", "Item gravado com sucesso: ${item.descricao}")
            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao gravar item: ${e.message}")
            }
        }
    }

    // Desregistra o receiver quando o ViewModel for destruído
    override fun onCleared() {
        super.onCleared()
        reconnectionReceiver.unregisterReceiver()
    }
}
