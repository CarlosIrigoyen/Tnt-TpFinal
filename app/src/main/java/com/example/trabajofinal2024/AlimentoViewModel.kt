package com.example.trabajofinal2024

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
//prueba de room
class AlimentoViewModel(private val repositorio: RepositorioAlimentos): ViewModel() {

    val allAlimentos: LiveData<List<Alimento>> = repositorio.allAlimentos.asLiveData()


    suspend fun insert(alimento:Alimento): Long {
        return repositorio.insert(alimento)
    }

    fun calcularValoresNutricionalesCompletos(
        alimentoBase: Alimento, // Valores por cada 100 de alimento
        gramosSeleccionados: Double,
        veces: Int,
        frecuenciaId: Int
    ): Alimento {

        // 1. Determinar el factor según la frecuencia
        val factorFrecuencia = when (frecuenciaId) {
            R.id.radioDiaria -> 1.0
            R.id.radioSemanal -> 1.0 / 7.0
            R.id.radioMensual -> 1.0 / 30.0
            R.id.radioAnual -> 1.0 / 365.0
            else -> 0.0
        }

        val multiplicador = (gramosSeleccionados / 100.0) * veces * factorFrecuencia
        val gramosDiarios = gramosSeleccionados * veces * factorFrecuencia
        return alimentoBase.copy(
            kcal = (alimentoBase.kcal * multiplicador).toFloat(),
            carbohidratos = (alimentoBase.carbohidratos * multiplicador).toFloat(),
            proteinas = (alimentoBase.proteinas * multiplicador).toFloat(),
            grasas = (alimentoBase.grasas * multiplicador).toFloat(),
            alcohol = (alimentoBase.alcohol * multiplicador).toFloat(),
            colesterol = (alimentoBase.colesterol * multiplicador).toFloat(),
            fibra = (alimentoBase.fibra * multiplicador).toFloat(),
            gramos = gramosDiarios.toFloat()
        )
    }

    suspend fun getAlimentoFirebase(uid: String, encuestaId: String, nombre: String) =
        repositorio.getAlimentoFirebase(uid, encuestaId, nombre)


    suspend fun update(alimento: Alimento) =
        repositorio.update(alimento)

    suspend fun getAlimentosPorEncuestaFirebase(uid:String, encuestaId: String): List<Alimento> {
        return repositorio.getAlimentosPorEncuestaFirebase(uid, encuestaId)
    }

    suspend fun guardarAlimentoEnFirebase(uid: String, encuestaId: String, alimento: Alimento) {
        repositorio.guardarAlimentoEnFirebase(uid, encuestaId, alimento)

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