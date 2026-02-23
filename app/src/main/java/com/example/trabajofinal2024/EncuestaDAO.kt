package com.example.trabajofinal2024

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao  // Esta anotación es para la INTERFAZ, no para el archivo
interface EncuestaDAO {  // La INTERFAZ se llama EncuestaDAO
    @Query("SELECT * from encuestas ORDER BY encuestaId ASC")
    fun getEncuestas(): Flow<List<Encuesta>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(encuesta: Encuesta): Long

    @Update
    suspend fun update(encuesta: Encuesta)

    @Query("DELETE FROM encuestas")
    suspend fun borrarTodos()

    @Query("SELECT * FROM encuestas WHERE encuestaId = :id")
    fun getEncuestaById(id: Int): Flow<Encuesta>

    @Query("SELECT * FROM encuestas WHERE encuestaId = :id LIMIT 1")
    suspend fun getEncuestaByIdOnce(id: Int): Encuesta?

    @Query("SELECT * FROM encuestas WHERE user_uid = :uid ORDER BY encuestaId DESC")
    fun getEncuestasPorUsuario(uid: String): Flow<List<Encuesta>>

    @Query("SELECT * FROM encuestas WHERE user_uid = :uid AND completada = 0 AND activa = 1 ORDER BY encuestaId DESC")
    fun getPendientesPorUsuario(uid: String): Flow<List<Encuesta>>

    @Query("UPDATE encuestas SET current_index = :index, updated_at = :updatedAt WHERE encuestaId = :id")
    suspend fun updateProgress(id: Int, index: Int, updatedAt: Long)

    @Query("UPDATE encuestas SET completada = 1, current_index = :index, updated_at = :updatedAt WHERE encuestaId = :id")
    suspend fun markCompleted(id: Int, index: Int, updatedAt: Long)

    @Query("UPDATE encuestas SET activa = 0, updated_at = :updatedAt WHERE encuestaId = :id")
    suspend fun abandonEncuesta(id: Int, updatedAt: Long)

    @Query("UPDATE encuestas SET activa = 1, updated_at = :updatedAt WHERE encuestaId = :id")
    suspend fun reanudarEncuesta(id: Int, updatedAt: Long)
}