package com.example.trabajofinal2024

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TurnosPendientesAdapter(
    private val onAsignar: (TurnoEntity) -> Unit,
    private val onCancelar: (TurnoEntity) -> Unit
) : RecyclerView.Adapter<TurnosPendientesAdapter.ViewHolder>() {

    private var turnos = listOf<TurnoEntity>()
    private var voluntariosInfo = mapOf<String, VoluntarioInfo>()

    fun submitList(nuevosTurnos: List<TurnoEntity>, nuevoMapa: Map<String, VoluntarioInfo>) {
        turnos = nuevosTurnos
        voluntariosInfo = nuevoMapa
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_turno_pendiente, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val turno = turnos[position]
        val info = voluntariosInfo[turno.uidVoluntario]
        holder.bind(turno, info, onAsignar, onCancelar)
    }

    override fun getItemCount() = turnos.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNombreApellido: TextView = itemView.findViewById(R.id.tvNombreApellido)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvTelefono: TextView = itemView.findViewById(R.id.tvTelefono)
        private val tvFechaNac: TextView = itemView.findViewById(R.id.tvFechaNac)
        private val tvFechaTurno: TextView = itemView.findViewById(R.id.tvFechaTurno)
        private val tvHorario: TextView = itemView.findViewById(R.id.tvHorario)
        private val tvMotivo: TextView = itemView.findViewById(R.id.tvMotivo)
        private val btnAsignar: Button = itemView.findViewById(R.id.btnAsignar)
        private val btnCancelar: Button = itemView.findViewById(R.id.btnCancelar)

        fun bind(turno: TurnoEntity, info: VoluntarioInfo?, onAsignar: (TurnoEntity) -> Unit, onCancelar: (TurnoEntity) -> Unit) {
            if (info != null) {
                tvNombreApellido.text = "${info.nombre} ${info.apellido}"
                tvEmail.text = "Email: ${info.email}"
                tvFechaNac.text = "Fecha de nac.: ${info.fechaNacimiento}"
                tvTelefono.text = if (info.telefono.isNotEmpty()) {
                    "Teléfono: ${info.telefono}"
                } else {
                    "Teléfono: no disponible"
                }
            } else {
                tvNombreApellido.text = "Cargando voluntario..."
                tvEmail.text = ""
                tvFechaNac.text = ""
                tvTelefono.text = ""
            }

            tvFechaTurno.text = "Fecha turno: ${turno.dia}"
            tvHorario.text = "Horario: ${turno.horario}"

            // Botón Asignar: solo visible para pendientes
            if (turno.estado == "pendiente") {
                btnAsignar.visibility = View.VISIBLE
                btnAsignar.isEnabled = true
                btnAsignar.setOnClickListener { onAsignar(turno) }
            } else {
                btnAsignar.visibility = View.GONE
            }

            // Botón Cancelar: solo confirmados con fecha futura
            if (turno.estado == "confirmado" && isFechaFutura(turno.dia)) {
                btnCancelar.visibility = View.VISIBLE
                btnCancelar.isEnabled = true
                btnCancelar.setOnClickListener { onCancelar(turno) }
            } else {
                btnCancelar.visibility = View.GONE
            }

            // Mostrar motivo si el turno está cancelado (estado "rechazado")
            if (turno.estado == "rechazado" && turno.descripcion.isNotEmpty()) {
                tvMotivo.visibility = View.VISIBLE
                tvMotivo.text = "Motivo: ${turno.descripcion}"
            } else {
                tvMotivo.visibility = View.GONE
            }
        }

        private fun isFechaFutura(fechaStr: String): Boolean {
            return try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val fechaTurno = sdf.parse(fechaStr) ?: return false
                fechaTurno.after(Date())
            } catch (e: Exception) { false }
        }
    }
}