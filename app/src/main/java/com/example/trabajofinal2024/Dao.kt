package com.example.trabajofinal2024

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface  EncuestaDAO {
    @Query("SELECT * from encuestas ORDER BY encuestaId ASC")
    fun getEncuestas(): Flow<List<Encuesta>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(encuesta:Encuesta)

    @Query("DELETE FROM encuestas")
    suspend fun borrarTodos()

}