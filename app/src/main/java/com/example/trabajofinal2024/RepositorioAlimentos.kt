package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
import com.example.trabajofinal2024.AlimentoDAO.DailySurveyStats
import kotlinx.coroutines.flow.Flow

class RepositorioAlimentos(private val alimentoDAO: AlimentoDAO) {

    val allAlimentos: Flow<List<Alimento>> = alimentoDAO.getAlimentos()

    suspend fun insert(alimento: Alimento):Long {
        return alimentoDAO.insertar(alimento)
    }

    suspend fun borrarTodos() {
        alimentoDAO.borrarTodos()
    }

    suspend fun getAlimento(encuestaId: Int, nombre: String) =
        alimentoDAO.getAlimento(encuestaId, nombre)

    suspend fun update(alimento: Alimento) =
        alimentoDAO.update(alimento)


    suspend fun obtenerAlimentosPorEncuesta(encuestaId: Int): List<Alimento> {
        return alimentoDAO.obtenerAlimentosPorEncuesta(encuestaId)
    }

    suspend fun borrarPorEncuesta(encuestaId: Int) {
        alimentoDAO.borrarPorEncuesta(encuestaId)
    }

    // Nuevo: expone la query de estadísticas
    fun getStatsAveragesForUser(uid: String) = alimentoDAO.getAveragesPerCompletedEncuestaByUser(uid)

    fun getDailyTotalsByUser(uid: String) = alimentoDAO.getDailyTotalsByUser(uid)

}
