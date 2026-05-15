package com.example.trabajofinal2024

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

            val hoy = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

            val turnos = FirebaseFirestore.getInstance()
                .collection("turnos")
                .whereEqualTo("estado", "confirmado")
                .whereEqualTo("asignado", false)
                .get()
                .await()
                .documents
            turnos.mapNotNull { doc ->
                val uidVoluntario = doc.getString("uidVoluntario") ?: return@mapNotNull null
                val horario = doc.getString("horario") ?: ""
                val dia = doc.getString("dia") ?: ""

                val fechaTurno = try {
                    LocalDate.parse(dia, formatter)
                } catch (e: Exception) {
                    return@mapNotNull null
                }
                if (fechaTurno.isBefore(hoy)) return@mapNotNull null

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
                    nombreVisible = "Turno a las $horario del día $dia - $nombreVoluntario",
                    uidVoluntario = uidVoluntario
                )
            }
        } catch (e: Exception) {
            Log.e("Error al momento de buscar turnos", "Error: ${e.message}")
            emptyList()
        }
    }



    suspend fun asignarTurno(turnoId: String) {
        try {
            turnosCollection.document(turnoId)
                .update("asignado", true)
                .await()
            Log.d("TurnoRepository", "Turno asignado correctamente")
        } catch (e: Exception) {
            Log.e("TurnoRepository", "Error asignando turno", e)
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
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("TurnoRepository", "UID al actualizar: $uid")
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

    // NUEVA FUNCIÓN: obtiene mapa de voluntarios con nombre, apellido, email,fecha de nacimiento y telefono
    suspend fun obtenerMapaVoluntarios(): Map<String, VoluntarioInfo> {
        return try {
            val snapshot = firestore.collection("voluntarios").get().await()
            snapshot.documents.associate { doc ->
                val nombre = doc.getString("nombre") ?: ""
                val apellido = doc.getString("apellido") ?: ""
                val email = doc.getString("email") ?: ""
                val fechaNac = doc.getString("fechaNac") ?: ""
                val telefono = doc.getString("telefono") ?: ""   // ← nuevo
                doc.id to VoluntarioInfo(nombre, apellido, email, fechaNac, telefono)
            }
        } catch (e: Exception) {
            Log.e("TurnoRepository", "Error obteniendo voluntarios", e)
            emptyMap()
        }
    }
}