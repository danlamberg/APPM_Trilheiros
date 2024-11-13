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
                // Garante que itens duplicados sejam removidos
                _itens.value = lista.distinctBy { it.id }
            }
        }
    }

    // Função para sincronizar itens que ainda não foram sincronizados
    fun sincronizarItensNaoSincronizados() {
        viewModelScope.launch {
            try {
                val itensNaoSincronizados = localRepository.listarItensNaoSincronizados()
                for (item in itensNaoSincronizados) {
                    if (item.isMarkedForDeletion) {
                        // Se o item já foi sincronizado com o Firestore (tem um firestoreId), exclui remotamente
                        if (!item.firestoreId.isNullOrEmpty()) {
                            remoteRepository.excluir(item)
                            localRepository.excluir(item) // Exclui também localmente após sincronizar
                            Log.d("ItensViewModel", "Item excluído do Firestore: ${item.descricao}")
                        } else {
                            // Se nunca foi sincronizado com o Firestore, apenas exclui localmente
                            localRepository.excluir(item)
                            Log.d(
                                "ItensViewModel",
                                "Item excluído localmente (não foi sincronizado): ${item.descricao}"
                            )
                        }
                    } else {
                        // Sincroniza normalmente se não estiver marcado para exclusão
                        remoteRepository.gravar(item)
                        item.isSynced = true
                        localRepository.atualizar(item)
                        Log.d("ItensViewModel", "Item sincronizado com sucesso: ${item.descricao}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao sincronizar itens: ${e.message}")
            }
        }
    }

    fun gravarItem(item: Item) {
        viewModelScope.launch {
            try {
                val existingItem = localRepository.buscarPorFirestoreId(item.firestoreId)
                    ?: localRepository.buscarPorDescricao(item.descricao)

                if (existingItem != null) {
                    Log.d(
                        "ItensViewModel",
                        "Item já existe localmente, não será gravado novamente."
                    )
                    return@launch
                }

                val isNetworkAvailable = ConnectionUtil.isNetworkAvailable(context)
                Log.d("ItensViewModel", "Verificando conexão: $isNetworkAvailable")

                if (!isNetworkAvailable) {
                    if (item.firestoreId.isNullOrEmpty()) {
                        item.firestoreId =
                            java.util.UUID.randomUUID().toString() // Gera um ID único
                    }
                    item.isSynced = false
                    localRepository.gravar(item) // Grava no banco local
                    Log.d(
                        "ItensViewModel",
                        "Item gravado localmente como não sincronizado: ${item.descricao} com ID temporário: ${item.firestoreId}"
                    )
                } else {
                    Log.d("ItensViewModel", "Conexão com a internet disponível.")
                    remoteRepository.gravar(item)
                    item.isSynced = true // Marca como sincronizado
                    Log.d("ItensViewModel", "Gravando item no Firestore: ${item.descricao}")

                    localRepository.atualizar(item) // Atualiza o item local
                    Log.d(
                        "ItensViewModel",
                        "Item atualizado localmente após sincronização: ${item.descricao}"
                    )
                }

                listarItensOffline()

            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao gravar item: ${e.message}", e)
            }
        }
    }


    // Atualizada para listar apenas os itens que não foram sincronizados
    private fun listarItensOffline() {
        viewModelScope.launch {
            try {
                // Lista os itens que não foram sincronizados (com id temporário ou não sincronizado)
                val itensOffline = localRepository.listarItensNaoSincronizados()

                // Atualiza a lista de itens offline na UI
                _itens.value = itensOffline
                Log.d("ItensViewModel", "Itens offline listados com sucesso.")
            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao listar itens offline: ${e.message}")
            }
        }
    }

    fun excluirItem(item: Item) {
        viewModelScope.launch {
            try {
                val isNetworkAvailable = ConnectionUtil.isNetworkAvailable(context)
                Log.d("ItensViewModel", "Verificando conexão: $isNetworkAvailable")

                if (!isNetworkAvailable) {
                    // Se estiver offline e o item não tem firestoreId, cria um
                    if (item.firestoreId.isNullOrEmpty()) {
                        item.firestoreId =
                            java.util.UUID.randomUUID().toString() // Gera um ID único
                        Log.d(
                            "ItensViewModel",
                            "Gerei um novo firestoreId para item offline: ${item.firestoreId}"
                        )
                    }
                    item.isSynced = false // Marca como não sincronizado
                    localRepository.excluir(item) // Exclui localmente
                    Log.d(
                        "ItensViewModel",
                        "Item excluído localmente como não sincronizado: ${item.descricao}"
                    )
                } else {
                    // Se estiver online, tenta excluir do Firestore
                    if (item.firestoreId.isNullOrEmpty()) {
                        Log.e(
                            "ItensViewModel",
                            "Erro ao excluir item: firestoreId está vazio ou nulo"
                        )
                        return@launch
                    }
                    remoteRepository.excluir(item) // Exclui do Firestore
                    Log.d("ItensViewModel", "Item excluído do Firestore: ${item.descricao}")

                    // Exclui também localmente após a exclusão no Firestore
                    localRepository.excluir(item)
                    Log.d(
                        "ItensViewModel",
                        "Item excluído localmente após sincronização: ${item.descricao}"
                    )
                }

                listarItensOffline()

            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao excluir item: ${e.message}", e)
            }
        }
    }

    fun atualizarItem(item: Item) {
        viewModelScope.launch {
            try {
                // Verifica a conexão com a internet
                val isNetworkAvailable = ConnectionUtil.isNetworkAvailable(context)
                Log.d("ItensViewModel", "Verificando conexão: $isNetworkAvailable")

                // Se estiver offline, gera um ID temporário para o Firestore, se necessário
                if (!isNetworkAvailable) {
                    // Garante que o firestoreId seja único, mas não substitui um ID já existente
                    if (item.firestoreId.isNullOrEmpty()) {
                        item.firestoreId =
                            java.util.UUID.randomUUID().toString() // Gera um ID único
                        Log.d(
                            "ItensViewModel",
                            "Gerando novo firestoreId para item offline: ${item.firestoreId}"
                        )
                    }
                    item.isSynced = false // Marca como não sincronizado

                    // Atualiza o item localmente
                    localRepository.atualizar(item)
                    Log.d(
                        "ItensViewModel",
                        "Item editado localmente como não sincronizado: ${item.descricao}"
                    )
                } else {
                    // Se estiver online, tenta atualizar no Firestore
                    remoteRepository.atualizar(item)
                    item.isSynced = true // Marca como sincronizado

                    Log.d("ItensViewModel", "Item atualizado no Firestore: ${item.descricao}")

                    // Atualiza o item localmente após a sincronização com o Firestore
                    localRepository.atualizar(item)
                    Log.d(
                        "ItensViewModel",
                        "Item atualizado localmente após sincronização: ${item.descricao}"
                    )
                }

                // Atualiza a lista de itens não sincronizados na UI
                listarItensOffline()

            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao editar item: ${e.message}", e)
            }
        }
    }
}