package com.example.appm_trilheiros.repositories

import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.models.ItemDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ItemRemoteRepository(
    private val dao: ItemDao
) : ItemRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val itemCollection = firestore.collection("itens")

    // Lista todos os itens do Firebase como Flow
    override fun listarFlow(): Flow<List<Item>> = callbackFlow {
        val listener = itemCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { document ->
                    val item = document.toObject(Item::class.java)
                    // Atualiza o firestoreId com o ID do documento do Firestore
                    item?.copy(firestoreId = document.id)
                }
                trySend(items).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    // Busca um item pelo ID no Firebase
    override suspend fun buscarPorId(idx: Long): Item { // Alterado para Long
        // Aqui você deve fazer a conversão do Long para String, pois o Firestore espera o ID como String
        return itemCollection.document(idx.toString()).get().await().toObject(Item::class.java)
            ?.copy(firestoreId = idx.toString()) // Atualiza firestoreId se necessário
            ?: throw Exception("Item não encontrado")
    }

    // Gravar o item no Firebase e localmente
    override suspend fun gravar(item: Item) {
        // Adiciona o item e espera o resultado
        val documentReference = itemCollection.add(item).await()
        // Atualiza o firestoreId do item com o ID do Firestore
        val newItem = item.copy(firestoreId = documentReference.id)
        dao.gravar(newItem) // Grava o item atualizado no banco local
    }

    // Excluir o item do Firebase e localmente
    override suspend fun excluir(item: Item) {
        itemCollection.document(item.firestoreId).delete().await() // Usa firestoreId para exclusão
        dao.excluir(item) // Exclui também do banco local
    }

    suspend fun sincronizarComLocal() {
        listarFlow().collect { itensRemotos ->
            itensRemotos.forEach { itemRemoto ->
                // Verifica se o item já existe localmente pelo firestoreId
                val itemLocal = dao.buscarPorFirestoreId(itemRemoto.firestoreId)
                if (itemLocal == null) {
                    // Se o item não existe no banco local, insere
                    dao.gravar(itemRemoto)
                } else {
                    // Se o item já existe, você pode optar por atualizar ou ignorar
                    // dao.gravar(itemRemoto) // Ou ignorar se não houver mudanças
                }
            }
        }
    }
}