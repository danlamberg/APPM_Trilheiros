package com.example.appm_trilheiros.dados

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM tab_itens")
    fun listarFlow(): Flow<List<Item>>

    @Query("SELECT * FROM tab_itens WHERE id = :idx")
    suspend fun buscarPorId(idx: Int): Item

    @Query("SELECT * FROM tab_itens WHERE userId = :userId")
    fun listarFlowPorUsuario(userId: String): Flow<List<Item>>

    @Upsert
    suspend fun gravar(item: Item)

    @Delete
    suspend fun excluir(item: Item)
}
