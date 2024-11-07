package com.example.appm_trilheiros

import android.app.Application
import com.example.appm_trilheiros.dados.db.ItemDB
import com.example.appm_trilheiros.repositories.ItemLocalRepository
import com.example.appm_trilheiros.repositories.ItemRemoteRepository
import com.example.appm_trilheiros.viewmodels.ItensViewModel
import com.example.appm_trilheiros.viewmodels.ItensViewModelFactory
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule)
        }
    }

    private val appModule = module {
        single { ItemDB.abrirBanco(get()).getItemDao() }
        single { ItemLocalRepository(get()) }
        single { ItemRemoteRepository(get()) }

        // Remover a criação da ViewModelFactory
        viewModel { ItensViewModel(get(), get(), get()) }
    }
}