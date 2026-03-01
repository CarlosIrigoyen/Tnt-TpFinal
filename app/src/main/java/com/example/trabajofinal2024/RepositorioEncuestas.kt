package com.example.trabajofinal2024

import android.util.Log
import androidx.annotation.WorkerThread
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class RepositorioEncuestas(private val encuestaDAO: EncuestaDAO,private val alimentoDAO: AlimentoDAO, ) {

    val allEncuestas: Flow<List<Encuesta>> = encuestaDAO.getEncuestas()


    private val db = Firebase.firestore

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(encuesta: Encuesta): Long {

        // 1️⃣ Guardar primero en Room
        val idRoom = encuestaDAO.insertar(encuesta)

        encuesta.encuestaId = idRoom.toInt()

        try {
            // 2️⃣ Subir a Firestore
            val firestoreId = subirEncuestaAFirebase(encuesta)

            // 3️⃣ Guardar el id de Firestore en Room
            encuesta.firestoreId = firestoreId
            encuestaDAO.update(encuesta)

        } catch (e: Exception) {
            Log.e("FIREBASE", "Error subiendo encuesta: ${e.message}")
            // Aquí NO cancelamos Room
            // Simplemente queda pendiente de sincronizar
        }

        return idRoom
    }

    private suspend fun subirEncuestaAFirebase(encuesta: Encuesta): String {

        val userId = encuesta.userUid

        require(userId.isNotEmpty()) {
            "El userUid está vacío. El usuario no está autenticado."
        }

        val docRef = db.collection("usuarios")
            .document(userId)
            .collection("encuestas")
            .add(
                mapOf(
                    "domicilio" to encuesta.domicilio,
                    "ciudad" to encuesta.ciudad,
                    "latitud" to encuesta.lan,
                    "longitud" to encuesta.lon,
                    "completa" to encuesta.completa,
                    "activa" to encuesta.activa,
                    "currentIndex" to encuesta.currentIndex,
                    "updatedAt" to encuesta.updatedAt
                )
            )
            .await()

        return docRef.id
    }

    @WorkerThread
    suspend fun update(encuesta: Encuesta) {
        encuestaDAO.update(encuesta)
    }

    fun getEncuestas() = encuestaDAO.getEncuestas()

    fun getEncuestaById(id: Int): Flow<Encuesta> {
        return encuestaDAO.getEncuestaById(id)
    }

    fun getEncuestasPorUsuario(uid: String): Flow<List<Encuesta>> = encuestaDAO.getEncuestasPorUsuario(uid)

    fun getPendientesPorUsuario(uid: String): Flow<List<Encuesta>> = encuestaDAO.getPendientesPorUsuario(uid)

    @WorkerThread
    suspend fun updateProgress(encuestaId: Int, index: Int) {
        encuestaDAO.updateProgress(encuestaId, index, System.currentTimeMillis())
        val encuesta = encuestaDAO.getEncuestaByIdOnce(encuestaId)

        encuesta?.let {
            actualizarProgresoEnFirebase(it)
        }
    }

    private suspend fun  actualizarProgresoEnFirebase(encuesta: Encuesta) {

        encuesta.firestoreId?.let { id ->

            db.collection("usuarios")
                .document(encuesta.userUid)
                .collection("encuestas")
                .document(id)
                .update(
                    mapOf(
                        "currentIndex" to encuesta.currentIndex,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
        }
    }

    @WorkerThread
    suspend fun markCompleted(encuestaId: Int, index: Int) {

        encuestaDAO.markCompleted(encuestaId, index, System.currentTimeMillis())

        val encuesta = encuestaDAO.getEncuestaByIdOnce(encuestaId)
            ?: run {
                Log.e("FIREBASE", "Encuesta no encontrada en Room")
                return
            }

        val alimentos = alimentoDAO.obtenerAlimentosPorEncuesta(encuestaId)

        try {
            subirEncuestaCompletaAFirebase(encuesta, alimentos)
            Log.d("FIREBASE", "Encuesta marcada como completada en Firebase")
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error marcando como completada: ${e.message}")
        }
    }
    private suspend fun subirEncuestaCompletaAFirebase(
        encuesta: Encuesta,
        alimentos: List<Alimento>
    ) {

        val firestoreId = encuesta.firestoreId

        if (firestoreId.isNullOrEmpty()) {
            Log.e("FIREBASE", "No existe firestoreId. No se puede actualizar en Firebase.")
            return
        }

        val encuestaRef = db.collection("usuarios")
            .document(encuesta.userUid)
            .collection("encuestas")
            .document(firestoreId)

        encuestaRef.update(
            mapOf(
                "completada" to true,
                "currentIndex" to encuesta.currentIndex,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()

        val alimentosCollection = encuestaRef.collection("alimentos")

        alimentos.forEach { alimento ->
            alimentosCollection
                .document(alimento.alimentoid.toString())
                .set(
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
                )
                .await()
        }
    }

    fun getEncuestasCompletasDesdeFirebase(
        onResult: (List<EncuestaFirestore>) -> Unit
    ) {
        db.collectionGroup("encuestas")
            .get()
            .addOnSuccessListener { result ->

                Log.d("FIREBASE_DEBUG", "Docs encontrados: ${result.size()}")

                result.documents.forEach {
                    Log.d("FIREBASE_DEBUG", it.data.toString())
                }

                val lista = result.documents.mapNotNull { doc ->
                    val lat = doc.getDouble("latitud")
                    val lon = doc.getDouble("longitud")
                    val completa = doc.getBoolean("completa") ?: false

                    if (lat != null && lon != null) {
                        EncuestaFirestore(
                            lan = lat,
                            lon = lon,
                            completa = completa
                        )
                    } else null
                }

                onResult(lista)
            }
    }





    @WorkerThread
    suspend fun abandonEncuesta(encuestaId: Int) {
        encuestaDAO.abandonEncuesta(encuestaId, System.currentTimeMillis())
    }

    // Nueva función para reanudar
    @WorkerThread
    suspend fun reanudarEncuesta(encuestaId: Int) {
        encuestaDAO.reanudarEncuesta(encuestaId, System.currentTimeMillis())
    }
}