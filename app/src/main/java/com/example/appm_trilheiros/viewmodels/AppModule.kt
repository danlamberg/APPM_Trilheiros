package com.example.appm_trilheiros.viewmodels

import android.content.Context
import com.example.appm_trilheiros.dados.db.ItemDB
import com.example.appm_trilheiros.repositories.ItemLocalRepository
import com.example.appm_trilheiros.repositories.ItemRemoteRepository
import com.example.appm_trilheiros.repositories.ItemRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Fornece a instância do banco de dados Room
    single { ItemDB.abrirBanco(get()) }

    // Fornece o DAO a partir do banco de dados
    single { get<ItemDB>().getItemDao() }

    // Fornece o repositório local
    single<ItemRepository> { ItemLocalRepository(get()) }

    // Fornece o repositório remoto
    single { ItemRemoteRepository(get()) }

    // Fornece a instância da ItensViewModel com injeção dos repositórios
    viewModel { ItensViewModel(get(), get(), get()) } // Aqui estão as dependências
}
