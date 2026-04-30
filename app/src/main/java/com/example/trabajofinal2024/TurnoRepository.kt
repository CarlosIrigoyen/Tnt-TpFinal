package com.example.trabajofinal2024

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TurnoRepository(
    private val turnoDao: TurnoDao,
    private val firestore: FirebaseFirestore
) {

    private val turnosCollection = firestore.collection("turnos")
    private var listenerRegistration: ListenerRegistration? = null

    val turnosLiveData: LiveData<List<TurnoEntity>> = turnoDao.getAll().asLiveData()
    val turnosPendientesLiveData: LiveData<List<TurnoEntity>> = turnoDao.getByEstado("pendiente").asLiveData()
    val turnosConfirmadosLiveData: LiveData<List<TurnoEntity>> = turnoDao.getByEstado("confirmado").asLiveData()
    val turnosRechazadosLiveData: LiveData<List<TurnoEntity>> = turnoDao.getByEstado("rechazado").asLiveData()

    fun startListening() {
        listenerRegistration?.remove()
        listenerRegistration = turnosCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("TurnoRepository", "Error escuchando turnos", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val lista = snapshot.documents.mapNotNull { doc ->
                        val uid = doc.getString("uidVoluntario")
                            ?: doc.getString("voluntarioUid")
                            ?: ""
                        if (uid.isBlank()) return@mapNotNull null

                        TurnoEntity(
                            firestoreId = doc.id,
                            uidVoluntario = uid,
                            estado = doc.getString("estado") ?: "pendiente",
                            dia = doc.getString("dia") ?: "",
                            horario = doc.getString("horario") ?: "",
                            direccion = doc.getString("direccion") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                        )
                    }
                    turnoDao.deleteAll()
                    if (lista.isNotEmpty()) turnoDao.insertAll(lista)
                    Log.d("TurnoRepository", "Turnos sincronizados: ${lista.size}")
                } catch (e: Exception) {
                    Log.e("TurnoRepository", "Error sincronizando turnos", e)
                }
            }
        }
    }

    suspend fun getTurnosConfirmados(): List<TurnoNombre> {
        return try {
            val turnos = FirebaseFirestore.getInstance()
                .collection("turnos")
                .whereEqualTo("estado", "confirmado")
                .get()
                .await()
                .documents
            turnos.mapNotNull { doc ->
                val uidVoluntario = doc.getString("uidVoluntario") ?: return@mapNotNull null
                val horario = doc.getString("horario") ?: ""
                val dia = doc.getString("dia") ?: ""

                val voluntario = FirebaseFirestore.getInstance()
                    .collection("voluntarios")
                    .whereEqualTo("firebaseUid", uidVoluntario)
                    .limit(1)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()

                val nombreVoluntario = voluntario?.getString("nombre") ?: "Desconocido"

                TurnoNombre(
                    id = doc.id,
                    nombreVisible = "Turno a las $horario del día $dia - $nombreVoluntario"
                )
            }
        } catch (e: Exception) {
            Log.e("Error al momento de buscar turnos", "Error: ${e.message}")
            emptyList()
        }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun getAllTurnos(): Flow<List<TurnoEntity>> = turnoDao.getAll()
    fun getTurnosByEstado(estado: String): Flow<List<TurnoEntity>> = turnoDao.getByEstado(estado)

    suspend fun getTurnoById(id: String): TurnoEntity? = turnoDao.getByIdOnce(id)

    suspend fun actualizarTurno(turno: TurnoEntity) {
        turnoDao.update(turno)
        try {
            turnosCollection.document(turno.firestoreId)
                .update(
                    mapOf(
                        "uidVoluntario" to turno.uidVoluntario,
                        "estado" to turno.estado,
                        "dia" to turno.dia,
                        "horario" to turno.horario,
                        "direccion" to turno.direccion,
                        "descripcion" to turno.descripcion,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            Log.e("TurnoRepository", "Error actualizando Firestore", e)
        }
    }

    suspend fun crearTurno(turno: TurnoEntity) {
        val docRef = turnosCollection.add(
            mapOf(
                "uidVoluntario" to turno.uidVoluntario,
                "estado" to turno.estado,
                "dia" to turno.dia,
                "horario" to turno.horario,
                "direccion" to turno.direccion,
                "descripcion" to turno.descripcion,
                "createdAt" to turno.createdAt,
                "updatedAt" to turno.updatedAt
            )
        ).await()
        turnoDao.insert(turno.copy(firestoreId = docRef.id))
    }

    // NUEVA FUNCIÓN: obtiene mapa de voluntarios con nombre, apellido, email y fecha de nacimiento
    suspend fun obtenerMapaVoluntarios(): Map<String, VoluntarioInfo> {
        return try {
            val snapshot = firestore.collection("voluntarios").get().await()
            snapshot.documents.associate { doc ->
                val nombre = doc.getString("nombre") ?: ""
                val apellido = doc.getString("apellido") ?: ""
                val email = doc.getString("email") ?: ""
                val fechaNac = doc.getString("fechaNac") ?: ""
                doc.id to VoluntarioInfo(nombre, apellido, email, fechaNac)
            }
        } catch (e: Exception) {
            Log.e("TurnoRepository", "Error obteniendo voluntarios", e)
            emptyMap()
        }
    }
}