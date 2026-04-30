package com.example.trabajofinal2024

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TurnosAdminFragment : Fragment() {

    private val turnoAdminViewModel: TurnoAdminViewModel by viewModels {
        TurnoAdminViewModel.TurnoAdminViewModelFactory(
            TurnoRepository(
                AppDatabase.getDatabase(requireContext(), lifecycleScope).turnoDao(),
                FirebaseFirestore.getInstance()
            )
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_turnos_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = view.findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayout)

        val fragments = listOf(
            TurnosListFragment().apply { arguments = Bundle().apply { putString("estado", "pendiente") } },
            TurnosListFragment().apply { arguments = Bundle().apply { putString("estado", "confirmado") } },
            TurnosListFragment().apply { arguments = Bundle().apply { putString("estado", "rechazado") } }
        )

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 3
            override fun createFragment(position: Int) = fragments[position]
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Pendientes"
                1 -> "Asignados"
                else -> "Historial"
            }
        }.attach()

        turnoAdminViewModel.startListening()
        turnoAdminViewModel.actualizarTurnosVencidos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        turnoAdminViewModel.stopListening()
    }

    private fun isFechaFutura(fechaStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaTurno = sdf.parse(fechaStr) ?: return false
            fechaTurno.after(Date())
        } catch (e: Exception) { false }
    }

    // ===============================
    // ASIGNAR TURNO (con notificación)
    // ===============================
    fun mostrarDialogoAsignar(turno: TurnoEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_asignar_turno, null)
        val etFecha = dialogView.findViewById<EditText>(R.id.etFecha)
        val etHorario = dialogView.findViewById<EditText>(R.id.etHorario)
        val etDireccion = dialogView.findViewById<EditText>(R.id.etDireccion)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.etDescripcion)

        etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(),
                { _, year, month, dayOfMonth ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, dayOfMonth)
                    if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                        cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        Toast.makeText(requireContext(), "Solo lunes a viernes", Toast.LENGTH_SHORT).show()
                        return@DatePickerDialog
                    }
                    val hoy = Calendar.getInstance()
                    hoy.set(Calendar.HOUR_OF_DAY, 0)
                    hoy.set(Calendar.MINUTE, 0)
                    hoy.set(Calendar.SECOND, 0)
                    if (cal.before(hoy)) {
                        Toast.makeText(requireContext(), "No se permiten fechas pasadas", Toast.LENGTH_SHORT).show()
                        return@DatePickerDialog
                    }
                    val fecha = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                    etFecha.setText(fecha)
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis()
            datePicker.show()
        }

        etHorario.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(requireContext(),
                { _, hourOfDay, minute ->
                    if (hourOfDay < 8 || hourOfDay > 16) {
                        Toast.makeText(requireContext(), "Horario permitido: 08:00 a 16:00", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }
                    if (hourOfDay == 16 && minute > 0) {
                        Toast.makeText(requireContext(), "El último horario válido es 16:00", Toast.LENGTH_SHORT).show()
                        return@TimePickerDialog
                    }
                    val roundedMinute = (minute / 20) * 20
                    val horaFormateada = String.format("%02d:%02d", hourOfDay, roundedMinute)
                    etHorario.setText(horaFormateada)
                },
                now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true
            ).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Asignar turno")
            .setView(dialogView)
            .setPositiveButton("Confirmar") { _, _ ->
                val fecha = etFecha.text.toString().trim()
                val horario = etHorario.text.toString().trim()
                val direccion = etDireccion.text.toString().trim()
                val descripcion = etDescripcion.text.toString().trim()

                if (fecha.isEmpty() || horario.isEmpty() || direccion.isEmpty()) {
                    Toast.makeText(requireContext(), "Complete fecha, horario y dirección", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val fechaObj = try { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha) } catch (e: Exception) { null }
                if (fechaObj == null || fechaObj.before(Date())) {
                    Toast.makeText(requireContext(), "Fecha inválida o pasada", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val cal = Calendar.getInstance().apply { time = fechaObj }
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                    Toast.makeText(requireContext(), "La fecha debe ser lunes a viernes", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val yaAsignado = turnoAdminViewModel.turnosConfirmados.value.any {
                    it.dia == fecha && it.horario == horario
                }
                if (yaAsignado) {
                    Toast.makeText(requireContext(), "Ese horario ya está ocupado", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val turnoActualizado = turno.copy(
                    estado = "confirmado",
                    dia = fecha,
                    horario = horario,
                    direccion = direccion,
                    descripcion = descripcion,
                    updatedAt = System.currentTimeMillis()
                )
                turnoAdminViewModel.updateTurno(turnoActualizado)

                val infoVoluntario = turnoAdminViewModel.voluntariosMap.value[turno.uidVoluntario]
                val nombreCompleto = infoVoluntario?.nombre ?: "Voluntario"

                NtfyHelper.enviarNotificacion(
                    context = requireContext(),
                    uid = turno.uidVoluntario,
                    nombreVoluntario = nombreCompleto,
                    titulo = "✅ Turno asignado",
                    mensaje = "Tu turno ha sido confirmado para el día $fecha a las $horario.\n📍 Dirección: $direccion",
                    esAsignacion = true
                )

                Toast.makeText(requireContext(), "Turno asignado correctamente", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ===============================
    // CANCELAR TURNO (con notificación)
    // ===============================
    fun mostrarDialogoCancelar(turno: TurnoEntity) {
        if (turno.estado != "confirmado" || !isFechaFutura(turno.dia)) {
            Toast.makeText(requireContext(), "Este turno no se puede cancelar", Toast.LENGTH_SHORT).show()
            return
        }

        val input = EditText(requireContext())
        input.hint = "Motivo de cancelación"

        AlertDialog.Builder(requireContext())
            .setTitle("Cancelar turno")
            .setView(input)
            .setPositiveButton("Confirmar") { _, _ ->
                val motivo = input.text.toString().trim()
                if (motivo.isEmpty()) {
                    Toast.makeText(requireContext(), "Ingrese un motivo", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val turnoActualizado = turno.copy(
                    estado = "rechazado",
                    descripcion = motivo,
                    updatedAt = System.currentTimeMillis()
                )
                turnoAdminViewModel.updateTurno(turnoActualizado)

                val infoVoluntario = turnoAdminViewModel.voluntariosMap.value[turno.uidVoluntario]
                val nombreCompleto = infoVoluntario?.nombre ?: "Voluntario"

                NtfyHelper.enviarNotificacion(
                    context = requireContext(),
                    uid = turno.uidVoluntario,
                    nombreVoluntario = nombreCompleto,
                    titulo = "❌ Turno cancelado",
                    mensaje = "Lamentamos informarte que tu turno ha sido cancelado.\n📝 Motivo: $motivo",
                    esAsignacion = false
                )

                Toast.makeText(requireContext(), "Turno cancelado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Volver", null)
            .show()
    }
}