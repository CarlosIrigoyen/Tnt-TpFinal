package com.example.trabajofinal2024

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class EncuestaViewModel(private val repositorio: RepositorioEncuestas) : ViewModel() {

    val allEncuestas: LiveData<List<Encuesta>> = repositorio.allEncuestas.asLiveData()

    // LiveData antiguo (ya no se usa, pero se mantiene por si acaso)
    private val _encuestasFirebase = MutableLiveData<List<EncuestaFirestore>>()
    val encuestasFirebase: LiveData<List<EncuestaFirestore>> = _encuestasFirebase

    // NUEVO: LiveData que viene del repositorio con escucha en tiempo real
    val encuestasFirestoreLiveData: LiveData<List<EncuestaFirestore>> = repositorio.encuestasFirestoreLiveData

    private val _encuestaId = MutableLiveData<Int>()
    val encuestaId: LiveData<Int> get() = _encuestaId

    fun insert(encuesta: Encuesta) {
        viewModelScope.launch {
            val id = repositorio.insert(encuesta)
            _encuestaId.value = id.toInt()
        }
    }

    fun update(encuesta: Encuesta) = viewModelScope.launch {
        repositorio.update(encuesta)
    }

    fun getEncuestas() = repositorio.getEncuestas().asLiveData()

    fun getEncuestaById(id: Int) = repositorio.getEncuestaById(id).asLiveData()

    fun getEncuestasPorUsuario(uid: String) = repositorio.getEncuestasPorUsuario(uid).asLiveData()

    fun getPendientesPorUsuario(uid: String) = repositorio.getPendientesPorUsuario(uid).asLiveData()

    fun updateProgress(encuestaId: Int, index: Int) = viewModelScope.launch {
        repositorio.updateProgress(encuestaId, index)
    }

     suspend fun markCompleted(encuesta: Encuesta) {
         repositorio.markCompleted(encuesta)
     }
    fun abandonEncuesta(encuestaId: Int) = viewModelScope.launch {
        repositorio.abandonEncuesta(encuestaId)
    }

    fun reanudarEncuesta(encuestaId: Int) = viewModelScope.launch {
        repositorio.reanudarEncuesta(encuestaId)
    }

    fun startListeningFirestore(userUid: String) {
        repositorio.listenEncuestasFromFirestore(userUid)
    }

    fun guardarAlimentoFirebase(encuesta: Encuesta, alimento: Alimento) {
        viewModelScope.launch {
            repositorio.guardarAlimentoEnFirebase(encuesta, alimento)
        }
    }


    // Método obsoleto eliminado
    // fun cargarEncuestasCompletasFirebase() { ... }

    class EncuestaViewModelFactory(private val repositorio: RepositorioEncuestas) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EncuestaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return EncuestaViewModel(repositorio) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida")
        }
    }
}