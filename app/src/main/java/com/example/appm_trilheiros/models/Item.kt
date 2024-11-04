package com.example.appm_trilheiros.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tab_itens")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Tipo Long para o ID gerado automaticamente
    var descricao: String = "", // Adicione um valor padrão
    var userId: String = "", // Adicione um valor padrão
    var updatedAt: Long = System.currentTimeMillis(), // Tipo Long para timestamp
    var firestoreId: String = "" // Tipo String para o Firestore ID
) {
    // Construtor padrão vazio
    constructor() : this(0, "", "", System.currentTimeMillis(), "")
}
