package com.example.appm_trilheiros.repositories

import com.example.appm_trilheiros.models.Item
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ItemRemoteRepository : ItemRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val itemCollection = firestore.collection("itens")

    override fun listarFlow(): Flow<List<Item>> = callbackFlow {
        val listener = itemCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull {
                    it.toObject(Item::class.java)
                }
                trySend(items).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun buscarPorId(idx: Int): Item {
        return itemCollection.document(idx.toString()).get().await().toObject(Item::class.java)
            ?: throw Exception("Item não encontrado")
    }

    // Gravar o item no Firebase
    override suspend fun gravar(item: Item) {
        itemCollection.document(item.id.toString()).set(item).await()
    }

    // Excluir o item do Firebase
    override suspend fun excluir(item: Item) {
        itemCollection.document(item.id.toString()).delete().await()
    }
}
