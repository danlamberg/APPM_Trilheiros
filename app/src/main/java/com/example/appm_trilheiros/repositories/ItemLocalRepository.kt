package com.example.appm_trilheiros.repositories

import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.models.ItemDao
import kotlinx.coroutines.flow.Flow

class ItemLocalRepository(
    private val dao: ItemDao,
    private val remoteRepository: ItemRemoteRepository
) : ItemRepository {

    override fun listarFlow(): Flow<List<Item>> {
        return dao.listarFlow()
    }

    override suspend fun buscarPorId(idx: Int): Item {
        return dao.buscarPorId(idx)
    }

    // Gravar o item localmente
    override suspend fun gravar(item: Item) {
        dao.gravar(item) // Grava no banco local
    }

    // Excluir o item
    override suspend fun excluir(item: Item) {
        dao.excluir(item) // Exclui do banco local
    }

    suspend fun sincronizarComFirebase() {
        remoteRepository.listarFlow().collect { itensRemotos ->
            itensRemotos.forEach { item ->
                // Verificar se o item já está no banco local
                val itemLocal = dao.buscarPorId(item.id)
                if (itemLocal == null) {
                    // Se não estiver, gravar no banco local
                    dao.gravar(item)
                }
            }
        }
    }
}