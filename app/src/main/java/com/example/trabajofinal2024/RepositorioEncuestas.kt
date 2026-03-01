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

        encuesta.firestoreId = idRoom.toString()

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

    suspend fun deleteEncuestasPorUsuario(uid: String) = encuestaDAO.deleteEncuestasPorUsuario(uid)

    fun getEncuestas() = encuestaDAO.getEncuestas()

    fun getEncuestaById(id: String): Flow<Encuesta> {
        return encuestaDAO.getEncuestaById(id)
    }

    fun getEncuestasPorUsuario(uid: String): Flow<List<Encuesta>> = encuestaDAO.getEncuestasPorUsuario(uid)

    fun getPendientesPorUsuario(uid: String): Flow<List<Encuesta>> = encuestaDAO.getPendientesPorUsuario(uid)

    @WorkerThread
    suspend fun updateProgress(encuestaId: String, index: Int) {
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

    suspend fun insertAll(encuestas: List<Encuesta>) {
        encuestaDAO.insertAll(encuestas)
    }
    @WorkerThread
    suspend fun markCompleted(encuestaId: String, index: Int) {
        encuestaDAO.markCompleted(encuestaId, index, System.currentTimeMillis())

        val encuesta = encuestaDAO.getEncuestaByIdOnce(encuestaId)
            ?: return

        val alimentos = alimentoDAO.obtenerAlimentosPorEncuesta(encuestaId)

        subirEncuestaCompletaAFirebase(encuesta, alimentos)
    }

    private suspend fun subirEncuestaCompletaAFirebase(
        encuesta: Encuesta,
        alimentos: List<Alimento>
    ) {

        val encuestaRef = db.collection("usuarios")
            .document(encuesta.userUid)
            .collection("encuestas")
            .document(encuesta.firestoreId!!)

        encuestaRef.update(
            mapOf(
                "completa" to true,
                "currentIndex" to encuesta.currentIndex,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()

        val alimentosCollection = encuestaRef.collection("alimentos")


        alimentos.forEach { alimento ->
            alimentosCollection.document(alimento.alimentoid.toString()).set(
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
        }
    }

    fun getEncuestasCompletasDesdeFirebase(
        onResult: (List<EncuestaFirestore>) -> Unit
    ) {
        db.collectionGroup("encuestas")
            .whereEqualTo("completa", true)
            .get()
            .addOnSuccessListener { result ->

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
    suspend fun abandonEncuesta(encuestaId: String) {
        encuestaDAO.abandonEncuesta(encuestaId, System.currentTimeMillis())
    }

    // Nueva función para reanudar
    @WorkerThread
    suspend fun reanudarEncuesta(encuestaId: String) {
        encuestaDAO.reanudarEncuesta(encuestaId, System.currentTimeMillis())
    }
}