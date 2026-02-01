package com.example.trabajofinal2024

import androidx.lifecycle.MutableLiveData

class FoodItem(val template: FoodTemplate) {
    val cantidad = MutableLiveData<String>("1")
    val numeroveces = MutableLiveData<String>("1")
    val frecuencia = MutableLiveData<String>("")

    val alimentoNombre: String get() = template.nombre
    val categoria: String get() = template.categoria

    private fun cantidadFloat(): Float = cantidad.value?.toFloatOrNull() ?: 0f
    private fun vecesFloat(): Float = numeroveces.value?.toFloatOrNull() ?: 0f

    private fun scaleByFrequency(value: Float): Float {
        return when (frecuencia.value) {
            "Diaria" -> value
            "Semanal" -> value * 7f
            "Mensual" -> value * 30f
            "Anual" -> value * 365f
            else -> 0f
        }
    }

    fun calcularGramosTotales(): Float = scaleByFrequency(cantidadFloat() * template.gramosPorUnidad * vecesFloat())
    fun calcularKcal(): Float = scaleByFrequency(cantidadFloat() * template.kcalPorUnidad * vecesFloat())
    fun calcularCarbohidratos(): Float = scaleByFrequency(cantidadFloat() * template.carbohidratosPorUnidad * vecesFloat())
    fun calcularProteinas(): Float = scaleByFrequency(cantidadFloat() * template.proteinasPorUnidad * vecesFloat())
    fun calcularGrasasTotales(): Float = scaleByFrequency(cantidadFloat() * template.grasasPorUnidad * vecesFloat())
    fun calcularAlcohol(): Float = scaleByFrequency(cantidadFloat() * template.alcoholPorUnidad * vecesFloat())
    fun calcularColesterol(): Float = scaleByFrequency(cantidadFloat() * template.colesterolPorUnidad * vecesFloat())
    fun calcularFibra(): Float = scaleByFrequency(cantidadFloat() * template.fibraPorUnidad * vecesFloat())

    fun resetToDefaults() {
        cantidad.value = "1"
        numeroveces.value = "1"
        frecuencia.value = ""
    }
}
