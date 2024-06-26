package com.example.trabajofinal2024

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface  AlimentoDAO {
    @Query("SELECT * from alimentos ORDER BY alimentoid ASC")
    fun getAlimentos(): Flow<List<Alimento>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(alimento:Alimento)

    @Query("DELETE FROM alimentos")
    suspend fun borrarTodos()

    @Query("SELECT * FROM alimentos WHERE encuesta = :encuestaId")
    suspend fun obtenerAlimentosPorEncuesta(encuestaId: Int): List<Alimento>


}