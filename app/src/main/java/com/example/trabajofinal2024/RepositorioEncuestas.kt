package com.example.trabajofinal2024

import androidx.annotation.WorkerThread
import androidx.room.Dao
import kotlinx.coroutines.flow.Flow

class RepositorioEncuestas(private val encuestaDAO: EncuestaDAO) {

    val allEncuestas: Flow<List<Encuesta>> = encuestaDAO.getEncuestas()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(encuesta:Encuesta): Long{
        return encuestaDAO.insertar(encuesta)
    }

    suspend fun update(encuesta: Encuesta) {
        encuestaDAO.update(encuesta)
    }

    fun getEncuestaById(id: Int): Flow<Encuesta> {
        return encuestaDAO.getEncuestaById(id)
    }


}