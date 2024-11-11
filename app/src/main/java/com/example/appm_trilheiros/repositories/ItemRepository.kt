package com.example.appm_trilheiros.repositories

import com.example.appm_trilheiros.models.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun listarFlow(): Flow<List<Item>> // Lista os itens como Flow
    suspend fun buscarPorId(idx: Long): Item? // Busca item pelo ID
    suspend fun gravar(item: Item) // Grava item
    suspend fun excluir(item: Item) // Exclui item
    suspend fun atualizar(item: Item) // Exclui item
}
