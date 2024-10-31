package com.example.trabajofinal2024

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class RepositorioAlimentos(private val alimentoDAO:AlimentoDAO) {
    val allAlimentos: Flow<List<Alimento>> = alimentoDAO.getAlimentos()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(alimento:Alimento){
        alimentoDAO.insertar(alimento)
    }


}