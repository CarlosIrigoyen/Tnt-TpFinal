package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DuraznoViewModel: ViewModel() {
    private val _cantidad = MutableLiveData<String>()
    private val _numeroVeces = MutableLiveData<String>()
    private val _eventoEnviar = MutableLiveData<Boolean>(false)
    private val _alimento = MutableLiveData<String>("Durazno")
    private val _frecuencia = MutableLiveData<String>()

    val eventoEnviar: LiveData<Boolean>
        get() = _eventoEnviar


    val alimento: LiveData<String>
        get() = _alimento

    val cantidad: LiveData<String>
        get() = _cantidad


    val frecuencia: LiveData<String>
        get() = _frecuencia


    val numeroveces: LiveData<String>
        get() = _numeroVeces

    lateinit var cantidadList: MutableList<String>
        private set

    private fun elegirCantidad() {
        cantidadList = mutableListOf(
            "1",
            "2",
            "3",
            "4",
            "6"
        )
    }

    init {
        elegirCantidad()
    }

    fun setNumeroVeces(value: String) {
        _numeroVeces.value = value
    }

    fun setAlimento(value: String) {
        _alimento.value = value
    }

    fun setCantidad(value: String) {
        cantidadList.remove(value)
        cantidadList.add(0,value)
        _cantidad.value = value
    }

    fun setFrecuencia(value: String) {
        _frecuencia.value = value
    }

    fun enviar() {
        _eventoEnviar.value = true
    }
}