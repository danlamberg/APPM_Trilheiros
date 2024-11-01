package com.example.appm_trilheiros.repositories

import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.models.ItemDao
import kotlinx.coroutines.flow.Flow

class ItemLocalRepository(
    private val dao: ItemDao
) : ItemRepository {

    // Lista todos os itens como Flow
    override fun listarFlow(): Flow<List<Item>> {
        return dao.listarFlow()
    }

    // Busca um item pelo ID
    override suspend fun buscarPorId(idx: Int): Item {
        return dao.buscarPorId(idx)
    }

    // Grava (insere ou atualiza) um item no banco local
    override suspend fun gravar(item: Item) {
        dao.gravar(item)
    }

    // Exclui um item do banco local
    override suspend fun excluir(item: Item) {
        dao.excluir(item)
    }
}
