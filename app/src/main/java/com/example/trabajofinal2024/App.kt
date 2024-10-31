package com.example.trabajofinal2024

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App: Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    //Encuestas
    val encuestaDatabase by lazy { AppDatabase.getDatabase(this, applicationScope)}
    val encuestaRepositorio by lazy { RepositorioEncuestas(encuestaDatabase.encuestaDAO())}

    //Alimentos

    val alimentoDatabase by lazy { AppDatabase.getDatabase(this, applicationScope)}
    val alimentoRepositorio by lazy { RepositorioAlimentos(alimentoDatabase.alimentoDAO())}
}