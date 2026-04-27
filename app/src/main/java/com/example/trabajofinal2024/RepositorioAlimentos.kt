package com.example.trabajofinal2024

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.trabajofinal2024.AlimentoDAO.DailySurveyStats
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RepositorioAlimentos(private val alimentoDao: AlimentoDAO) {

    val allAlimentos: Flow<List<Alimento>> = alimentoDao.getAlimentos()


    val db = Firebase.firestore

    suspend fun insert(alimento: Alimento):Long {
        return alimentoDao.insertar(alimento)
    }

    suspend fun getAlimento(encuestaId: Int, nombre: String): Alimento? {
        return alimentoDao.getAlimento(encuestaId, nombre)
    }

    suspend fun getAlimentoFirebase(
        uid: String,
        encuestaId: String,
        nombre: String
    ): Alimento? {

        return try {
            val doc = db.collection("usuarios")
                .document(uid)
                .collection("encuestas")
                .document(encuestaId)
                .collection("alimentos")
                .document(nombre)
                .get()
                .await()

            if (doc.exists()) {
                Alimento(
                    nombre_alimento = nombre,
                    numero_veces = doc.getString("numero_veces") ?: "",
                    cantidad_alimento = doc.getString("cantidad") ?: "",
                    frecuencia_veces = doc.getString("frecuencia") ?: "",
                    gramos = doc.getDouble("gramos")?.toFloat() ?: 0f,
                    kcal = doc.getDouble("kcal")?.toFloat() ?: 0f,
                    carbohidratos = doc.getDouble("carbohidratos")?.toFloat() ?: 0f,
                    proteinas = doc.getDouble("proteinas")?.toFloat() ?: 0f,
                    grasas = doc.getDouble("grasas")?.toFloat() ?: 0f,
                    alcohol = doc.getDouble("alcohol")?.toFloat() ?: 0f,
                    colesterol = doc.getDouble("colesterol")?.toFloat() ?: 0f,
                    fibra = doc.getDouble("fibra")?.toFloat() ?: 0f,
                    categoria = doc.getString("categoria") ?: "",
                    encuestaId = 0
                )
            } else null

        } catch (e: Exception) {
            Log.e("FIREBASE", "Error obteniendo alimento: ${e.message}")
            return null

        }

    }

    suspend fun getAlimentosPorEncuestaFirebase(uid: String, encuestaId: String): List<Alimento> {
        return try {
            val snapshot = db.collection("usuarios")
                .document(uid)
                .collection("encuestas")
                .document(encuestaId)
                .collection("alimentos")
                .get()
                .await()

            Log.e("FIREBASE", "Obteniendo alimentos: ${snapshot.documents.size}")

            snapshot.documents.mapNotNull { doc ->
                if (doc.exists()) {
                    Alimento(
                        nombre_alimento = doc.id,
                        numero_veces = doc.getString("numero_veces") ?: "",
                        cantidad_alimento = doc.getString("cantidad") ?: "",
                        frecuencia_veces = doc.getString("frecuencia") ?: "",
                        gramos = doc.getDouble("gramos")?.toFloat() ?: 0f,
                        kcal = doc.getDouble("kcal")?.toFloat() ?: 0f,
                        carbohidratos = doc.getDouble("carbohidratos")?.toFloat() ?: 0f,
                        proteinas = doc.getDouble("proteinas")?.toFloat() ?: 0f,
                        grasas = doc.getDouble("grasas")?.toFloat() ?: 0f,
                        alcohol = doc.getDouble("alcohol")?.toFloat() ?: 0f,
                        colesterol = doc.getDouble("colesterol")?.toFloat() ?: 0f,
                        fibra = doc.getDouble("fibra")?.toFloat() ?: 0f,
                        categoria = doc.getString("categoria") ?: "",
                        encuestaId = 0
                    )

                } else null
            }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error obteniendo alimentos: ${e.message}")
            emptyList()
        }
    }

    suspend fun update(alimento: Alimento) =
        alimentoDao.update(alimento)


    suspend fun guardarAlimentoEnFirebase(
        uid: String, encuestaId: String,
        alimento: Alimento
    ) {
        withContext(NonCancellable) {
            try {
                val ref = db.collection("usuarios")
                    .document(uid)
                    .collection("encuestas")
                    .document(encuestaId)
                    .collection("alimentos")
                    .document(alimento.nombre_alimento)

                ref.set(
                    mapOf(
                        "nombre" to alimento.nombre_alimento,
                        "categoria" to alimento.categoria,
                        "cantidad" to alimento.cantidad_alimento,
                        "numero_veces" to alimento.numero_veces,
                        "frecuencia" to alimento.frecuencia_veces,
                        "gramos" to alimento.gramos,
                        "kcal" to alimento.kcal,
                        "carbohidratos" to alimento.carbohidratos,
                        "proteinas" to alimento.proteinas,
                        "grasas" to alimento.grasas,
                        "alcohol" to alimento.alcohol,
                        "colesterol" to alimento.colesterol,
                        "fibra" to alimento.fibra
                    )
                ).await()

            } catch (e: Exception) {
                Log.e("FIREBASE", "Error guardando alimento: ${e.message}")
            }

        }
    }

    suspend fun obtenerAlimentosPorEncuesta(encuestaId: Int): List<Alimento> {
        return alimentoDao.obtenerAlimentosPorEncuesta(encuestaId)
    }

    suspend fun borrarPorEncuesta(encuestaId: Int) {
        alimentoDao.borrarPorEncuesta(encuestaId)
    }

    fun getStatsAveragesForUser(uid: String): Flow<StatsAverages?> {
        return alimentoDao.getAveragesPerCompletedEncuestaByUser(uid)
    }

    suspend fun getStatsAveragesForUserFirebase(uid: String): StatsAverages? {
        return try {
            val encuestasSnapshot = db.collection("usuarios")
                .document(uid)
                .collection("encuestas")
                .whereEqualTo("completa", true)
                .get()
                .await()

            if (encuestasSnapshot.isEmpty) return null

            val totalesPorEncuesta = encuestasSnapshot.documents.map { encuestaDoc ->
                val alimentosSnapshot = db.collection("usuarios")
                    .document(uid)
                    .collection("encuestas")
                    .document(encuestaDoc.id)
                    .collection("alimentos")
                    .get()
                    .await()

                // Sumar todos los alimentos de esta encuesta
                var kcal = 0.0
                var carbs = 0.0
                var proteinas = 0.0
                var colesterol = 0.0
                var fibra = 0.0
                var gramos = 0.0
                var grasas = 0.0
                var alcohol = 0.0

                alimentosSnapshot.documents.forEach { doc ->
                    kcal += doc.getDouble("kcal") ?: 0.0
                    carbs += doc.getDouble("carbohidratos") ?: 0.0
                    proteinas += doc.getDouble("proteinas") ?: 0.0
                    colesterol += doc.getDouble("colesterol") ?: 0.0
                    fibra += doc.getDouble("fibra") ?: 0.0
                    gramos += doc.getDouble("gramos") ?: 0.0
                    grasas += doc.getDouble("grasas") ?: 0.0
                    alcohol += doc.getDouble("alcohol") ?: 0.0
                }

                // Retorna el total de esta encuesta
                listOf(kcal, carbs, proteinas, colesterol, fibra, gramos, grasas, alcohol)
            }

            val n = totalesPorEncuesta.size.toDouble()

            StatsAverages(
                avg_kcal = totalesPorEncuesta.sumOf { it[0] } / n,
                avg_carbohidratos = totalesPorEncuesta.sumOf { it[1] } / n,
                avg_proteinas = totalesPorEncuesta.sumOf { it[2] } / n,
                avg_colesterol = totalesPorEncuesta.sumOf { it[3] } / n,
                avg_fibra = totalesPorEncuesta.sumOf { it[4] } / n,
                avg_gramos = totalesPorEncuesta.sumOf { it[5] } / n,
                avg_grasas = totalesPorEncuesta.sumOf { it[6] } / n,
                avg_alcohol = totalesPorEncuesta.sumOf { it[7] } / n
            )

        } catch (e: Exception) {
            Log.e("FIREBASE", "Error calculando estadísticas: ${e.message}")
            null
        }
    }

    suspend fun getDailyTotalsByUserFirebase(uid: String): List<DailySurveyStats> {
        return try {
            val encuestasSnapshot = db.collection("usuarios")
                .document(uid)
                .collection("encuestas")
                .whereEqualTo("completa", true)
                .get()
                .await()

            if (encuestasSnapshot.isEmpty) return emptyList()

            encuestasSnapshot.documents.mapIndexed { index, encuestaDoc ->
                val alimentosSnapshot = db.collection("usuarios")
                    .document(uid)
                    .collection("encuestas")
                    .document(encuestaDoc.id)
                    .collection("alimentos")
                    .get()
                    .await()

                var kcal = 0.0
                var proteinas = 0.0
                var fibra = 0.0
                var colesterol = 0.0
                var alcohol = 0.0
                var grasas = 0.0
                var carbs = 0.0
                var gramos = 0.0

                alimentosSnapshot.documents.forEach { doc ->
                    kcal += doc.getDouble("kcal") ?: 0.0
                    proteinas += doc.getDouble("proteinas") ?: 0.0
                    fibra += doc.getDouble("fibra") ?: 0.0
                    colesterol += doc.getDouble("colesterol") ?: 0.0
                    alcohol += doc.getDouble("alcohol") ?: 0.0
                    grasas += doc.getDouble("grasas") ?: 0.0
                    carbs += doc.getDouble("carbohidratos") ?: 0.0
                    gramos += doc.getDouble("gramos") ?: 0.0
                }

                DailySurveyStats(
                    encuestaId = index.toLong() + 1,
                    total_kcal = kcal,
                    total_proteinas = proteinas,
                    total_fibra = fibra,
                    total_colesterol = colesterol,
                    total_alcohol = alcohol,
                    total_grasas = grasas,
                    total_carbohidratos = carbs,
                    total_gramos = gramos
                )
            }

        } catch (e: Exception) {
            Log.e("FIREBASE", "Error obteniendo totales: ${e.message}")
            emptyList()
        }
    }

    fun getDailyTotalsByUser(uid: String): LiveData<List<AlimentoDAO.DailySurveyStats>> {
        return alimentoDao.getDailyTotalsByUser(uid)
    }
    }

