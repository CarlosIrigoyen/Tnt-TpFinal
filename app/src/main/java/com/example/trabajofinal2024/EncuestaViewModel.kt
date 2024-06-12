package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EncuestaViewModel(private val repositorio: RepositorioEncuestas): ViewModel() {

    val allEncuestas: LiveData<List<Encuesta>> = repositorio.allEncuestas.asLiveData()

    fun insert(encuesta:Encuesta) = viewModelScope.launch {
        repositorio.insert(encuesta)
    }

    private val _cantidad = MutableLiveData<String>()
    private val _numeroVeces = MutableLiveData<String>()
    private val _alimento = MutableLiveData<String>("Yogurt Bebible")
    private val _frecuencia = MutableLiveData<String>()

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
}

class EncuestaViewModelFactory(private val repositorio: RepositorioEncuestas) : ViewModelProvider.Factory {
    override fun <T: ViewModel> create (modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(EncuestaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EncuestaViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}