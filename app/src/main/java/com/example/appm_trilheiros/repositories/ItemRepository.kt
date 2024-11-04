package com.example.appm_trilheiros.repositories

import com.example.appm_trilheiros.models.Item
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    /**
     * Obtém uma lista de itens como um fluxo.
     */
    fun listarFlow(): Flow<List<Item>>

    /**
     * Busca um item pelo seu ID.
     * Retorna o item se encontrado, ou nulo se não for encontrado.
     */
    suspend fun buscarPorId(idx: Long): Item?

    /**
     * Grava um item no repositório.
     */
    suspend fun gravar(item: Item)

    /**
     * Exclui um item do repositório.
     */
    suspend fun excluir(item: Item)
}
