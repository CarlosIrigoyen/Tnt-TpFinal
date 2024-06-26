package com.example.trabajofinal2024

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( tableName = "encuestas")
data class Encuesta(
    @PrimaryKey( autoGenerate = true)
    var encuestaId: Int = 0,

    @ColumnInfo(name = "alimento")
    var nombre_alimento: String,

    @ColumnInfo(name = "cantidad")
    var cantidad_alimento: String,

    @ColumnInfo(name = "numero_veces")
    var numero_veces: String,

    @ColumnInfo(name = "frecuencia")
    var frecuencia_veces: String

)