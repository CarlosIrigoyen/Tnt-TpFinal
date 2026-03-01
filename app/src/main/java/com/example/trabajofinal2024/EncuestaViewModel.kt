package com.example.trabajofinal2024

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class EncuestaViewModel(private val repositorio: RepositorioEncuestas) : ViewModel() {

    val allEncuestas: LiveData<List<Encuesta>> = repositorio.allEncuestas.asLiveData()

    private val _encuestasFirebase = MutableLiveData<List<EncuestaFirestore>>()
    val encuestasFirebase: LiveData<List<EncuestaFirestore>> =
        _encuestasFirebase


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

    fun getEncuestas() =
        repositorio.getEncuestas().asLiveData()


    fun sincronizarDesdeFirestore(uid: String) {
        viewModelScope.launch {
            val result = FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .collection("encuestas")
                .get()
                .await()

            val lista = result.map { doc ->
                Log.d("SYNC_FIREBASE", "Doc ID: ${doc.id}")

                Encuesta(
                    firestoreId = doc.id,
                    domicilio = doc.getString("domicilio") ?: "",
                    ciudad = doc.getString("ciudad") ?: "",
                    lon = doc.getDouble("longitud") ?: 0.0,
                    lan = doc.getDouble("latitud") ?: 0.0,
                    userUid = uid,
                    currentIndex = (doc.getLong("currentIndex") ?: 0).toInt(),
                    activa = doc.getBoolean("activa") ?: true,
                    completa = doc.getBoolean("completa") ?: false,
                    updatedAt = doc.getLong("updatedAt") ?: 0L
                )
            }

            repositorio.deleteEncuestasPorUsuario(uid)

            repositorio.insertAll(lista)
        }
    }

    fun getEncuestaById(id: String) = repositorio.getEncuestaById(id).asLiveData()

    fun getEncuestasPorUsuario(uid: String) = repositorio.getEncuestasPorUsuario(uid).asLiveData()

    fun getPendientesPorUsuario(uid: String) = repositorio.getPendientesPorUsuario(uid).asLiveData()

    fun updateProgress(encuestaId: String, index: Int) = viewModelScope.launch {
        repositorio.updateProgress(encuestaId, index)
    }

    fun markCompleted(encuestaId: String, index: Int) = viewModelScope.launch {
        repositorio.markCompleted(encuestaId, index)
    }

    fun abandonEncuesta(encuestaId: String) = viewModelScope.launch {
        repositorio.abandonEncuesta(encuestaId)
    }

    // Nueva función para reanudar
    fun reanudarEncuesta(encuestaId: String) = viewModelScope.launch {
        repositorio.reanudarEncuesta(encuestaId)
    }


    fun cargarEncuestasCompletasFirebase() {
        repositorio.getEncuestasCompletasDesdeFirebase { lista ->
            _encuestasFirebase.postValue(lista)
        }
    }


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