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
                // Log para depuração, verificando se a lista está sendo atualizada
                Log.d("ItensViewModel", "Itens atualizados: $lista")
                // Atualiza o estado com a lista de itens
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
                            Log.d("ItensViewModel", "Item excluído localmente (não foi sincronizado): ${item.descricao}")
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
                // Verifica se o item já existe no banco local
                val existingItem = localRepository.buscarPorFirestoreId(item.firestoreId)
                    ?: localRepository.buscarPorDescricao(item.descricao)

                Log.d("ItensViewModel", "Item encontrado localmente: $existingItem")

                // Se o item já existe, não faz nada
                if (existingItem != null) {
                    Log.d("ItensViewModel", "Item já existe localmente, não será gravado novamente.")
                    return@launch
                }

                // Verifica a conexão com a internet
                val isNetworkAvailable = ConnectionUtil.isNetworkAvailable(context)
                Log.d("ItensViewModel", "Verificando conexão: $isNetworkAvailable")

                // Se o aparelho estiver offline, não tentamos gravar no Firestore imediatamente
                if (isNetworkAvailable) {
                    Log.d("ItensViewModel", "Conexão com a internet disponível.")

                    // Se estiver online, grava no Firestore
                    remoteRepository.gravar(item)
                    item.isSynced = true // Marca como sincronizado
                    Log.d("ItensViewModel", "Gravando item no Firestore: ${item.descricao}")

                    // Atualiza o item local
                    localRepository.atualizar(item)
                    Log.d("ItensViewModel", "Item atualizado localmente após sincronização: ${item.descricao}")

                } else {
                    // Marca o item como não sincronizado se estiver offline
                    item.isSynced = false
                    localRepository.gravar(item) // Grava no banco local
                    Log.d("ItensViewModel", "Item gravado localmente como não sincronizado: ${item.descricao}")
                }

                // Atualiza a lista de itens não sincronizados na UI
                listarItensOffline()

            } catch (e: Exception) {
                // Loga a exceção para depuração
                Log.e("ItensViewModel", "Erro ao gravar item: ${e.message}", e)
            }
        }
    }


    fun excluirItem(item: Item) {
        viewModelScope.launch {
            try {
                // Verifica a conexão com a rede antes de tentar excluir no Firestore
                if (ConnectionUtil.isNetworkAvailable(context)) {
                    // Exclui o item remotamente no Firestore
                    remoteRepository.excluir(item)
                    Log.d("ItensViewModel", "Item excluído do Firestore com sucesso: ${item.descricao}")
                } else {

                    Log.d("ItensViewModel", "Sem conexão. Não foi possível excluir do Firestore.")
                }

                // Exclui o item localmente no banco de dados
                localRepository.excluir(item) // Exclui no banco local
                Log.d("ItensViewModel", "Item excluído localmente com sucesso: ${item.descricao}")

                // Atualiza a lista de itens offline após a exclusão
                listarItensOffline()

            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao excluir item: ${e.message}")
            }
        }
    }

    fun excluirItemOffline(item: Item) {
        viewModelScope.launch {
            try {
                item.isMarkedForDeletion = true // Marca o item para exclusão
                localRepository.atualizar(item) // Atualiza o status no banco local
                Log.d("ItensViewModel", "Item marcado para exclusão localmente: ${item.descricao}")
                listarItensOffline()
            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao marcar item para exclusão: ${e.message}")
            }
        }
    }

    fun atualizarItem(item: Item) {
        viewModelScope.launch {
            try {
                // Atualiza o item remotamente no Firestore
                remoteRepository.atualizar(item)
                Log.d("ItensViewModel", "Item atualizado remotamente: ${item.descricao}")

                // Atualiza o item localmente no banco de dados Room
                localRepository.atualizar(item)  // Aqui, você precisa garantir que o método "atualizar" exista no ItemLocalRepository

                // Atualiza a lista de itens offline após a atualização
                listarItensOffline()

            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao atualizar item: ${e.message}")
            }
        }
    }

    // Atualizada para listar apenas os itens que não foram sincronizados
    private fun listarItensOffline() {
        viewModelScope.launch {
            // Lista os itens que não foram sincronizados
            val itensOffline = localRepository.listarItensNaoSincronizados()

            // Atualiza a lista de itens offline
            _itens.value = itensOffline
        }
    }
}
