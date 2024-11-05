package com.example.appm_trilheiros.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    // Retorna todos os itens como um fluxo
    @Query("SELECT * FROM tab_itens")
    fun listarFlow(): Flow<List<Item>>

    // Retorna um item específico pelo ID, que pode ser nulo se não encontrado
    @Query("SELECT * FROM tab_itens WHERE id = :idx")
    suspend fun buscarPorId(idx: Long): Item? // Retorna Item? para lidar com itens não encontrados

    // Retorna itens associados a um usuário específico
    @Query("SELECT * FROM tab_itens WHERE userId = :userId")
    fun listarFlowPorUsuario(userId: String): Flow<List<Item>>

    // Insere ou atualiza um item
    @Upsert
    suspend fun gravar(item: Item)

    // Exclui um item
    @Delete
    suspend fun excluir(item: Item)

    // Novo método para buscar um item pelo firestoreId
    @Query("SELECT * FROM tab_itens WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun buscarPorFirestoreId(firestoreId: String): Item?
}
