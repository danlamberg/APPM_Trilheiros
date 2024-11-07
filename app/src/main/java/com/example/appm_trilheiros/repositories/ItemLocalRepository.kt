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

    // Grava um item no banco local
    override suspend fun gravar(item: Item) {
        // Verifica se o item já existe no banco local pela descrição
        val existingItem = dao.buscarPorDescricao(item.descricao)
        if (existingItem != null) {
            Log.d("ItemLocalRepository", "Item já existe no banco local: ${item.descricao}")
            return // Se já existir, não insere
        }

        val novoItem = item.copy(isSynced = false) // Marca como não sincronizado inicialmente
        dao.gravar(novoItem) // Grava no banco local

        // Log para indicar que o item foi salvo no banco local
        Log.d("ItemLocalRepository", "Item salvo no banco local: ${novoItem.descricao}")
    }

    // Exclui um item do banco local
    override suspend fun excluir(item: Item) {
        dao.excluir(item) // Exclui do banco local
    }
}
