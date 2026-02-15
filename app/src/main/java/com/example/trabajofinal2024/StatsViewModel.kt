package com.example.trabajofinal2024

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData

class StatsViewModel(private val repositorioAlimentos: RepositorioAlimentos) : ViewModel() {

    /**
     * Devuelve LiveData<StatsAverages?> observables por la UI.
     * Llamar con el UID del usuario actual.
     */
    fun getAveragesForUser(uid: String) = repositorioAlimentos.getStatsAveragesForUser(uid).asLiveData()

    fun getDailyTotals(uid: String) = repositorioAlimentos.getDailyTotalsByUser(uid)
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
