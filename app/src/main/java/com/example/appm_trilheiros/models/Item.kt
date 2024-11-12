package com.example.appm_trilheiros.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tab_itens")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var descricao: String = "",
    var userId: String = "",
    var updatedAt: Long = System.currentTimeMillis(),
    var firestoreId: String = "",
    var isSynced: Boolean = false,
    var isMarkedForDeletion: Boolean = false // Novo campo para marcar itens para exclusão
)
 {
    // Construtor padrão vazio
    constructor() : this(0, "", "", System.currentTimeMillis(), "", false)
}
