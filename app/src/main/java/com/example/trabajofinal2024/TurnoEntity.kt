package com.example.trabajofinal2024

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "turnos")
data class TurnoEntity(
    @PrimaryKey
    val firestoreId: String = "",
    val uidVoluntario: String = "",
    val estado: String = "pendiente",   // pendiente / confirmado / rechazado
    val dia: String = "",
    val horario: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)