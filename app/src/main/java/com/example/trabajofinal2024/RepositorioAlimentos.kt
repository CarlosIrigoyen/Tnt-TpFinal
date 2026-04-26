package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class RepositorioAlimentos(private val alimentoDao: AlimentoDAO) {

    val allAlimentos: Flow<List<Alimento>> = alimentoDao.getAlimentos()

    suspend fun insert(alimento: Alimento): Long {
        return alimentoDao.insertar(alimento)
    }

    suspend fun getAlimento(encuestaId: Int, nombre: String): Alimento? {
        return alimentoDao.getAlimento(encuestaId, nombre)
    }

    suspend fun update(alimento: Alimento) {
        alimentoDao.update(alimento)
    }

    suspend fun deleteAll() {
        alimentoDao.borrarTodos()
    }

    suspend fun getAlimentosPorEncuesta(encuestaId: Int): List<Alimento> {
        return alimentoDao.obtenerAlimentosPorEncuesta(encuestaId)
    }

    suspend fun borrarPorEncuesta(encuestaId: Int) {
        alimentoDao.borrarPorEncuesta(encuestaId)
    }

    // Estadísticas – funciones originales
    fun getStatsAveragesForUser(uid: String): Flow<StatsAverages?> {
        return alimentoDao.getAveragesPerCompletedEncuestaByUser(uid)
    }

    fun getDailyTotalsByUser(uid: String): LiveData<List<AlimentoDAO.DailySurveyStats>> {
        return alimentoDao.getDailyTotalsByUser(uid)
    }
}