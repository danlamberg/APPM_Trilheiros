package com.example.appm_trilheiros.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.repositories.ItemLocalRepository
import com.example.appm_trilheiros.repositories.ItemRemoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ItensViewModel(
    private val localRepository: ItemLocalRepository,
    private val remoteRepository: ItemRemoteRepository
) : ViewModel() {

    private val _itens = MutableStateFlow<List<Item>>(emptyList())
    val itens: StateFlow<List<Item>> get() = _itens

    init {
        viewModelScope.launch {
            localRepository.listarFlow().collectLatest { lista ->
                _itens.value = lista
            }
        }
    }

    fun sincronizarDados() {
        viewModelScope.launch {
            try {
                // Primeiro, sincroniza os dados do Firebase para o banco local
                remoteRepository.listarFlow().collect { itensRemotos ->
                    itensRemotos.forEach { itemRemoto ->
                        // Gravar ou atualizar localmente
                        localRepository.gravar(itemRemoto)
                    }
                }
                // Sincronize os dados locais com o Firebase
                val itensLocais = localRepository.listarFlow().first()
                itensLocais.forEach { itemLocal ->
                    // Grava no Firebase se o item ainda não estiver lá
                    remoteRepository.gravar(itemLocal)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    suspend fun buscarItemPorId(itemId: Int): Item? {
        return localRepository.buscarPorId(itemId)
    }

    // Método para gravar o item localmente
    fun gravarItemLocal(item: Item) {
        viewModelScope.launch {
            try {
                Log.d("ItensViewModel", "Iniciando gravação do item localmente: ${item.id}")

                // Gravar no banco local
                localRepository.gravar(item)
                Log.d("ItensViewModel", "Item gravado localmente com sucesso: ${item.id}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ItensViewModel", "Erro ao gravar o item localmente: ${e.message}")
            }
        }
    }

    // Método para gravar o item no Firebase
    fun gravarItemFirebase(item: Item) {
        viewModelScope.launch {
            try {
                Log.d("ItensViewModel", "Iniciando gravação do item no Firebase: ${item.id}")

                // Gravar no Firebase
                remoteRepository.gravar(item)
                Log.d("ItensViewModel", "Item gravado com sucesso no Firebase: ${item.id}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ItensViewModel", "Erro ao gravar o item no Firebase: ${e.message}")
            }
        }
    }

    // Método para excluir um item
    fun excluirItem(item: Item) {
        viewModelScope.launch {
            try {
                localRepository.excluir(item) // Exclui do local
                remoteRepository.excluir(item) // Exclui do Firebase
                Log.d("ItensViewModel", "Item excluído com sucesso: ${item.id}")
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("ItensViewModel", "Erro ao excluir o item: ${e.message}")
            }
        }
    }
}