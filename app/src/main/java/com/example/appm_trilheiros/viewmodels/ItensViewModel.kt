package com.example.appm_trilheiros.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.repositories.ItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItensViewModel(
    private val repository: ItemRepository
) : ViewModel() {

    // MutableStateFlow para armazenar a lista de itens
    private val _itens = MutableStateFlow<List<Item>>(emptyList())
    val itens: StateFlow<List<Item>> get() = _itens

    // Coletor para atualizar a lista de itens
    init {
        viewModelScope.launch {
            repository.listarFlow().collectLatest { lista ->
                _itens.value = lista
            }
        }
    }

    // Busca um item pelo ID
    suspend fun buscarItemPorId(itemId: Int): Item? {
        return withContext(Dispatchers.IO) {
            repository.buscarPorId(itemId)
        }
    }

    // Grava um item
    fun gravarItem(item: Item) {
        viewModelScope.launch {
            repository.gravar(item)
        }
    }

    // Exclui um item
    fun excluirItem(item: Item) {
        viewModelScope.launch {
            repository.excluir(item)
        }
    }
}
