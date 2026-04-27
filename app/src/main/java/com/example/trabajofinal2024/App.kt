package com.example.trabajofinal2024

import android.app.Application
import com.example.trabajofinal2024.AppDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    // Base de datos única
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    // Repositorio de encuestas
    val encuestaRepositorio by lazy {
        RepositorioEncuestas(
            database.encuestaDAO(),
            database.alimentoDAO()
        )
    }

    // Repositorio de alimentos
    val alimentoRepositorio by lazy {
        RepositorioAlimentos(database.alimentoDAO())
    }

    // Repositorio de turnos
    val turnoRepository by lazy {
        TurnoRepository(
            turnoDao = database.turnoDao(),
            firestore = FirebaseFirestore.getInstance()
        )
    }
}