package com.example.trabajofinal2024

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class RepositorioAlimentos(private val alimentoDAO: AlimentoDAO) {
    val allAlimentos: Flow<List<Alimento>> = alimentoDAO.getAlimentos()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(alimento: Alimento) {
        alimentoDAO.insertar(alimento)
    }

    // Nuevos helpers
    @WorkerThread
    suspend fun borrarPorEncuesta(encuestaId: Int) {
        alimentoDAO.borrarPorEncuesta(encuestaId)
    }

    @WorkerThread
    suspend fun obtenerPorEncuesta(encuestaId: Int): List<Alimento> {
        return alimentoDAO.obtenerAlimentosPorEncuesta(encuestaId)
    }
}
