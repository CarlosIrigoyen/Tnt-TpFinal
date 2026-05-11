package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
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
    suspend fun insertar(alimento: Alimento):Long 


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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(alimentos: List<Alimento>)

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
            AVG(enc_fibra) AS avg_fibra,
            AVG(enc_gramos) AS avg_gramos,
            AVG(enc_grasas) AS avg_grasas,
            AVG(enc_alcohol) AS avg_alcohol
        FROM (
            SELECT 
                encuesta,
                SUM(kcal_totales) AS enc_kcal,
                SUM(carbohidratos) AS enc_carbohidratos,
                SUM(proteinas) AS enc_proteinas,
                SUM(colesterol) AS enc_colesterol,
                SUM(fibra) AS enc_fibra,
                SUM(gramos) as enc_gramos, 
                SUM(grasas_totales) as enc_grasas,
                SUM(alcohol) as enc_alcohol
            FROM alimentos
            WHERE encuesta IN (
                SELECT encuestaId FROM encuestas WHERE administrador_uuid = :uid AND completada = 1
            )
            GROUP BY encuesta
        ) 
    """)
    fun getAveragesPerCompletedEncuestaByUser(uid: String): Flow<StatsAverages?>


    data class DailySurveyStats(
        val encuestaId: Long,
        val total_kcal: Double?,
        val total_proteinas: Double?,
        val total_fibra: Double?,
        val total_colesterol: Double?,
        val total_alcohol: Double?,
        val total_grasas: Double?,
        val total_carbohidratos: Double?,
        val total_gramos: Double?
    )


    @Query("""
SELECT a.encuesta as encuestaId,
    SUM(a.kcal_totales) as total_kcal,
    SUM(a.proteinas) as total_proteinas,
    SUM(a.fibra) as total_fibra,
    SUM(a.colesterol) as total_colesterol,
    SUM(a.alcohol) as total_alcohol,
    SUM(a.grasas_totales) as total_grasas,
    SUM(a.carbohidratos) as total_carbohidratos,
    SUM(a.gramos) as total_gramos
FROM alimentos a
INNER JOIN encuestas e
    ON a.encuesta = e.encuestaId
WHERE e.administrador_uuid = :uid /*AND e.completada = 1*/
GROUP BY a.encuesta
ORDER BY a.encuesta
""")
    fun getDailyTotalsByUser(uid: String): LiveData<List<DailySurveyStats>>
}
