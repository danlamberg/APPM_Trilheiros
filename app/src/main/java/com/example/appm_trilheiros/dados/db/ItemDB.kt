package com.example.appm_trilheiros.dados.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.appm_trilheiros.models.Item
import com.example.appm_trilheiros.models.ItemDao

@Database(entities = [Item::class], version = 1)
abstract class ItemDB : RoomDatabase() {

    abstract fun getItemDao(): ItemDao

    companion object {
        @Volatile
        private var INSTANCE: ItemDB? = null

        fun abrirBanco(context: Context): ItemDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ItemDB::class.java,
                    "arquivo.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}