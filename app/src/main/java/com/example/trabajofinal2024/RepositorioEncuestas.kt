package com.example.trabajofinal2024

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    // ======================== SINCRONIZACIÓN INICIAL ========================

    /**
     * Sincroniza todas las encuestas del usuario desde Firestore a Room.
     * Llama a esto después del login o al abrir la pantalla de listado.
     */
    suspend fun syncAllEncuestasFromFirebase(userUid: String) {
        try {
            Log.d("FIREBASE", "Sincronizando encuestas para usuario $userUid")
            val snapshot = db.collection("encuestas")
                .whereEqualTo("administradorid", userUid)
                .get()
                .await()

            val encuestas = snapshot.documents.mapNotNull { doc ->
                val encuestaId = doc.getLong("encuestaId")?.toInt() ?: return@mapNotNull null
                Encuesta(
                    encuestaId = encuestaId,
                    administradorid = doc.getString("administradorid") ?: "",
                    voluntarioid = doc.getString("voluntarioid") ?: "",
                    domicilio = doc.getString("domicilio") ?: "",
                    ciudad = doc.getString("ciudad") ?: "",
                    lan = doc.getDouble("latitud") ?: 0.0,
                    lon = doc.getDouble("longitud") ?: 0.0,
                    completa = doc.getBoolean("completa") ?: false,
                    activa = doc.getBoolean("activa") ?: true,
                    currentIndex = doc.getLong("currentIndex")?.toInt() ?: 0,
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis(),
                    firestoreId = doc.id
                )
            }

            // Reemplazar en Room
            encuestaDAO.borrarTodos()
            if (encuestas.isNotEmpty()) {
                encuestaDAO.insertAll(encuestas)
                Log.d("FIREBASE", "${encuestas.size} encuestas guardadas en Room")
            }

            // Por cada encuesta, sincronizar sus alimentos
            encuestas.forEach { encuesta ->
                syncAlimentosFromFirebase(encuesta, userUid)
            }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error sincronizando encuestas", e)
        }
    }

    private suspend fun syncAlimentosFromFirebase(encuesta: Encuesta, userUid: String) {
        try {
            val firestoreId = encuesta.firestoreId ?: return
            val snapshot = db.collection("encuestas")
                .document(firestoreId)
                .collection("alimentos")
                .get()
                .await()

            val alimentos = snapshot.documents.map { doc ->
                Alimento(
                    encuestaId = encuesta.encuestaId,
                    nombre_alimento = doc.getString("nombre") ?: "",
                    categoria = doc.getString("categoria") ?: "",
                    cantidad_alimento = doc.getString("cantidad") ?: "",
                    numero_veces = doc.getString("numero_veces") ?: "1",
                    frecuencia_veces = doc.getString("frecuencia") ?: "Nunca",
                    gramos = doc.getDouble("gramos")?.toFloat() ?: 0f,
                    kcal = doc.getDouble("kcal")?.toFloat() ?: 0f,
                    carbohidratos = doc.getDouble("carbohidratos")?.toFloat() ?: 0f,
                    proteinas = doc.getDouble("proteinas")?.toFloat() ?: 0f,
                    grasas = doc.getDouble("grasas")?.toFloat() ?: 0f,
                    alcohol = doc.getDouble("alcohol")?.toFloat() ?: 0f,
                    colesterol = doc.getDouble("colesterol")?.toFloat() ?: 0f,
                    fibra = doc.getDouble("fibra")?.toFloat() ?: 0f
                )
            }

            alimentoDAO.borrarPorEncuesta(encuesta.encuestaId)
            if (alimentos.isNotEmpty()) {
                alimentoDAO.insertAll(alimentos)
                Log.d("FIREBASE", "${alimentos.size} alimentos guardados para encuesta ${encuesta.encuestaId}")
            }
        } catch (e: Exception) {
            Log.e("FIREBASE", "Error sincronizando alimentos de encuesta ${encuesta.encuestaId}", e)
        }
    }

    // ======================== LISTENER EN TIEMPO REAL (para ubicaciones) ========================





    private val _encuestasFirestore = MutableLiveData<List<Encuesta>>()
    val encuestasFirestore: LiveData<List<Encuesta>> = _encuestasFirestore

    fun listenEncuestasFromFirestore(userUid: String) {
        listenerRegistration?.remove()
        listenerRegistration = db.collection("encuestas")
            .whereEqualTo("administradorid", userUid)
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

    // ======================== OPERACIONES CON FIREBASE (subir, actualizar) ========================

    fun Alimento.getFirestoreId(): String {
        return nombre_alimento.lowercase().trim().replace(" ", "_")
    }

    suspend fun guardarAlimentoEnFirebase(encuesta: Encuesta, alimento: Alimento) {
        val firestoreId = encuesta.firestoreId ?: return
        val ref = db.collection("encuestas")
            .document(firestoreId)
            .collection("alimentos")
            .document(alimento.getFirestoreId())

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
        val docRef = db.collection("encuestas")
            .add(
                mapOf(
                    "domicilio" to encuesta.domicilio,
                    "ciudad" to encuesta.ciudad,
                    "latitud" to encuesta.lan,
                    "longitud" to encuesta.lon,
                    "completa" to encuesta.completa,
                    "administradorid" to encuesta.administradorid,
                    "voluntarioid" to encuesta.voluntarioid,
                    "activa" to encuesta.activa,
                    "currentIndex" to encuesta.currentIndex,
                    "updatedAt" to encuesta.updatedAt,
                    "turnoId" to encuesta.turnoId
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

        db.collection("encuestas")
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

                        administradorid = doc.getString("administradorid") ?: "",
                        voluntarioid = doc.getString("voluntarioid") ?: ""
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
            db.collection("encuestas").whereEqualTo("administradorid", uid).orderBy("updatedAt")
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
                            administradorid = doc.getString("administradorid") ?: "",
                            voluntarioid = doc.getString("voluntarioid") ?: ""
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
                db.collection("encuestas")
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
            db.collection("encuestas")
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
        db.collection("encuestas")
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
        db.collection("encuestas")
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