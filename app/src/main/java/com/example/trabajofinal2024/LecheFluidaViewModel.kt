package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LecheFluidaViewModel: ViewModel() {
    private val _cantidad = MutableLiveData<String>()
    private val _numeroVeces = MutableLiveData<String>()
    private val _categoria = MutableLiveData<String>("Leche y Yogurt")
    private val _alimento = MutableLiveData<String>("Leche fluida entera")
    private  val _gramos = MutableLiveData<Float> (100.0f)
    private val _kilocal = MutableLiveData<Float> (57.9f)
    private val _carbohidratos = MutableLiveData<Float> (4.6f)
    private val _proteinas = MutableLiveData<Float> (3.1f)
    private val _grasasTotales = MutableLiveData<Float> (3.0f)
    private val _alcohol = MutableLiveData<Float> (0.0f)
    private val _colesterol = MutableLiveData<Float> (10.1f)
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

    var encuestaId: Int = 0
        private set

    fun setEncuestaId(id: Int) {
        encuestaId = id
    }


    val frecuencia: LiveData<String>
        get() = _frecuencia


    val numeroveces: LiveData<String>
        get() = _numeroVeces

    lateinit var cantidadList: MutableList<String>
        private set

    private fun elegirCantidad() {
        cantidadList = mutableListOf(
            "250",
            "500",
            "750",
            "1000",
            "2000"
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
        val cantidadEnMl = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val gramosPorMl = _gramos.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidadEnMl * gramosPorMl * numeroVeces
            "Semanal" -> cantidadEnMl * gramosPorMl * numeroVeces * 7
            "Mensual" -> cantidadEnMl * gramosPorMl * numeroVeces * 30
            "Anual" -> cantidadEnMl * gramosPorMl * numeroVeces * 365
            else -> 0f
        }
    }

    fun calcularKcal(): Float {
        val cantidadEnMl = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val kcal = _kilocal.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidadEnMl * kcal * numeroVeces
            "Semanal" -> cantidadEnMl * kcal * numeroVeces * 7
            "Mensual" -> cantidadEnMl * kcal * numeroVeces * 30
            "Anual" -> cantidadEnMl * kcal * numeroVeces * 365
            else -> 0f
        }
    }

    fun calcularCarbohidratos(): Float {
        val cantidadEnMl = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val carbohidratos = _carbohidratos.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidadEnMl * carbohidratos * numeroVeces
            "Semanal" -> cantidadEnMl * carbohidratos * numeroVeces * 7
            "Mensual" -> cantidadEnMl * carbohidratos * numeroVeces * 30
            "Anual" -> cantidadEnMl * carbohidratos * numeroVeces * 365
            else -> 0f
        }
    }
    fun calcularProteinas(): Float {
        val cantidadEnMl = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val proteinas = _proteinas.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidadEnMl * proteinas * numeroVeces
            "Semanal" -> cantidadEnMl * proteinas * numeroVeces * 7
            "Mensual" -> cantidadEnMl * proteinas * numeroVeces * 30
            "Anual" -> cantidadEnMl * proteinas * numeroVeces * 365
            else -> 0f
        }
    }
    fun calcularGrasasTotales(): Float {
        val cantidadEnMl = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val grasasTotales = _grasasTotales.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidadEnMl * grasasTotales * numeroVeces
            "Semanal" -> cantidadEnMl * grasasTotales * numeroVeces * 7
            "Mensual" -> cantidadEnMl * grasasTotales * numeroVeces * 30
            "Anual" -> cantidadEnMl * grasasTotales * numeroVeces * 365
            else -> 0f
        }
    }

    fun calcularAlcohol(): Float {
        val cantidadEnMl = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val alcohol = _alcohol.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidadEnMl * alcohol * numeroVeces
            "Semanal" -> cantidadEnMl * alcohol * numeroVeces * 7
            "Mensual" -> cantidadEnMl * alcohol * numeroVeces * 30
            "Anual" -> cantidadEnMl * alcohol * numeroVeces * 365
            else -> 0f
        }
    }

    fun calcularColesterol(): Float {
        val cantidadEnMl = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val colesterol = _colesterol.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidadEnMl * colesterol * numeroVeces
            "Semanal" -> cantidadEnMl * colesterol * numeroVeces * 7
            "Mensual" -> cantidadEnMl * colesterol * numeroVeces * 30
            "Anual" -> cantidadEnMl * colesterol * numeroVeces * 365
            else -> 0f
        }
    }

    fun calcularFibra(): Float {
        val cantidadEnMl = _cantidad.value?.toFloatOrNull() ?: 0f
        val numeroVeces = _numeroVeces.value?.toFloatOrNull() ?: 0f
        val fibra = _fibra.value ?: 0f

        return when (_frecuencia.value) {
            "Diaria" -> cantidadEnMl * fibra * numeroVeces
            "Semanal" -> cantidadEnMl * fibra * numeroVeces * 7
            "Mensual" -> cantidadEnMl * fibra * numeroVeces * 30
            "Anual" -> cantidadEnMl * fibra * numeroVeces * 365
            else -> 0f
        }
    }
}