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
                // Atualiza o estado com a lista de itens sem duplicações
                _itens.value = lista.distinctBy { it.id }
            }
        }
    }

    fun sincronizarItensNaoSincronizados() {
        viewModelScope.launch {
            try {
                val itensNaoSincronizados = localRepository.listarItensNaoSincronizados()
                for (item in itensNaoSincronizados) {
                    if (item.isMarkedForDeletion) {
                        // Exclui o item do Firestore e do banco local
                        if (!item.firestoreId.isNullOrEmpty()) {
                            remoteRepository.excluir(item)
                            localRepository.excluir(item)
                        } else {
                            localRepository.excluir(item) // Exclui apenas localmente
                        }
                    } else {
                        // Sincroniza o item
                        remoteRepository.gravar(item)
                        item.isSynced = true
                        localRepository.atualizar(item)
                    }
                }
                listarItensOffline()
            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao sincronizar itens: ${e.message}")
            }
        }
    }

    fun gravarItem(item: Item) {
        viewModelScope.launch {
            try {
                // Verifica se o item já existe no banco local
                val existingItem = localRepository.buscarPorFirestoreId(item.firestoreId)
                    ?: localRepository.buscarPorDescricao(item.descricao)

                if (existingItem != null) return@launch // Item já existe, não grava novamente

                val isNetworkAvailable = ConnectionUtil.isNetworkAvailable(context)

                // Se estiver offline, gere um firestoreId temporário
                if (!isNetworkAvailable && item.firestoreId.isEmpty()) {
                    val temporaryFirestoreId = "offline_${System.currentTimeMillis()}"
                    item.firestoreId = temporaryFirestoreId
                }

                if (isNetworkAvailable) {
                    remoteRepository.gravar(item)
                    item.isSynced = true
                    localRepository.atualizar(item)
                } else {
                    item.isSynced = false
                    localRepository.gravar(item) // Grava no banco local
                }

                _itens.value = _itens.value + item
                listarItensOffline()

            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao gravar item: ${e.message}")
            }
        }
    }


    fun excluirItem(item: Item) {
        viewModelScope.launch {
            try {
                if (ConnectionUtil.isNetworkAvailable(context)) {
                    if (item.firestoreId.isNotEmpty()) {
                        remoteRepository.excluir(item) // Exclui do Firestore
                    }
                }
                localRepository.excluir(item) // Exclui no banco local
                listarItensOffline() // Atualiza a lista de itens offline
            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao excluir item: ${e.message}")
            }
        }
    }


    fun excluirItemOffline(item: Item) {
        viewModelScope.launch {
            try {
                item.isMarkedForDeletion = true
                localRepository.atualizar(item) // Atualiza o status no banco local
                listarItensOffline()
            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao marcar item para exclusão: ${e.message}")
            }
        }
    }

    fun atualizarItem(item: Item) {
        viewModelScope.launch {
            try {
                // Verifica se o item existe no banco local
                val existingItem = localRepository.buscarPorId(item.id)

                if (existingItem == null) {
                    Log.e("ItensViewModel", "Item não encontrado para atualização.")
                    return@launch
                }

                val isNetworkAvailable = ConnectionUtil.isNetworkAvailable(context)

                if (isNetworkAvailable) {
                    // Se a rede estiver disponível, atualiza o item no Firestore e no banco local
                    remoteRepository.atualizar(item)
                    item.isSynced = true // Marca como sincronizado
                    localRepository.atualizar(item)
                } else {
                    // Se estiver offline, apenas atualiza localmente e marca como não sincronizado
                    item.isSynced = false
                    localRepository.atualizar(item) // Atualiza apenas no banco local
                }

                listarItensOffline() // Atualiza a lista de itens offline

            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao atualizar item: ${e.message}")
            }
        }
    }

    private fun listarItensOffline() {
        viewModelScope.launch {
            val itensOffline = localRepository.listarItensNaoSincronizados()
            _itens.value = itensOffline
        }
    }
}
