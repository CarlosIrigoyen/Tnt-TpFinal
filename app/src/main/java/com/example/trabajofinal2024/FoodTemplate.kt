package com.example.trabajofinal2024

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FoodTemplate(
    val nombre: String,
    val categoria: String,
    val gramosPorUnidad: Float,
    val kcalPorUnidad: Float,
    val carbohidratosPorUnidad: Float,
    val proteinasPorUnidad: Float,
    val grasasPorUnidad: Float,
    val alcoholPorUnidad: Float = 0f,
    val colesterolPorUnidad: Float = 0f,
    val fibraPorUnidad: Float = 0f
) : Parcelable
