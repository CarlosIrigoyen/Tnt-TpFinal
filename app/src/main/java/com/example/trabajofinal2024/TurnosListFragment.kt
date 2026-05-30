package com.example.trabajofinal2024

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class TurnosListFragment : Fragment() {

    private lateinit var adapter: TurnosPendientesAdapter
    private var estado: String = "pendiente"
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

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

        recyclerView = view.findViewById(R.id.rvTurnos)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView.setPadding(4, 4, 4, 4)

        adapter = TurnosPendientesAdapter(
            onAsignar = { turno ->
                (requireParentFragment() as? TurnosAdminFragment)?.mostrarDialogoAsignar(turno)
            },
            onCancelar = { turno ->
                (requireParentFragment() as? TurnosAdminFragment)?.mostrarDialogoCancelar(turno)
            },
            esCancelable = { turno ->
                viewModel.esTurnoCancelable(turno.dia, turno.horario)
            }
        )
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.isLoadingVoluntarios.collect { isLoading ->
                if (isLoading) {
                    progressBar.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }

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
                "cancelado" -> {
                    viewModel.turnosCancelados.collect { turnos ->
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
                    else -> viewModel.turnosCancelados.value
                }
                adapter.submitList(turnos, mapa)
            }
        }
    }
}