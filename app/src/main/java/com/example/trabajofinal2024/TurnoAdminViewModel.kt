package com.example.trabajofinal2024

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TurnoAdminViewModel(
    private val repository: TurnoRepository
) : ViewModel() {

    private val _turnosPendientes = MutableStateFlow<List<TurnoEntity>>(emptyList())
    val turnosPendientes: StateFlow<List<TurnoEntity>> = _turnosPendientes.asStateFlow()

    private val _turnosConfirmados = MutableStateFlow<List<TurnoEntity>>(emptyList())
    val turnosConfirmados: StateFlow<List<TurnoEntity>> = _turnosConfirmados.asStateFlow()

    private val _turnosRechazados = MutableStateFlow<List<TurnoEntity>>(emptyList())
    val turnosRechazados: StateFlow<List<TurnoEntity>> = _turnosRechazados.asStateFlow()

    private val _voluntariosMap = MutableStateFlow<Map<String, VoluntarioInfo>>(emptyMap())
    val voluntariosMap: StateFlow<Map<String, VoluntarioInfo>> = _voluntariosMap.asStateFlow()

    // --- NUEVO: Estado de carga del mapa de voluntarios ---
    private val _isLoadingVoluntarios = MutableStateFlow(true)
    val isLoadingVoluntarios: StateFlow<Boolean> = _isLoadingVoluntarios.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getTurnosByEstado("pendiente").collect { _turnosPendientes.value = it }
        }
        viewModelScope.launch {
            repository.getTurnosByEstado("confirmado").collect { _turnosConfirmados.value = it }
        }
        viewModelScope.launch {
            repository.getTurnosByEstado("rechazado").collect { _turnosRechazados.value = it }
        }
        viewModelScope.launch {
            _isLoadingVoluntarios.value = true
            val mapa = repository.obtenerMapaVoluntarios()
            _voluntariosMap.value = mapa
            _isLoadingVoluntarios.value = false
        }
    }

    fun updateTurno(turno: TurnoEntity) {
        viewModelScope.launch {
            repository.actualizarTurno(turno)
        }
    }

    fun startListening() = repository.startListening()
    fun stopListening() = repository.stopListening()

    fun actualizarTurnosVencidos() {
        viewModelScope.launch {
            val hoy = Calendar.getInstance()
            hoy.set(Calendar.HOUR_OF_DAY, 0)
            hoy.set(Calendar.MINUTE, 0)
            hoy.set(Calendar.SECOND, 0)
            val confirmados = _turnosConfirmados.value
            val vencidos = confirmados.filter { turno ->
                try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaTurno = sdf.parse(turno.dia) ?: return@filter false
                    fechaTurno.before(hoy.time)
                } catch (e: Exception) { false }
            }
            vencidos.forEach { turno ->
                val actualizado = turno.copy(
                    estado = "rechazado",
                    descripcion = "Turno vencido (fecha pasada)",
                    updatedAt = System.currentTimeMillis()
                )
                repository.actualizarTurno(actualizado)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopListening()
    }

    private val _turnosNombre = MutableStateFlow<List<TurnoNombre>>(emptyList())
    val turnosNombre: StateFlow<List<TurnoNombre>> = _turnosNombre.asStateFlow()

    fun buscarTurnosConfirmados() {
        viewModelScope.launch {
            val turnos = repository.getTurnosConfirmados()
            _turnosNombre.value = turnos
        }
    }

    fun asignarTurno(turnoId: String) {
        viewModelScope.launch {
            repository.asignarTurno(turnoId)
        }
    }

    class TurnoAdminViewModelFactory(
        private val turnoRepository: TurnoRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TurnoAdminViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TurnoAdminViewModel(turnoRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}