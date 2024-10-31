package com.example.trabajofinal2024

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity( tableName = "encuestas")
data class Encuesta(
    @PrimaryKey( autoGenerate = true)
    var encuestaId: Int = 0,

    @ColumnInfo (name = "completada")
    var completa: Boolean = false,

    @ColumnInfo (name = "domicilio")
    var domicilio: String,

    @ColumnInfo (name = "ciudad")
    var ciudad: String

)