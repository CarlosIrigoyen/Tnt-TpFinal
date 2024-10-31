package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class EncuestaViewModel(private val repositorio: RepositorioEncuestas): ViewModel() {

    val allEncuestas: LiveData<List<Encuesta>> = repositorio.allEncuestas.asLiveData()
    private val _encuestaId = MutableLiveData<Int>()
    val encuestaId: LiveData <Int>
        get() = _encuestaId

    fun insert(encuesta:Encuesta) {
        viewModelScope.launch{
            val id = repositorio.insert(encuesta)
            _encuestaId.value = id.toInt()
        }
    }


    fun update(encuesta: Encuesta) = viewModelScope.launch {
        repositorio.update(encuesta)
    }

    fun getEncuestaById(id: Int): LiveData<Encuesta> {
        return repositorio.getEncuestaById(id).asLiveData()
    }



    private val _domicilio = MutableLiveData<String>()
    private val _ciudad = MutableLiveData<String>()
    private val _completada = MutableLiveData<Boolean>(false)

    val domicilio: LiveData<String>
        get() = _domicilio

    val ciudad: LiveData<String>
        get() = _ciudad


    val completada: LiveData<Boolean>
        get() = _completada

    class EncuestaViewModelFactory(private val repositorio: RepositorioEncuestas) : ViewModelProvider.Factory {
        override fun <T: ViewModel> create (modelClass: Class<T>): T {
            if(modelClass.isAssignableFrom(EncuestaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EncuestaViewModel(repositorio) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida")
        }
    }


}

