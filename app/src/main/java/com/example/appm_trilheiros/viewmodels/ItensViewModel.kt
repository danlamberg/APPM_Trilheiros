package com.example.appm_trilheiros.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.repositories.ItemLocalRepository
import com.example.appm_trilheiros.repositories.ItemRemoteRepository
import com.example.appm_trilheiros.utils.ConnectivityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class ItensViewModel(
    private val localRepository: ItemLocalRepository,
    private val remoteRepository: ItemRemoteRepository,
    private val context: Context
) : ViewModel() {

    private val _itens = MutableStateFlow<List<Item>>(emptyList())
    val itens: StateFlow<List<Item>> get() = _itens

    init {
        // Colete os itens do repositório local e observe para mudanças
        viewModelScope.launch {
            localRepository.listarFlow().collectLatest { lista ->
                _itens.value = lista.distinctBy { it.id }  // Remove duplicados pela chave 'id'
            }
        }

        // Sincronize os dados no início
        sincronizarDados()
    }

    // Sincronização de dados entre local e remoto
    fun sincronizarDados() {
        if (ConnectivityUtils.isOnline(context)) {
            viewModelScope.launch {
                try {
                    // Sincronize o repositório remoto com o banco local, evitando duplicações
                    remoteRepository.sincronizarComLocal()
                } catch (e: Exception) {
                    Log.e("ItensViewModel", "Erro ao sincronizar dados: ${e.message}")
                }
            }
        } else {
            Log.d("ItensViewModel", "Conexão offline, sincronização não realizada.")
        }
    }

    fun gravarItem(item: Item) {
        viewModelScope.launch {
            try {
                // Verificar se o item já existe localmente pelo 'firestoreId' para evitar duplicações
                val existeLocalmente = localRepository.buscarPorFirestoreId(item.firestoreId)

                if (existeLocalmente == null) {
                    // Atualiza o timestamp e gera um novo Firestore ID se for um novo item
                    val novoItem = if (item.firestoreId.isEmpty()) {
                        item.copy(
                            updatedAt = System.currentTimeMillis(),
                            firestoreId = UUID.randomUUID().toString()  // Gerar um novo Firestore ID
                        )
                    } else {
                        item.copy(updatedAt = System.currentTimeMillis())
                    }

                    // Gravar o item localmente
                    localRepository.gravar(novoItem)
                    Log.d("ItensViewModel", "Item gravado localmente: ${novoItem.descricao}")

                    // Se estiver online, gravar no Firestore
                    if (ConnectivityUtils.isOnline(context)) {
                        remoteRepository.gravar(novoItem)  // Gravar no Firestore
                        Log.d("ItensViewModel", "Item gravado remotamente: ${novoItem.descricao} com ID: ${novoItem.firestoreId}")
                    } else {
                        Log.d("ItensViewModel", "Item gravado localmente e será sincronizado posteriormente.")
                    }
                } else {
                    Log.d("ItensViewModel", "Item já existe localmente: ${item.descricao}")
                }
            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao gravar item: ${e.message}")
            }
        }
    }


    // Exclui um item
    fun excluirItem(item: Item) {
        viewModelScope.launch {
            try {
                localRepository.excluir(item)

                if (ConnectivityUtils.isOnline(context)) {
                    remoteRepository.excluir(item)
                } else {
                    Log.d("ItensViewModel", "Item excluído localmente e será sincronizado posteriormente.")
                }
            } catch (e: Exception) {
                Log.e("ItensViewModel", "Erro ao excluir item: ${e.message}")
            }
        }
    }
}
