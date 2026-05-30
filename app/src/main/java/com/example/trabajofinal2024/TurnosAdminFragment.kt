package com.example.trabajofinal2024

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.firestore.FirebaseFirestore
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
            TurnosListFragment().apply { arguments = Bundle().apply { putString("estado", "cancelado") } }
        )

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 3
            override fun createFragment(position: Int) = fragments[position]
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Pendientes"
                1 -> "Asignados"
                else -> "Cancelados"
            }
        }.attach()

        turnoAdminViewModel.startListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        turnoAdminViewModel.stopListening()
    }


    fun mostrarDialogoAsignar(turno: TurnoEntity) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_asignar_turno, null)
        val etFecha = dialogView.findViewById<EditText>(R.id.etFecha)
        val etHorario = dialogView.findViewById<EditText>(R.id.etHorario)
        val etDireccion = dialogView.findViewById<EditText>(R.id.etDireccion)
        val etDescripcion = dialogView.findViewById<EditText>(R.id.etDescripcion)

        etFecha.setOnClickListener {
            val fechasCompletas = turnoAdminViewModel.getFechasCompletas()

            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(object : CalendarConstraints.DateValidator {
                    override fun isValid(date: Long): Boolean {
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = date
                        val dow = cal.get(Calendar.DAY_OF_WEEK)

                        if (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY) return false

                        val calHoy = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = MaterialDatePicker.todayInUtcMilliseconds()
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val calFecha = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            timeInMillis = date
                        }
                        if (calFecha.before(calHoy)) return false

                        val fechaStr = String.format(
                            "%02d/%02d/%d",
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.YEAR)
                        )

                        if (fechaStr in fechasCompletas) return false

                        val calHoyLocal = Calendar.getInstance()
                        val esHoy = cal.get(Calendar.DAY_OF_MONTH) == calHoyLocal.get(Calendar.DAY_OF_MONTH) &&
                                cal.get(Calendar.MONTH) == calHoyLocal.get(Calendar.MONTH) &&
                                cal.get(Calendar.YEAR) == calHoyLocal.get(Calendar.YEAR)

                        if (esHoy) {
                            val horaActual = calHoyLocal.get(Calendar.HOUR_OF_DAY)
                            val minutoActual = calHoyLocal.get(Calendar.MINUTE)
                            if (horaActual > 16 || (horaActual == 16 && minutoActual > 0)) return false
                        }
                        return true
                    }

                    override fun describeContents() = 0
                    override fun writeToParcel(dest: android.os.Parcel, flags: Int) {}
                })

            val picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Seleccionar fecha")
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

            picker.addOnPositiveButtonClickListener { selection ->
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                cal.timeInMillis = selection
                val fecha = String.format(
                    "%02d/%02d/%d",
                    cal.get(Calendar.DAY_OF_MONTH),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.YEAR)
                )
                etFecha.setText(fecha)
                etHorario.isEnabled = true
                etHorario.setText("")
            }

            picker.show(parentFragmentManager, "date_picker")
        }

        etHorario.setOnClickListener {
            val fechaSeleccionada = etFecha.text.toString().trim()
            if (fechaSeleccionada.isEmpty()) {
                Toast.makeText(requireContext(), "Primero seleccioná una fecha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ocupados = turnoAdminViewModel.getHorariosOcupados(fechaSeleccionada)

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaHoy = sdf.format(Date())
            val slotsPasados = if (fechaSeleccionada == fechaHoy) {
                val ahora = Calendar.getInstance()
                val horaActualInt = ahora.get(Calendar.HOUR_OF_DAY)
                val minutoActualInt = ahora.get(Calendar.MINUTE)

                val todosSlots = mutableListOf<String>()
                for (hour in 8..15) {
                    for (minute in listOf(0, 20, 40)) {
                        todosSlots.add(String.format("%02d:%02d", hour, minute))
                    }
                }
                todosSlots.add("16:00")

                todosSlots.filter { slot ->
                    val partes = slot.split(":")
                    val slotHora = partes[0].toInt()
                    val slotMinuto = partes[1].toInt()
                    slotHora < horaActualInt || (slotHora == horaActualInt && slotMinuto <= minutoActualInt)
                }.toSet()
            } else {
                emptySet()
            }

            val todosLosSlots = mutableListOf<String>()
            for (hour in 8..15) {
                for (minute in listOf(0, 20, 40)) {
                    todosLosSlots.add(String.format("%02d:%02d", hour, minute))
                }
            }
            todosLosSlots.add("16:00")

            val slotsDisponibles = todosLosSlots.filter { it !in ocupados && it !in slotsPasados }

            if (slotsDisponibles.isEmpty()) {
                Toast.makeText(requireContext(), "No hay horarios disponibles para esa fecha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val horasDisponibles = slotsDisponibles
                .map { it.split(":")[0].toInt() }
                .distinct()
                .sorted()

            var horaActual = horasDisponibles.first()

            fun getMinutosParaHora(hora: Int): Array<String> {
                return slotsDisponibles
                    .filter { it.split(":")[0].toInt() == hora }
                    .map { it.split(":")[1] }
                    .toTypedArray()
            }

            val dialogView = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER
                setPadding(48, 32, 48, 32)
            }

            val npHora = NumberPicker(requireContext()).apply {
                minValue = 0
                maxValue = horasDisponibles.size - 1
                displayedValues = horasDisponibles.map { String.format("%02d", it) }.toTypedArray()
                value = 0
            }

            val separador = android.widget.TextView(requireContext()).apply {
                text = ":"
                textSize = 28f
                setPadding(16, 0, 16, 0)
                gravity = android.view.Gravity.CENTER
            }

            var minutosActuales = getMinutosParaHora(horaActual)
            val npMinuto = NumberPicker(requireContext()).apply {
                minValue = 0
                maxValue = minutosActuales.size - 1
                displayedValues = minutosActuales
                value = 0
            }

            npHora.setOnValueChangedListener { _, _, newIdx ->
                horaActual = horasDisponibles[newIdx]
                minutosActuales = getMinutosParaHora(horaActual)
                npMinuto.displayedValues = null
                npMinuto.minValue = 0
                npMinuto.maxValue = minutosActuales.size - 1
                npMinuto.displayedValues = minutosActuales
                npMinuto.value = 0
            }

            dialogView.addView(npHora)
            dialogView.addView(separador)
            dialogView.addView(npMinuto)

            AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar horario")
                .setView(dialogView)
                .setPositiveButton("Confirmar") { _, _ ->
                    val hora = horasDisponibles[npHora.value]
                    val minuto = minutosActuales[npMinuto.value]
                    etHorario.setText(String.format("%02d:%02d", hora, minuto.toInt()))
                }
                .setNegativeButton("Cancelar", null)
                .show()
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
                if (fechaObj == null) {
                    Toast.makeText(requireContext(), "Fecha inválida", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val hoy = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                if (fechaObj.before(hoy)) {
                    Toast.makeText(requireContext(), "Fecha pasada", Toast.LENGTH_SHORT).show()
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

    fun mostrarDialogoCancelar(turno: TurnoEntity) {
        if (turno.estado != "confirmado" || !turnoAdminViewModel.esTurnoCancelable(turno.dia, turno.horario)) {
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
                    estado = "cancelado",
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