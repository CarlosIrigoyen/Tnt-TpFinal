package com.example.trabajofinal2024

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {

    @Query("SELECT * FROM turnos ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turnos WHERE estado = :estado ORDER BY createdAt DESC")
    fun getByEstado(estado: String): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turnos WHERE firestoreId = :id LIMIT 1")
    suspend fun getByIdOnce(id: String): TurnoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(turno: TurnoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(turnos: List<TurnoEntity>)

    @Update
    suspend fun update(turno: TurnoEntity)

    @Query("DELETE FROM turnos")
    suspend fun deleteAll()
}