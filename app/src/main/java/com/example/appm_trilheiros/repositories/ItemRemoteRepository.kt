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
                    item?.copy(firestoreId = document.id)  // Pega o ID do Firestore diretamente
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
            // Verifica se o item já tem um firestoreId antes de gravar no Firestore
            if (item.firestoreId.isEmpty()) {
                // Cria um novo documento no Firestore, o Firestore irá gerar um ID único automaticamente
                val documentRef = itemCollection.add(item).await()
                val documentId = documentRef.id  // ID gerado pelo Firestore

                // Atualiza o firestoreId e o status de sincronização
                val newItem = item.copy(firestoreId = documentId, isSynced = true)

                // Grava o item no banco local
                dao.gravar(newItem)

                // Exibe a mensagem com o UserID
                Log.d("ItemRemoteRepository", "Item cadastrado para o UserID: ${newItem.userId} - ${newItem.descricao}")
            } else {
                // Se já tiver firestoreId, apenas atualiza no Firestore
                itemCollection.document(item.firestoreId).set(item).await()

                // Atualiza o status de sincronização no banco local
                val newItem = item.copy(isSynced = true)
                dao.gravar(newItem)  // Atualiza localmente

                // Log de edição do item
                Log.d("ItemRemoteRepository", "Item editado: ${newItem.descricao} (Firestore ID: ${newItem.firestoreId})")
                Log.d("ItemRemoteRepository", "Item atualizado no Firestore: ${newItem.descricao}")
            }
        } catch (e: Exception) {
            Log.e("ItemRemoteRepository", "Erro ao gravar o item: ${e.message}")
        }
    }

    // Excluir o item do Firestore e localmente
    override suspend fun excluir(item: Item) {
        try {
            // Primeiro tenta excluir no Firestore
            if (item.firestoreId.isNotEmpty()) {
                itemCollection.document(item.firestoreId).delete().await()
                Log.d("ItemRemoteRepository", "Item excluído no Firestore.")
            }

            // Em qualquer caso, exclui localmente
            dao.excluir(item)
            Log.d("ItemRemoteRepository", "Item excluído localmente.")
        } catch (e: Exception) {
            // Se a exclusão falhar no Firestore, apenas atualiza localmente para exclusão pendente
            Log.e("ItemRemoteRepository", "Erro ao excluir o item: ${e.message}")
            val itemComFalha = item.copy(isSynced = false)  // Marca como não sincronizado
            dao.gravar(itemComFalha)  // Atualiza localmente para futura sincronização
        }
    }

    // Sincroniza os itens do Firestore com o banco local
    suspend fun sincronizarComLocal() {
        try {
            listarFlow().collect { itensRemotos ->
                itensRemotos.forEach { itemRemoto ->
                    // Verifica se o item já existe no banco local pelo firestoreId
                    val itemLocal = dao.buscarPorFirestoreId(itemRemoto.firestoreId)
                    if (itemLocal == null) {
                        // Se o item não existe no banco local, insira
                        dao.gravar(itemRemoto)
                        Log.d("ItemRemoteRepository", "Item sincronizado com sucesso: ${itemRemoto.descricao}")
                    } else {
                        // Se o item já existe no banco local, você pode atualizá-lo ou ignorá-lo
                        // Aqui, se o item foi alterado no Firestore, atualiza o banco local
                        if (itemRemoto.updatedAt > itemLocal.updatedAt) {
                            dao.gravar(itemRemoto)
                            Log.d("ItemRemoteRepository", "Item foi atualizado do Firestore: ${itemRemoto.descricao}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ItemRemoteRepository", "Erro ao sincronizar com o banco local: ${e.message}")
        }
    }
}
