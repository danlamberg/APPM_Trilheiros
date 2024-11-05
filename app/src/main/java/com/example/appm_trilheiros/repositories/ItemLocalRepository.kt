package com.example.appm_trilheiros.repositories

import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.models.ItemDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ItemLocalRepository(
    private val dao: ItemDao
) : ItemRepository {

    override fun listarFlow(): Flow<List<Item>> {
        return dao.listarFlow()
    }

    // Método para listar itens não sincronizados
    suspend fun listarItensNaoSincronizados(): List<Item> {
        return dao.listarFlow().first().filter { !it.isSynced } // Pega o primeiro valor do Flow e filtra
    }

    // Retorna Item? para evitar exceções
    override suspend fun buscarPorId(idx: Long): Item? {
        return dao.buscarPorId(idx)
    }

    // Novo método para buscar item pelo firestoreId
    suspend fun buscarPorFirestoreId(firestoreId: String): Item? {
        return dao.buscarPorFirestoreId(firestoreId)
    }

    override suspend fun gravar(item: Item) {
        val novoItem = item.copy(isSynced = false)
        dao.gravar(novoItem) // Grava no banco local
    }

    override suspend fun excluir(item: Item) {
        dao.excluir(item) // Exclui apenas do banco local
    }
}
