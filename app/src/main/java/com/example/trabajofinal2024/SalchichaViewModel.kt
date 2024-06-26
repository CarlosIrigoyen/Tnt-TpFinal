package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SalchichaViewModel: ViewModel() {
    private val _cantidad = MutableLiveData<String>()
    private val _numeroVeces = MutableLiveData<String>()
    private val _categoria = MutableLiveData<String>("Carnes procesadas")
    private val _alimento = MutableLiveData<String>("Salchicha")
    private  val _gramos = MutableLiveData<Float> (100.0f)
    private val _kilocal = MutableLiveData<Float> (187.9f)
    private val _carbohidratos = MutableLiveData<Float> (5.8f)
    private val _proteinas = MutableLiveData<Float> (12.3f)
    private val _grasasTotales = MutableLiveData<Float> (12.9f)
    private val _alcohol = MutableLiveData<Float> (0.0f)
    private val _colesterol = MutableLiveData<Float> (30.0f)
    private val _fibra = MutableLiveData<Float> (0.0f)


    private val _frecuencia = MutableLiveData<String>()

    val gramos: LiveData<Float>
        get() = _gramos

    val kilocal: LiveData<Float>
        get() = _kilocal

    val carbohidratos: LiveData<Float>
        get() = _carbohidratos
    val proteinas: LiveData<Float>
        get() = _proteinas
    val grasasTotales: LiveData<Float>
        get() = _grasasTotales
    val alcohol: LiveData<Float>
        get() = _alcohol
    val colesterol: LiveData<Float>
        get() = _colesterol
    val fibra: LiveData<Float>
        get() = _fibra



    val categoria: LiveData<String>
        get() = _categoria
    val alimento: LiveData<String>
        get() = _alimento

    val cantidad: LiveData<String>
        get() = _cantidad


    val frecuencia: LiveData<String>
        get() = _frecuencia


    val numeroveces: LiveData<String>
        get() = _numeroVeces

    var encuestaId: Int = 0
        private set

    fun setEncuestaId(id: Int) {
        encuestaId = id
    }
    lateinit var cantidadList: MutableList<String>
        private set

    private fun elegirCantidad() {
        cantidadList = mutableListOf(
            "1",
            "2",
            "3",
            "4",
            "5"
        )
    }

    init {
        elegirCantidad()
    }

    fun setNumeroVeces(value: String) {
        _numeroVeces.value = value
    }


    fun setCantidad(value: String) {
        cantidadList.remove(value)
        cantidadList.add(0,value)
        _cantidad.value = value
    }

    fun setFrecuencia(value: String) {
        _frecuencia.value = value
    }

    fun calcularGramosTotales(): Float {
        val cantidad = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val gramosPorMl = _gramos.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidad * gramosPorMl * numeroVeces
            "Semanal" -> cantidad * gramosPorMl * numeroVeces * 7
            "Mensual" -> cantidad * gramosPorMl * numeroVeces * 30
            "Anual" -> cantidad * gramosPorMl * numeroVeces * 365
            else -> 0f
        }
    }

    fun calcularKcal(): Float {
        val cantidad = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val kcal = _kilocal.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidad * kcal * numeroVeces
            "Semanal" -> cantidad * kcal * numeroVeces * 7
            "Mensual" -> cantidad * kcal * numeroVeces * 30
            "Anual" -> cantidad * kcal * numeroVeces * 365
            else -> 0f
        }
    }

    fun calcularCarbohidratos(): Float {
        val cantidad = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val carbohidratos = _carbohidratos.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidad * carbohidratos * numeroVeces
            "Semanal" -> cantidad * carbohidratos * numeroVeces * 7
            "Mensual" -> cantidad * carbohidratos * numeroVeces * 30
            "Anual" -> cantidad * carbohidratos * numeroVeces * 365
            else -> 0f
        }
    }
    fun calcularProteinas(): Float {
        val cantidad = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val proteinas = _proteinas.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidad * proteinas * numeroVeces
            "Semanal" -> cantidad * proteinas * numeroVeces * 7
            "Mensual" -> cantidad * proteinas * numeroVeces * 30
            "Anual" -> cantidad * proteinas * numeroVeces * 365
            else -> 0f
        }
    }
    fun calcularGrasasTotales(): Float {
        val cantidad = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val grasasTotales = _grasasTotales.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidad * grasasTotales * numeroVeces
            "Semanal" -> cantidad * grasasTotales * numeroVeces * 7
            "Mensual" -> cantidad * grasasTotales * numeroVeces * 30
            "Anual" -> cantidad * grasasTotales * numeroVeces * 365
            else -> 0f
        }
    }

    fun calcularAlcohol(): Float {
        val cantidad = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val alcohol = _alcohol.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidad * alcohol * numeroVeces
            "Semanal" -> cantidad * alcohol * numeroVeces * 7
            "Mensual" -> cantidad * alcohol * numeroVeces * 30
            "Anual" -> cantidad * alcohol * numeroVeces * 365
            else -> 0f
        }
    }

    fun calcularColesterol(): Float {
        val cantidad = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val colesterol = _colesterol.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidad * colesterol * numeroVeces
            "Semanal" -> cantidad * colesterol * numeroVeces * 7
            "Mensual" -> cantidad * colesterol * numeroVeces * 30
            "Anual" -> cantidad * colesterol * numeroVeces * 365
            else -> 0f
        }
    }

    fun calcularFibra(): Float {
        val cantidad = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val fibra = _fibra.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidad * fibra * numeroVeces
            "Semanal" -> cantidad * fibra * numeroVeces * 7
            "Mensual" -> cantidad * fibra * numeroVeces * 30
            "Anual" -> cantidad * fibra * numeroVeces * 365
            else -> 0f
        }
    }
}