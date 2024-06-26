package com.example.trabajofinal2024

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class EncuestaApp: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope)}
    val repositorio by lazy { RepositorioEncuestas(database.encuestaDAO())}
}