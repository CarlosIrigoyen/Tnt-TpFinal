package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData

class StatsViewModel(private val repositorioAlimentos: RepositorioAlimentos) : ViewModel() {

    /**
     * Devuelve LiveData<StatsAverages?> observables por la UI.
     * Llamar con el UID del usuario actual.
     */
    fun getAveragesForUser(uid: String) = repositorioAlimentos.getStatsAveragesForUser(uid).asLiveData()

    fun getAveragesForUserFirebase(uid: String): LiveData<StatsAverages?> = liveData {
        emit(repositorioAlimentos.getStatsAveragesForUserFirebase(uid))
    }

    fun getDailyTotals(uid: String) = repositorioAlimentos.getDailyTotalsByUser(uid)

    fun getDailyTotalsByUserFirebase(uid: String): LiveData<List<AlimentoDAO.DailySurveyStats>> = liveData {
        emit(repositorioAlimentos.getDailyTotalsByUserFirebase(uid))
    }

    class Factory(private val repositorioAlimentos: RepositorioAlimentos) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StatsViewModel(repositorioAlimentos) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
