package com.example.appm_trilheiros.repositories

import android.util.Log
import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.models.ItemDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ItemLocalRepository(
    private val dao: ItemDao
) : ItemRepository {

    override fun listarFlow(): Flow<List<Item>> {
        return dao.listarFlow()
    }

    // Retorna os itens que não foram sincronizados
    suspend fun listarItensNaoSincronizados(): List<Item> {
        return dao.listarFlow().first().filter { !it.isSynced }
    }

    // Busca um item pelo ID
    override suspend fun buscarPorId(idx: Long): Item? {
        return dao.buscarPorId(idx)
    }

    // Busca um item pelo firestoreId
    suspend fun buscarPorFirestoreId(firestoreId: String): Item? {
        return dao.buscarPorFirestoreId(firestoreId)
    }

    // Função para buscar um item pelo campo descricao no banco local
    suspend fun buscarPorDescricao(descricao: String): Item? {
        return dao.buscarPorDescricao(descricao)
    }

    // Grava um item no banco local
    override suspend fun gravar(item: Item) {
        dao.gravar(item) // Exclui do banco local
    }

    // Exclui um item do banco local
    override suspend fun excluir(item: Item) {
        dao.excluir(item) // Exclui do banco local
    }
}
