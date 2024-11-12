package com.example.appm_trilheiros

import android.app.Application
import com.example.appm_trilheiros.viewmodels.appModule
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule)
        }
    }
}
