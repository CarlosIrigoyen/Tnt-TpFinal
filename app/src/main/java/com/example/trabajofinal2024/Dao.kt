package com.example.trabajofinal2024

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
@Dao
interface  EncuestaDAO {
    @Query("SELECT * from encuestas ORDER BY encuestaId ASC")
    fun getEncuestas(): Flow<List<Encuesta>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(encuesta:Encuesta): Long

    @Update
    suspend fun update(encuesta: Encuesta)

    @Query("DELETE FROM encuestas")
    suspend fun borrarTodos()

    @Query("SELECT * FROM encuestas WHERE encuestaId = :id")
    fun getEncuestaById(id: Int): Flow <Encuesta>


}