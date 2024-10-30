package com.example.appm_trilheiros.dados

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tab_itens")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var descricao: String,
    var userId: String
)