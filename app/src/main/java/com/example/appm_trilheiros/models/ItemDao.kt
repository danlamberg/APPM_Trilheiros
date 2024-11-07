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

    // Busca um item pelo ID
    @Query("SELECT * FROM tab_itens WHERE id = :idx")
    suspend fun buscarPorId(idx: Long): Item?

    // Busca um item pelo firestoreId
    @Query("SELECT * FROM tab_itens WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun buscarPorFirestoreId(firestoreId: String): Item?

    // Insere ou atualiza um item
    @Upsert
    suspend fun gravar(item: Item)

    // Exclui um item
    @Delete
    suspend fun excluir(item: Item)

    // Busca um item pela descrição (para verificação de duplicação)
    @Query("SELECT * FROM tab_itens WHERE descricao = :descricao LIMIT 1")
    suspend fun buscarPorDescricao(descricao: String): Item?

    // Lista itens não sincronizados
    @Query("SELECT * FROM tab_itens WHERE isSynced = 0")
    suspend fun listarItensNaoSincronizados(): List<Item>

    // Lista itens do usuário com base no userId
    @Query("SELECT * FROM tab_itens WHERE userId = :userId")
    fun listarFlowPorUsuario(userId: String): Flow<List<Item>>
}
