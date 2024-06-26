package com.example.trabajofinal2024

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AlimentoApp: Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope)}
    val repositorio by lazy { RepositorioAlimentos(database.alimentoDAO())}
}