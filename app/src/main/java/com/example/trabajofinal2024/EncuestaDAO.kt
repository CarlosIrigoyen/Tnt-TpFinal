package com.example.trabajofinal2024

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao  // Esta anotación es para la INTERFAZ, no para el archivo
interface EncuestaDAO {  // La INTERFAZ se llama EncuestaDAO
    @Query("SELECT * from encuestas ORDER BY firestore_id ASC")
    fun getEncuestas(): Flow<List<Encuesta>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(encuesta: Encuesta): Long

    @Query("DELETE FROM encuestas WHERE user_uid = :uid")
    suspend fun deleteEncuestasPorUsuario(uid: String)

    @Update
    suspend fun update(encuesta: Encuesta)

    @Query("DELETE FROM encuestas")
    suspend fun borrarTodos()

    @Query("SELECT * FROM encuestas WHERE firestore_id = :id")
    fun getEncuestaById(id: String): Flow<Encuesta>

    @Query("SELECT * FROM encuestas WHERE firestore_id = :id LIMIT 1")
    suspend fun getEncuestaByIdOnce(id: String): Encuesta?

    @Query("SELECT * FROM encuestas WHERE user_uid = :uid ORDER BY firestore_id DESC")
    fun getEncuestasPorUsuario(uid: String): Flow<List<Encuesta>>

    @Query("SELECT * FROM encuestas WHERE user_uid = :uid AND completada = 0 AND activa = 1 ORDER BY firestore_id DESC")
    fun getPendientesPorUsuario(uid: String): Flow<List<Encuesta>>

    @Query("UPDATE encuestas SET current_index = :index, updated_at = :updatedAt WHERE firestore_id = :id")
    suspend fun updateProgress(id: String, index: Int, updatedAt: Long)

    @Query("UPDATE encuestas SET completada = 1, current_index = :index, updated_at = :updatedAt WHERE firestore_id = :id")
    suspend fun markCompleted(id: String, index: Int, updatedAt: Long)

    @Query("UPDATE encuestas SET activa = 0, updated_at = :updatedAt WHERE firestore_id = :id")
    suspend fun abandonEncuesta(id: String, updatedAt: Long)

    @Query("UPDATE encuestas SET activa = 1, updated_at = :updatedAt WHERE firestore_id = :id")
    suspend fun reanudarEncuesta(id: String, updatedAt: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(encuestas: List<Encuesta>)
}