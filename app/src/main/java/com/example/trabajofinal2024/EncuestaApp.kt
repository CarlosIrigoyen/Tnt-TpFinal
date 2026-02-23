package com.example.trabajofinal2024

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class EncuestaApp: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope)}

    val encuestaDatabase by lazy { AppDatabase.getDatabase(this, applicationScope)}
    val encuestaRepositorio by lazy {
        RepositorioEncuestas(
            database.encuestaDAO(),
            databaseAl.alimentoDAO(),
        )
    }
    //Alimentos

    val databaseAl by lazy { AppDatabase.getDatabase(this, applicationScope)}
    val alimentoRepositorio by lazy { RepositorioAlimentos(databaseAl.alimentoDAO())}
}