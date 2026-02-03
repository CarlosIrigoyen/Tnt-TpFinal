package com.example.trabajofinal2024

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.trabajofinal2024.databinding.FragmentFoodBinding
import kotlinx.coroutines.launch

class FoodFragment : Fragment(R.layout.fragment_food) {

    private lateinit var binding: FragmentFoodBinding
    private val alimentoViewModel: AlimentoViewModel by viewModels {
        AlimentoViewModel.AlimentoViewModelFactory((activity?.application as App).alimentoRepositorio)
    }
    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory((activity?.application as App).encuestaRepositorio)
    }

    private var encuestaId: Int = 0
    private var currentIndex: Int = 0
    private var foodItem: FoodItem? = null
    private var encuestaCompletada: Boolean = false

    private val cantidadOpciones = arrayOf("50", "100", "150", "200", "250", "300", "350", "400", "450", "500")

    companion object {
        private const val STATE_INDEX = "state_current_index"
        private const val ARG_ENCUESTA_ID = "encuestaid"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { encuestaId = it.getInt(ARG_ENCUESTA_ID, 0) }
        currentIndex = savedInstanceState?.getInt(STATE_INDEX) ?: 0

        val total = FoodCatalog.ALL.size
        if (total == 0) currentIndex = 0
        if (currentIndex < 0) currentIndex = 0
        if (currentIndex >= total) currentIndex = 0

        if (encuestaId > 0) {
            encuestaViewModel.getEncuestaById(encuestaId).observe(this) { encuesta ->
                encuesta?.let {
                    encuestaCompletada = it.completa

                    if (it.completa) {
                        // Encuesta ya completada
                        Toast.makeText(
                            requireContext(),
                            "Esta encuesta ya fue completada.",
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().popBackStack()
                        return@observe
                    }

                    // Si está abandonada, mostramos mensaje
                    if (!it.activa) {
                        Toast.makeText(
                            requireContext(),
                            "Reanudando encuesta abandonada...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    currentIndex = it.currentIndex.coerceIn(0, FoodCatalog.ALL.size)
                    setFoodAtIndex(currentIndex)

                    // Mostrar progreso
                    binding.progresoText.text = "Alimento ${currentIndex + 1} de ${FoodCatalog.ALL.size}"
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_INDEX, currentIndex)
    }

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): android.view.View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_food, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        setFoodAtIndex(currentIndex)
        return binding.root
    }

    private fun setFoodAtIndex(index: Int) {
        val list = FoodCatalog.ALL
        if (list.isEmpty()) {
            Toast.makeText(context, "Catálogo vacío", Toast.LENGTH_LONG).show()
            return
        }
        val safeIndex = index.coerceIn(0, list.size - 1)
        val template = list[safeIndex]
        foodItem = FoodItem(template)
        binding.foodItem = foodItem

        binding.vecesInput.setText(foodItem?.numeroveces?.value ?: "1")

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cantidadOpciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOpciones.adapter = adapter

        val cantidadGuardada = foodItem?.cantidad?.value
        val posicion = if (cantidadGuardada != null) {
            cantidadOpciones.indexOf(cantidadGuardada).coerceAtLeast(0)
        } else {
            1
        }
        binding.spinnerOpciones.setSelection(posicion)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar texto de progreso
        binding.progresoText.text = "Alimento ${currentIndex + 1} de ${FoodCatalog.ALL.size}"

        configurarNumeroVeces()
        configurarSpinner()

        binding.increment.setOnClickListener {
            val current = binding.spinnerOpciones.selectedItemPosition
            val next = (current + 1).coerceAtMost(cantidadOpciones.size - 1)
            binding.spinnerOpciones.setSelection(next)
        }

        binding.decrement.setOnClickListener {
            val current = binding.spinnerOpciones.selectedItemPosition
            val prev = (current - 1).coerceAtLeast(0)
            binding.spinnerOpciones.setSelection(prev)
        }

        binding.frecuenciaGroup.setOnCheckedChangeListener { _, checkedId ->
            val f = when (checkedId) {
                R.id.radioDiaria -> "Diaria"
                R.id.radioSemanal -> "Semanal"
                R.id.radioMensual -> "Mensual"
                R.id.radioAnual -> "Anual"
                R.id.radioNunca -> "Nunca"
                else -> ""
            }
            foodItem?.frecuencia?.value = f
        }

        binding.siguienteAlimento.setOnClickListener { saveCurrentAlimentoAndAdvance() }

        binding.cancelarEncuesta.setOnClickListener {
            // Solo regresa sin abandonar
            findNavController().popBackStack()
        }

        binding.abandonarEncuesta.setOnClickListener {
            if (encuestaId > 0) {
                lifecycleScope.launch {
                    encuestaViewModel.abandonEncuesta(encuestaId)
                    Toast.makeText(
                        requireContext(),
                        "Encuesta abandonada. Puedes reanudarla más tarde desde la lista.",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun configurarNumeroVeces() {
        binding.vecesInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                foodItem?.numeroveces?.value = s?.toString() ?: "1"
            }
        })
        binding.vecesInput.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.all { it.isDigit() }) null else ""
        })
    }

    private fun configurarSpinner() {
        binding.spinnerOpciones.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val valor = parent?.getItemAtPosition(position)?.toString() ?: "100"
                foodItem?.cantidad?.value = valor
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun saveCurrentAlimentoAndAdvance() {
        val item = foodItem ?: return

        val gramos = item.calcularGramosTotales()
        val kcal = item.calcularKcal()
        val carbo = item.calcularCarbohidratos()
        val prote = item.calcularProteinas()
        val grasas = item.calcularGrasasTotales()
        val alcohol = item.calcularAlcohol()
        val colesterol = item.calcularColesterol()
        val fibra = item.calcularFibra()

        lifecycleScope.launch {
            try {
                alimentoViewModel.insert(
                    Alimento(
                        encuestaId = encuestaId,
                        nombre_alimento = item.alimentoNombre,
                        categoria = item.categoria,
                        cantidad_alimento = item.cantidad.value ?: "100",
                        numero_veces = item.numeroveces.value ?: "1",
                        frecuencia_veces = item.frecuencia.value ?: "",
                        gramos = gramos,
                        kcal = kcal,
                        carbohidratos = carbo,
                        proteinas = prote,
                        grasas = grasas,
                        alcohol = alcohol,
                        colesterol = colesterol,
                        fibra = fibra
                    )
                )

                currentIndex += 1
                // Actualizar progreso
                encuestaViewModel.updateProgress(encuestaId, currentIndex)

                // Actualizar texto de progreso
                binding.progresoText.text = "Alimento ${currentIndex + 1} de ${FoodCatalog.ALL.size}"

                if (currentIndex >= FoodCatalog.ALL.size) {
                    // Encuesta completada
                    encuestaViewModel.markCompleted(encuestaId, currentIndex)
                    Toast.makeText(
                        context,
                        "¡Encuesta completada! Has registrado todos los alimentos.",
                        Toast.LENGTH_LONG
                    ).show()
                    findNavController().popBackStack()
                    return@launch
                } else {
                    // Continuar con siguiente alimento
                    setFoodAtIndex(currentIndex)
                    binding.frecuenciaGroup.clearCheck()
                    // Mantener el foco en el siguiente alimento
                    binding.vecesInput.requestFocus()
                }
            } catch (e: Exception) {
                Log.e("FoodFragment", "Error insertando alimento: ${e.message}")
                Toast.makeText(context, "Error al guardar el alimento", Toast.LENGTH_SHORT).show()
            }
        }
    }
}