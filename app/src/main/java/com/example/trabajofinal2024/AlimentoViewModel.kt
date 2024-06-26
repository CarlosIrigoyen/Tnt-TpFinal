package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AlimentoViewModel(private val repositorio: RepositorioAlimentos): ViewModel() {

    val allAlimentos: LiveData<List<Alimento>> = repositorio.allAlimentos.asLiveData()


    fun insert(alimento:Alimento) = viewModelScope.launch {
        repositorio.insert(alimento)
    }



    class AlimentoViewModelFactory(private val repositorio: RepositorioAlimentos) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AlimentoViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AlimentoViewModel(repositorio) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida")
        }
    }

}