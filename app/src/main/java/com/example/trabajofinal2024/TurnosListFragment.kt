package com.example.trabajofinal2024

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class TurnosListFragment : Fragment() {

    private lateinit var adapter: TurnosPendientesAdapter
    private var estado: String = "pendiente"

    private val viewModel: TurnoAdminViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            estado = it.getString("estado", "pendiente") ?: "pendiente"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_turnos_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvTurnos)
        // Grid de 3 columnas (sin cambios)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.setPadding(4, 4, 4, 4)

        adapter = TurnosPendientesAdapter(
            onAsignar = { turno ->
                (requireParentFragment() as? TurnosAdminFragment)?.mostrarDialogoAsignar(turno)
            },
            onCancelar = { turno ->
                (requireParentFragment() as? TurnosAdminFragment)?.mostrarDialogoCancelar(turno)
            }
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            when (estado) {
                "pendiente" -> {
                    viewModel.turnosPendientes.collect { turnos ->
                        adapter.submitList(turnos, viewModel.voluntariosMap.value)
                    }
                }
                "confirmado" -> {
                    viewModel.turnosConfirmados.collect { turnos ->
                        adapter.submitList(turnos, viewModel.voluntariosMap.value)
                    }
                }
                "rechazado" -> {
                    viewModel.turnosRechazados.collect { turnos ->
                        adapter.submitList(turnos, viewModel.voluntariosMap.value)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.voluntariosMap.collect { mapa ->
                val turnos = when (estado) {
                    "pendiente" -> viewModel.turnosPendientes.value
                    "confirmado" -> viewModel.turnosConfirmados.value
                    else -> viewModel.turnosRechazados.value
                }
                adapter.submitList(turnos, mapa)
            }
        }
    }
}