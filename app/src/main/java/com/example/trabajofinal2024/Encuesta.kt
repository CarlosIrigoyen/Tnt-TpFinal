package com.example.trabajofinal2024

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "encuestas")
data class Encuesta(
    @PrimaryKey(autoGenerate = true)
    var encuestaId: Int = 0,

    @ColumnInfo(name = "completada")
    var completa: Boolean = false,

    @ColumnInfo(name = "domicilio")
    var domicilio: String = "",

    @ColumnInfo(name = "ciudad")
    var ciudad: String = "",

    // UID del usuario (soporte para Firebase Auth futuro)
    @ColumnInfo(name = "user_uid")
    var userUid: String = "",

    // Índice del FoodCatalog donde se quedó (0..N)
    @ColumnInfo(name = "current_index")
    var currentIndex: Int = 0,

    // Activa = true si puede reanudarse; false si fue abandonada
    @ColumnInfo(name = "activa")
    var activa: Boolean = true,

    // Timestamp opcional de última actualización (ms desde epoch)
    @ColumnInfo(name = "updated_at")
    var updatedAt: Long? = null
)
