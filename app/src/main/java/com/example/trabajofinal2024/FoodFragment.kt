package com.example.trabajofinal2024

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.trabajofinal2024.databinding.FragmentFoodBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FoodFragment : Fragment(R.layout.fragment_food) {

    private lateinit var binding: FragmentFoodBinding

    private var encuestaInicializada = false

    private var setFoodJob: Job? = null


    private val alimentoViewModel: AlimentoViewModel by viewModels {
        AlimentoViewModel.AlimentoViewModelFactory((activity?.application as App).alimentoRepositorio)
    }
    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory((activity?.application as App).encuestaRepositorio)
    }

    private var encuestaId: String? = ""
    private var currentIndex: Int = 0

    private var encuestaActual: Encuesta? = null
    private var foodItem: FoodItem? = null
    private var encuestaCompletada: Boolean = false

    private val cantidadOpciones = arrayOf("100", "150", "200", "250", "500")

    companion object {
        private const val STATE_INDEX = "state_current_index"
        private const val ARG_ENCUESTA_ID = "encuestaid"
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { encuestaId = it.getString(ARG_ENCUESTA_ID) }
        currentIndex = savedInstanceState?.getInt(STATE_INDEX) ?: 0
        encuestaInicializada = savedInstanceState?.getBoolean("encuestaInicializada") ?: false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(STATE_INDEX, currentIndex)
        outState.putBoolean("encuestaInicializada", encuestaInicializada)
    }

    override fun onCreateView(inflater: android.view.LayoutInflater, container: android.view.ViewGroup?, savedInstanceState: Bundle?): android.view.View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_food, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private fun setFoodAtIndex(index: Int) {

        if (index !in 0 until FoodCatalog.ALL.size) {
            Log.e("FoodFragment", "Índice inválido: $index")
            return
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val encuestaId = encuestaActual?.firestoreId ?: return

        val template = FoodCatalog.ALL[index]
        setFoodJob?.cancel()
        setFoodJob = lifecycleScope.launch {
            val alimentoGuardado = alimentoViewModel.getAlimentoFirebase(uid, encuestaId, template.nombre)

            if (!isActive) return@launch
            val item = if (alimentoGuardado != null) {
                FoodItem(template).apply {
                    numeroveces.value = alimentoGuardado.numero_veces
                    cantidad.value = alimentoGuardado.cantidad_alimento
                    frecuencia.value = alimentoGuardado.frecuencia_veces
                }.also {
                    foodItem = it
                    binding.foodItem = foodItem
                    restaurarUI()
                }
            } else {

                foodItem = FoodItem(template).apply {
                    frecuencia.value = "Nunca"
                }
                binding.spinnerOpciones.setSelection(0, false)
                foodItem?.cantidad?.value = cantidadOpciones[0]
                binding.foodItem = foodItem

                binding.radioNunca.isChecked = true
            }

            actualizarBotones()
        }
    }



    private fun restaurarUI() {
        // Restaurar frecuencia
        when (foodItem?.frecuencia?.value) {
            "Diaria" -> binding.radioDiaria.isChecked = true
            "Semanal" -> binding.radioSemanal.isChecked = true
            "Mensual" -> binding.radioMensual.isChecked = true
            "Anual" -> binding.radioAnual.isChecked = true
            "Nunca" -> binding.radioNunca.isChecked = true
        }

        val cantidadBD = foodItem?.cantidad?.value ?: cantidadOpciones[0]
        val cantidadNormalizada = cantidadBD.trim().removeSuffix(".0")
        foodItem?.cantidad?.value = cantidadNormalizada

        val pos = cantidadOpciones.indexOf(cantidadNormalizada).coerceAtLeast(0)
        binding.spinnerOpciones.setSelection(pos, false)

        binding.vecesInput.setText(foodItem?.numeroveces?.value ?: "1")
    }

    private fun actualizarBotones() {

        binding.anteriorAlimento.isEnabled = currentIndex > 0
        binding.anteriorAlimento.alpha =
            if (currentIndex > 0) 1f else 0.3f

        binding.cancelarEncuesta.visibility =
            if (currentIndex > 0) View.VISIBLE else View.GONE
    }

    private fun construirAlimentoDesdeUI(): Alimento? {
        val currentFood = foodItem ?: return null

        val cantidadSeleccionada =
            binding.spinnerOpciones.selectedItem?.toString() ?: cantidadOpciones[0]
        val veces = currentFood.numeroveces.value?.toIntOrNull() ?: 1
        val frecuencia = currentFood.frecuencia.value
            ?.takeIf { it.isNotBlank() }
            ?: "Nunca"

        val alimentoBase = Alimento(
            encuestaId = 0,
            nombre_alimento = currentFood.alimentoNombre,
            categoria = currentFood.categoria,
            cantidad_alimento = cantidadSeleccionada,
            numero_veces = veces.toString(),
            frecuencia_veces = frecuencia,
            gramos = currentFood.template.gramosPorUnidad,
            kcal = currentFood.template.kcalPorUnidad,
            carbohidratos = currentFood.template.carbohidratosPorUnidad,
            proteinas = currentFood.template.proteinasPorUnidad,
            grasas = currentFood.template.grasasPorUnidad,
            alcohol = currentFood.template.alcoholPorUnidad,
            colesterol = currentFood.template.colesterolPorUnidad,
            fibra = currentFood.template.fibraPorUnidad
        )

        val alimentoCalculado = alimentoViewModel.calcularValoresNutricionalesCompletos(
            alimentoBase,
            cantidadSeleccionada.toDoubleOrNull() ?: 0.0,
            veces,
            when (frecuencia) {
                "Diaria" -> R.id.radioDiaria
                "Semanal" -> R.id.radioSemanal
                "Mensual" -> R.id.radioMensual
                "Anual" -> R.id.radioAnual
                else -> R.id.radioNunca
            }
        )

        alimentoCalculado.frecuencia_veces = frecuencia
        alimentoCalculado.numero_veces = veces.toString()

        return alimentoCalculado
    }


    private fun configurarSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cantidadOpciones
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerOpciones.adapter = adapter

        binding.spinnerOpciones.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    foodItem?.cantidad?.value = cantidadOpciones[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val idEncuesta = encuestaId ?: return

        encuestaViewModel.getEncuestaFirebase(uid, idEncuesta)
            .observe(viewLifecycleOwner) {
                        encuesta ->

                    if (encuesta == null) {
                        Toast.makeText(requireContext(), "Encuesta no encontrada", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                        return@observe
                    }
                    encuestaCompletada = encuesta.completa
                    encuestaActual = encuesta

                    if (encuesta.completa) {
                        Toast.makeText(
                            requireContext(),
                            "Esta encuesta ya fue completada.",
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().popBackStack()

                        return@observe
                    }

                    if (!encuesta.activa) {
                        Toast.makeText(
                            requireContext(),
                            "Reanudando encuesta abandonada...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                if (!encuestaInicializada) {
                    encuestaInicializada = true
                    currentIndex = encuesta.currentIndex.coerceIn(0, FoodCatalog.ALL.size - 1)
                    binding.progresoText.text = "Alimento ${currentIndex + 1} de ${FoodCatalog.ALL.size}"
                    setFoodAtIndex(currentIndex)
                } else {
                    binding.progresoText.text = "Alimento ${currentIndex + 1} de ${FoodCatalog.ALL.size}"
                    setFoodAtIndex(currentIndex)
                }
            }


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


        binding.anteriorAlimento.setOnClickListener {

            if (currentIndex > 0) {

                lifecycleScope.launch {

                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                    val encuestaId = encuestaActual?.firestoreId ?: return@launch

                    val alimento = construirAlimentoDesdeUI()
                    Log.d("Alimento", alimento?.encuestaId.toString())
                    if (alimento != null) {
                        guardarAlimento(uid, encuestaId,alimento) // update si existe, insert si no
                    }

                    currentIndex--

                    encuestaViewModel.updateProgress(uid, encuestaId, currentIndex)

                    binding.progresoText.text =
                        "Alimento ${currentIndex + 1} de ${FoodCatalog.ALL.size}"

                    setFoodAtIndex(currentIndex)

                }
            }
        }

        binding.siguienteAlimento.setOnClickListener {

            lifecycleScope.launch {

                val alimento = construirAlimentoDesdeUI() ?: return@launch
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val encuestaId = encuestaActual?.firestoreId ?: return@launch

                guardarAlimento(uid, encuestaId, alimento)

                currentIndex++

                encuestaViewModel.updateProgress(uid, encuestaId, currentIndex)

                if (currentIndex >= FoodCatalog.ALL.size) {

                    encuestaViewModel.markCompletedFirebase(uid, encuestaId, currentIndex - 1)

                    Toast.makeText(
                        requireContext(),
                        "¡Encuesta completada!",
                        Toast.LENGTH_LONG
                    ).show()

                    findNavController().popBackStack()

                }

                else {

                    binding.progresoText.text =
                        "Alimento ${currentIndex + 1} de ${FoodCatalog.ALL.size}"

                    setFoodAtIndex(currentIndex)
                }
            }
        }

        binding.cancelarEncuesta.setOnClickListener {
            findNavController().popBackStack(R.id.encuestasListFragment, false)
        }

        binding.abandonarEncuesta.setOnClickListener {

            if (encuestaId != null) {
                lifecycleScope.launch {
                    val encuestaId = encuestaActual?.firestoreId ?: return@launch
                    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                    encuestaViewModel.abandonEncuestaFirebase(uid, encuestaId)
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


    private suspend fun guardarAlimento(uid: String, encuestaId: String, alimento: Alimento) {

        alimentoViewModel.guardarAlimentoEnFirebase(uid, encuestaId, alimento)
    }


    }