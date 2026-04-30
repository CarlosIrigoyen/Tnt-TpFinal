package com.example.trabajofinal2024

data class TurnoNombre (
    val id: String,
    val nombreVisible: String
) {
    override fun toString() = nombreVisible
}