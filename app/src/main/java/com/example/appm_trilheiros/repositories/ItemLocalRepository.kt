package com.example.appm_trilheiros.repositories

import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.models.ItemDao
import kotlinx.coroutines.flow.Flow

class ItemLocalRepository(
    private val dao: ItemDao
) : ItemRepository {

    override fun listarFlow(): Flow<List<Item>> {
        return dao.listarFlow()
    }

    // Retornar Item? para evitar exceções
    override suspend fun buscarPorId(idx: Long): Item? {
        return dao.buscarPorId(idx) // Certifique-se de que esse método no ItemDao retorna Item? (ou seja, pode retornar null)
    }

    override suspend fun gravar(item: Item) {
        dao.gravar(item) // Grava apenas no banco local
    }

    override suspend fun excluir(item: Item) {
        dao.excluir(item) // Exclui apenas do banco local
    }
}
