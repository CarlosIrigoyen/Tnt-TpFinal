package com.example.trabajofinal2024

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class RepositorioEncuestas(private val encuestaDAO: EncuestaDAO) {

    val allEncuestas: Flow<List<Encuesta>> = encuestaDAO.getEncuestas()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(encuesta:Encuesta){
        encuestaDAO.insertar(encuesta)
    }
}