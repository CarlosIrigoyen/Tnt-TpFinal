package com.example.trabajofinal2024

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlimentoDAO {

    @Query("SELECT * from alimentos ORDER BY alimentoid ASC")
    fun getAlimentos(): Flow<List<Alimento>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertar(alimento: Alimento)


    @Query("DELETE FROM alimentos")
    suspend fun borrarTodos()

    @Query("SELECT * FROM alimentos WHERE encuesta = :encuestaId AND alimento = :nombre LIMIT 1")
    suspend fun getAlimento(encuestaId: Int, nombre: String): Alimento?

    @Query("SELECT * FROM alimentos WHERE encuesta = :encuestaId")
    suspend fun obtenerAlimentosPorEncuesta(encuestaId: Int): List<Alimento>

    @Update
    suspend fun update(alimento: Alimento)

    @Query("DELETE FROM alimentos WHERE encuesta = :encuestaId")
    suspend fun borrarPorEncuesta(encuestaId: Int)

    /**
     * Calcula el promedio por encuesta (sumas por encuesta) de los campos solicitados
     * para las encuestas completadas del usuario :uid.
     *
     * Devuelve un Flow<StatsAverages?> que será null si no hay encuestas completadas.
     */
    @Query("""
        SELECT 
            AVG(enc_kcal) AS avg_kcal,
            AVG(enc_carbohidratos) AS avg_carbohidratos,
            AVG(enc_proteinas) AS avg_proteinas,
            AVG(enc_colesterol) AS avg_colesterol,
            AVG(enc_fibra) AS avg_fibra
        FROM (
            SELECT 
                encuesta,
                SUM(kcal_totales) AS enc_kcal,
                SUM(carbohidratos) AS enc_carbohidratos,
                SUM(proteinas) AS enc_proteinas,
                SUM(colesterol) AS enc_colesterol,
                SUM(fibra) AS enc_fibra
            FROM alimentos
            WHERE encuesta IN (
                SELECT encuestaId FROM encuestas WHERE user_uid = :uid AND completada = 1
            )
            GROUP BY encuesta
        ) 
    """)
    fun getAveragesPerCompletedEncuestaByUser(uid: String): Flow<StatsAverages?>
}
