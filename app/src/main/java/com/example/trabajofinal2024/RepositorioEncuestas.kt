package com.example.trabajofinal2024

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable

import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class RepositorioEncuestas(
    private val encuestaDAO: EncuestaDAO,
    private val alimentoDAO: AlimentoDAO,
) {

    val allEncuestas: Flow<List<Encuesta>> = encuestaDAO.getEncuestas()

    private val db = Firebase.firestore

    // LiveData para el mapa con actualización en tiempo real
    private val _encuestasFirestoreLiveData = MutableLiveData<List<EncuestaFirestore>>()

    private var listenerRegistration: ListenerRegistration? = null
    val encuestasFirestoreLiveData: LiveData<List<EncuestaFirestore>> = _encuestasFirestoreLiveData


    private val _encuestasFirestore = MutableLiveData<List<Encuesta>>()
    val encuestasFirestore: LiveData<List<Encuesta>> = _encuestasFirestore

    fun listenEncuestasFromFirestore(userUid: String) {

        listenerRegistration?.remove()
        listenerRegistration = db.collection("usuarios")
            .document(userUid)
            .collection("encuestas")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("FIREBASE", "Error en listener: ${error.message}")
                    return@addSnapshotListener
                }

                val lista = snapshots?.documents?.mapNotNull { doc ->
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
                } ?: emptyList()

                _encuestasFirestoreLiveData.postValue(lista)
            }
    }

    fun Alimento.getFirestoreId(): String {
        return nombre_alimento
            .lowercase()
            .trim()
            .replace(" ", "_")
    }


    @WorkerThread
    suspend fun insert(encuesta: Encuesta): Long {
        val idRoom = encuestaDAO.insertar(encuesta)
        encuesta.encuestaId = idRoom.toInt()
        try {
            val firestoreId = subirEncuestaAFirebase(encuesta)
            encuesta.firestoreId = firestoreId
            encuestaDAO.update(encuesta)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error subiendo encuesta: ${e.message}")
        }
        return idRoom
    }

    suspend fun insertFirebase(encuesta:Encuesta): String {
        return try {
            val firestoreId = subirEncuestaAFirebase(encuesta)
            encuesta.firestoreId = firestoreId
            firestoreId
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error subiendo encuesta: ${e.message}")
            ""
        }
    }

    private suspend fun subirEncuestaAFirebase(encuesta: Encuesta): String {
        val userId = encuesta.userUid
        require(userId.isNotEmpty()) { "El userUid está vacío." }
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

    fun getEncuestaFirebase(uid: String, encuestaId: String): LiveData<Encuesta?> {
        val liveData = MutableLiveData<Encuesta?>()

        db.collection("usuarios")
            .document(uid)
            .collection("encuestas")
            .document(encuestaId)
            .addSnapshotListener { doc, error ->

                if (error != null) {
                    liveData.postValue(null)
                    return@addSnapshotListener
                }

                if (doc != null && doc.exists()) {
                    val encuesta = Encuesta(
                        encuestaId = 0,
                        firestoreId = doc.id,

                        domicilio = doc.getString("domicilio") ?: "",
                        ciudad = doc.getString("ciudad") ?: "",

                        lan = doc.getDouble("latitud") ?: 0.0,
                        lon = doc.getDouble("longitud") ?: 0.0,

                        completa = doc.getBoolean("completa") ?: false,
                        activa = doc.getBoolean("activa") ?: true,

                        currentIndex = doc.getLong("currentIndex")?.toInt() ?: 0,
                        updatedAt = doc.getLong("updatedAt"),

                        userUid = uid
                    )

                    liveData.postValue(encuesta)
                }
            }

        return liveData
    }

    fun getEncuestaById(id: Int): Flow<Encuesta> = encuestaDAO.getEncuestaById(id)

    //fun getEncuestasPorUsuario(uid: String): Flow<List<Encuesta>> = encuestaDAO.getEncuestasPorUsuario(uid)

    fun getEncuestasPorUsuario(uid: String): Flow<List<Encuesta>> = callbackFlow {
        val listener =
            db.collection("usuarios").document(uid).collection("encuestas").orderBy("updatedAt")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("FIREBASE", "Error en listener: ${error.message}")
                        return@addSnapshotListener
                    }
                    val lista = snapshots?.documents?.mapNotNull { doc ->
                        val lat = doc.getDouble("latitud")
                        val lon = doc.getDouble("longitud")
                        val completa = doc.getBoolean("completa") ?: false
                        Encuesta(
                            encuestaId = 0,
                            firestoreId = doc.id,
                            domicilio = doc.getString("domicilio") ?: "",
                            ciudad = doc.getString("ciudad") ?: "",
                            lan = lat ?: 0.0,
                            lon = lon ?: 0.0,
                            completa = completa,
                            activa = doc.getBoolean("activa") ?: false,
                            currentIndex = doc.getLong("currentIndex")?.toInt() ?: 0,
                            updatedAt = doc.getLong("updatedAt") ?: 0,
                            userUid = uid
                        )
                    } ?: emptyList()
                    trySend(lista)
                }
        awaitClose { listener.remove() }

    }


    fun getPendientesPorUsuario(uid: String): Flow<List<Encuesta>> =
        encuestaDAO.getPendientesPorUsuario(uid)

    /*@WorkerThread
    suspend fun updateProgress(uid: String, encuestaId: String, index: Int) {
        //encuestaDAO.updateProgress(encuestaId, index, System.currentTimeMillis())
        //val encuesta = encuestaDAO.getEncuestaByIdOnce(encuestaId)
        encuesta?.let { actualizarProgresoEnFirebase(it) }
    }
    */


    suspend fun updateProgress(uid: String, encuestaId: String, index: Int) {
        withContext(NonCancellable) {
            try {
                db.collection("usuarios")
                    .document(uid)
                    .collection("encuestas")
                    .document(encuestaId)
                    .update(
                        mapOf(
                            "currentIndex" to index,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()

            } catch (
                e: Exception
            ) {
                Log.e("FIREBASE", "Error actualizando progreso: ${e.message}")
            }

        }

    }



    @WorkerThread
    suspend fun markCompleted(encuesta: Encuesta) {
        /*try {
            encuestaDAO.update(encuesta)
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error actualizando encuesta: ${e.message}")
        }
        */

    }

    suspend fun markCompletedFirebase(
        uid: String,
        encuestaId: String,
        currentIndex: Int
    ) { withContext(NonCancellable) {

        try {
            db.collection("usuarios")
                .document(uid)
                .collection("encuestas")
                .document(encuestaId)
                .update(
                    mapOf(
                        "completa" to true,
                        "currentIndex" to currentIndex,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Log.d("FIREBASE", "Encuesta marcada como completada")
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error marcando como completada: ${e.message}")
        }

    }

    }

    @WorkerThread
    suspend fun abandonEncuesta(encuestaId: Int) {
        encuestaDAO.abandonEncuesta(encuestaId, System.currentTimeMillis())
    }

    @WorkerThread
    suspend fun abandonEncuestaFirebase(uid: String, encuestaId: String) {
        db.collection("usuarios")
            .document(uid)
            .collection("encuestas")
            .document(encuestaId)
            .update(
                mapOf(
                    "activa" to false,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()

    }

    @WorkerThread
    suspend fun reanudarEncuestaFirebase(uid: String, encuestaId: String) {
        db.collection("usuarios")
            .document(uid)
            .collection("encuestas")
            .document(encuestaId)
            .update(
                mapOf(
                    "activa" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .await()

    }



    @WorkerThread
    suspend fun reanudarEncuesta(encuestaId: Int) {
        encuestaDAO.reanudarEncuesta(encuestaId, System.currentTimeMillis())
    }

    @WorkerThread
    suspend fun obtenerAlimentosPorEncuesta(encuestaId: Int): List<Alimento> {
        return alimentoDAO.obtenerAlimentosPorEncuesta(encuestaId)
    }
}