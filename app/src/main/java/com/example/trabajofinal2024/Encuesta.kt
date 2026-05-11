package com.example.trabajofinal2024

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encuestas")
data class Encuesta(
    @PrimaryKey(autoGenerate = true)
    var encuestaId: Int = 0,

    @ColumnInfo(name = "firestore_id")
    var firestoreId: String? = null,

    @ColumnInfo(name = "completada")
    var completa: Boolean = false,

    @ColumnInfo(name = "domicilio")
    var domicilio: String = "",

    @ColumnInfo(name = "ciudad")
    var ciudad: String = "",

    @ColumnInfo(name= "longitud")
    var lon: Double = 0.0,

    @ColumnInfo(name = "latitud")
    var lan: Double = 0.0,

    // UID del usuario (soporte para Firebase Auth futuro)
    @ColumnInfo(name = "voluntario_uuid")
    var voluntarioid: String = "",

    @ColumnInfo(name = "administrador_uuid")
    var administradorid: String = "",


    // Índice del FoodCatalog donde se quedó (0..N)
    @ColumnInfo(name = "current_index")
    var currentIndex: Int = 0,

    // Activa = true si puede reanudarse; false si fue abandonada
    @ColumnInfo(name = "activa")
    var activa: Boolean = true,

    // Timestamp opcional de última actualización (ms desde epoch)
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long? = null,

    @ColumnInfo(name = "turnoId")
    var turnoId: String? = null
)
