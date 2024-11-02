package com.example.appm_trilheiros.repositories

import com.example.appm_trilheiros.models.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {

    // Método para listar itens com Flow
    fun listarFlow(): Flow<List<Item>>

    // Método para buscar um item por ID
    suspend fun buscarPorId(idx: Int): Item

    // Método para gravar (inserir ou atualizar) um item
    suspend fun gravar(item: Item)

    // Método para excluir um item
    suspend fun excluir(item: Item)
}
