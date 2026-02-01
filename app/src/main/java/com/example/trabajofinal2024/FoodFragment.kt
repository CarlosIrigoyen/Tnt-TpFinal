package com.example.trabajofinal2024

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.trabajofinal2024.databinding.FragmentFoodBinding

class FoodFragment : Fragment() {

    private lateinit var binding: FragmentFoodBinding

    private val alimentoViewModel: AlimentoViewModel by viewModels {
        AlimentoViewModel.AlimentoViewModelFactory((activity?.application as App).alimentoRepositorio)
    }

    private var encuestaId: Int = 0
    private var currentIndex: Int = 0
    private var foodItem: FoodItem? = null

    companion object {
        private const val STATE_INDEX = "state_current_index"
        private const val ARG_ENCUESTA_ID = "encuestaid"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { encuestaId = it.getInt(ARG_ENCUESTA_ID, 0) }
        currentIndex = savedInstanceState?.getInt(STATE_INDEX) ?: 0
        if (currentIndex < 0) currentIndex = 0
        if (currentIndex >= FoodCatalog.ALL.size) currentIndex = 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_INDEX, currentIndex)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
        val template = list[index]
        foodItem = FoodItem(template)
        binding.foodItem = foodItem
        binding.alimento.text = template.nombre
        binding.spinnerOpciones.setSelection(0)
        binding.numeroid.setText(foodItem?.numeroveces?.value ?: "1")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarNumeroVeces()
        configurarSpinner()

        binding.increment.setOnClickListener {
            val valorActual = binding.numeroid.text.toString().toIntOrNull() ?: 0
            binding.numeroid.setText((valorActual + 1).toString())
        }

        binding.decrement.setOnClickListener {
            val valorActual = binding.numeroid.text.toString().toIntOrNull() ?: 0
            if (valorActual > 0) binding.numeroid.setText((valorActual - 1).toString())
        }

        binding.numeroid.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            if (source.all { it.isDigit() }) null else ""
        })

        binding.frecuenciaGroup.setOnCheckedChangeListener { _, checkedId ->
            val frecuencia = when (checkedId) {
                R.id.diarioId -> "Diaria"
                R.id.semanalId -> "Semanal"
                R.id.mensualId -> "Mensual"
                R.id.anualId -> "Anual"
                R.id.nuncaId -> "Nunca"
                else -> ""
            }
            foodItem?.frecuencia?.value = frecuencia
        }

        setupClickListeners()
    }

    private fun configurarNumeroVeces() {
        binding.numeroid.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
            override fun afterTextChanged(s: Editable?) {
                foodItem?.numeroveces?.value = s?.toString() ?: "1"
            }
        })
    }

    private fun configurarSpinner() {
        val cantidadList = listOf("1", "2", "3", "4", "6")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cantidadList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOpciones.adapter = adapter

        binding.spinnerOpciones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val valor = parent?.getItemAtPosition(position).toString()
                foodItem?.cantidad?.value = valor
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupClickListeners() {
        binding.siguiente.setOnClickListener { saveCurrentAlimentoAndAdvance() }
        binding.cancelar.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_encuestaFragment_to_welcomeLogin)
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

        try {
            alimentoViewModel.insert(
                Alimento(
                    encuestaId = encuestaId,
                    nombre_alimento = item.alimentoNombre,
                    categoria = item.categoria,
                    cantidad_alimento = item.cantidad.value ?: "1",
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
        } catch (e: Exception) {
            Log.e("FoodFragment", "Error insertando alimento: ${e.message}")
        }

        currentIndex += 1
        if (currentIndex < FoodCatalog.ALL.size) {
            setFoodAtIndex(currentIndex)
        } else {
            Toast.makeText(context, "Encuesta finalizada: todos los alimentos cargados", Toast.LENGTH_LONG).show()
            NavHostFragment.findNavController(this).navigate(R.id.action_encuestaFragment_to_welcomeLogin)
        }
    }
}
