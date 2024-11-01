package com.example.appm_trilheiros.repositories

import com.example.appm_trilheiros.models.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {

    fun listarFlow(): Flow<List<Item>>
    suspend fun buscarPorId(idx: Int): Item
    suspend fun gravar(item: Item)
    suspend fun excluir(item: Item)
}
