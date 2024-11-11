package com.example.appm_trilheiros.repositories

import android.util.Log
import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.models.ItemDao
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class ItemRemoteRepository(
    private val dao: ItemDao
) : ItemRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val itemCollection = firestore.collection("itens")

    // Lista todos os itens do Firebase como Flow
    override fun listarFlow() = callbackFlow {
        val listener = itemCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { document ->
                    val item = document.toObject(Item::class.java)
                    item?.copy(firestoreId = document.id)
                }
                trySend(items).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    // Busca um item pelo ID no Firebase
    override suspend fun buscarPorId(idx: Long): Item {
        return itemCollection.document(idx.toString()).get().await()
            .toObject(Item::class.java)
            ?.copy(firestoreId = idx.toString())
            ?: throw Exception("Item não encontrado")
    }

    // Grava um item no Firestore
    override suspend fun gravar(item: Item) {
        try {
            if (item.firestoreId.isEmpty()) {
                val timestamp = System.currentTimeMillis()
                val documentId =
                    "${item.userId}_${item.descricao.hashCode()}_$timestamp"

                itemCollection.document(documentId).set(item).await()
                val newItem = item.copy(firestoreId = documentId, isSynced = true)
                dao.gravar(newItem)
                Log.d("ItemRemoteRepository", "Item cadastrado para o UserID: ${newItem.userId} - ${newItem.descricao}")
            } else {
                itemCollection.document(item.firestoreId).set(item).await()
                val newItem = item.copy(isSynced = true)
                dao.gravar(newItem)
                Log.d("ItemRemoteRepository", "Item editado: ${newItem.descricao}")
            }
        } catch (e: Exception) {
            Log.e("ItemRemoteRepository", "Erro ao gravar o item: ${e.message}")
        }
    }

    override suspend fun excluir(item: Item) {
        try {
            // Exclui o item do Firestore
            if (item.firestoreId.isNotEmpty()) {
                itemCollection.document(item.firestoreId).delete().await()
                Log.d("ItemRemoteRepository", "Item excluído no Firestore.")
            }

            // Exclui o item localmente
            dao.excluir(item)
            Log.d("ItemRemoteRepository", "Item excluído localmente.")
        } catch (e: Exception) {
            Log.e("ItemRemoteRepository", "Erro ao excluir o item: ${e.message}")
            val itemComFalha = item.copy(isSynced = false)
            dao.gravar(itemComFalha)
        }
    }

    // Implementando o método de atualizar
    override suspend fun atualizar(item: Item) {
        try {
            // Atualiza o item no Firestore
            if (item.firestoreId.isNotEmpty()) {
                itemCollection.document(item.firestoreId).set(item).await()
                Log.d("ItemRemoteRepository", "Item atualizado no Firestore: ${item.descricao}")
            }

            // Atualiza o status de sincronização no banco local
            val updatedItem = item.copy(isSynced = true)
            dao.gravar(updatedItem) // Atualiza localmente
            Log.d("ItemRemoteRepository", "Item atualizado localmente: ${item.descricao}")

        } catch (e: Exception) {
            Log.e("ItemRemoteRepository", "Erro ao atualizar o item: ${e.message}")
        }
    }
}
