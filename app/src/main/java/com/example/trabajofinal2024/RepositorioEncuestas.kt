package com.example.trabajofinal2024

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class RepositorioEncuestas(private val encuestaDAO: EncuestaDAO) {

    val allEncuestas: Flow<List<Encuesta>> = encuestaDAO.getEncuestas()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(encuesta: Encuesta): Long {
        return encuestaDAO.insertar(encuesta)
    }

    @WorkerThread
    suspend fun update(encuesta: Encuesta) {
        encuestaDAO.update(encuesta)
    }

    fun getEncuestas() = encuestaDAO.getEncuestas()

    fun getEncuestaById(id: Int): Flow<Encuesta> {
        return encuestaDAO.getEncuestaById(id)
    }

    fun getEncuestasPorUsuario(uid: String): Flow<List<Encuesta>> = encuestaDAO.getEncuestasPorUsuario(uid)

    fun getPendientesPorUsuario(uid: String): Flow<List<Encuesta>> = encuestaDAO.getPendientesPorUsuario(uid)

    @WorkerThread
    suspend fun updateProgress(encuestaId: Int, index: Int) {
        encuestaDAO.updateProgress(encuestaId, index, System.currentTimeMillis())
    }

    @WorkerThread
    suspend fun markCompleted(encuestaId: Int, index: Int) {
        encuestaDAO.markCompleted(encuestaId, index, System.currentTimeMillis())
    }

    @WorkerThread
    suspend fun abandonEncuesta(encuestaId: Int) {
        encuestaDAO.abandonEncuesta(encuestaId, System.currentTimeMillis())
    }

    // Nueva función para reanudar
    @WorkerThread
    suspend fun reanudarEncuesta(encuestaId: Int) {
        encuestaDAO.reanudarEncuesta(encuestaId, System.currentTimeMillis())
    }
}