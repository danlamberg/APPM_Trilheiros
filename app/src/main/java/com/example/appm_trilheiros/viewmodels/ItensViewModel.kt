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
                    // Garantir que itens duplicados não sejam gravados novamente
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
                // Verificar se o item já existe localmente
                val existeLocalmente = localRepository.buscarPorId(item.id)
                if (existeLocalmente == null) {
                    // Tente gravar localmente
                    localRepository.gravar(item.copy(updatedAt = System.currentTimeMillis())) // Atualiza o timestamp
                    Log.d("ItensViewModel", "Item gravado localmente: ${item.descricao}")

                    // Verificar conexão e gravar no repositório remoto, se online
                    if (ConnectivityUtils.isOnline(context)) {
                        // Pega o ID do item que foi salvo localmente
                        val idGerado = localRepository.buscarPorId(item.id)
                        idGerado?.let { // Usando o operador seguro
                            remoteRepository.gravar(item.copy(firestoreId = it.id.toString()))
                            Log.d("ItensViewModel", "Item gravado remotamente: ${item.descricao}")
                        } ?: Log.d("ItensViewModel", "ID do item gerado não encontrado.")
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
