package com.example.trabajofinal2024


import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import com.example.trabajofinal2024.databinding.FragmentQueso2Binding
import com.example.trabajofinal2024.databinding.FragmentQuesoBinding

class QuesoFragment2 : Fragment() {
    private lateinit var binding: FragmentQueso2Binding

    private val alimentoViewModel: AlimentoViewModel by viewModels() {
        AlimentoViewModel.AlimentoViewModelFactory((activity?.application as App).alimentoRepositorio)
    }
    val quesoViewModel2: QuesoViewModel2 by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_queso2, container, false)

        binding.quesoviewmodel2 = quesoViewModel2
        binding.lifecycleOwner = this

        val encuestaid = arguments?.getInt("encuestaid")
        if (encuestaid != null) {
            quesoViewModel2.setEncuestaId(encuestaid)
        }


        return binding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setupClickListeners(binding, alimentoViewModel)


        super.onViewCreated(view, savedInstanceState)
        configurarNumeroVeces()
        configurarSpinner()
        quesoViewModel2.frecuencia.observe(viewLifecycleOwner, Observer {
            actualizarRadioGroup()
        })
        configurarRadioGroup()

        quesoViewModel2.cantidad.observe(viewLifecycleOwner, Observer {
            binding.spinnerOpciones.setSelection(quesoViewModel2.cantidadList.indexOf(it))
        })

        binding.increment.setOnClickListener {
            val valorActual = binding.numeroid.text.toString().toIntOrNull() ?: 0
            val nuevoValor = valorActual + 1
            binding.numeroid.setText(nuevoValor.toString())
        }

        binding.decrement.setOnClickListener {
            val valorActual = binding.numeroid.text.toString().toIntOrNull() ?: 0
            if (valorActual > 0) {
                val nuevoValor = valorActual - 1
                binding.numeroid.setText(nuevoValor.toString())
            }
        }

        binding.numeroid.filters = arrayOf(InputFilter { source, start, end, dest, dstart, dend ->
            if (source.all { it.isDigit() }) {
                null // Permitir la entrada
            } else {
                "" // No permitir la entrada (eliminar el texto ingresado)
            }
        })



    }
    fun actualizarRadioGroup() {
        val frecuenciaSeleccionada = quesoViewModel2.frecuencia.value ?: ""
        when(frecuenciaSeleccionada) {
            "Diaria" -> binding.frecuenciaGroup.check(R.id.diarioId)
            "Semanal" -> binding.frecuenciaGroup.check(R.id.semanalId)
            "Mensual" -> binding.frecuenciaGroup.check(R.id.mensualId)
            "Anual" -> binding.frecuenciaGroup.check(R.id.anualId)
            "Nunca" -> binding.frecuenciaGroup.check(R.id.nuncaId)
        }
    }
    fun configurarNumeroVeces() {
        binding.numeroid.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d("TextWatcher", "BeforeTextChanged producido")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextWatcher", "onTextChanged lanzado")

            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    quesoViewModel2.setNumeroVeces(s.toString())
                }
            }
        })
    }
    fun configurarRadioGroup() {
        binding.frecuenciaGroup.setOnCheckedChangeListener { _, checkedId ->
            val frecuencia = when(checkedId) {
                R.id.diarioId -> "Diaria"
                R.id.semanalId -> "Semanal"
                R.id.mensualId -> "Mensual"
                R.id.anualId -> "Anual"
                R.id.nuncaId -> "Nunca"
                else -> ""
            }
            quesoViewModel2.setFrecuencia(frecuencia)
        }
    }
    fun configurarSpinner() {
        val spinner: Spinner = binding.spinnerOpciones
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, quesoViewModel2.cantidadList)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.let {
                    val valorSeleccionado = parent.getItemAtPosition(position).toString()
                    quesoViewModel2.setCantidad(valorSeleccionado)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }
    private fun setupClickListeners(binding: FragmentQueso2Binding, viewModel: AlimentoViewModel){
        binding.siguiente.setOnClickListener{
            try {
                viewModel.insert(
                    Alimento(
                        encuestaId = quesoViewModel2.encuestaId,
                        nombre_alimento = quesoViewModel2.alimento.value ?: "",
                        categoria = quesoViewModel2.categoria.value ?: "",
                        cantidad_alimento = quesoViewModel2.cantidad.value ?: "",
                        numero_veces = quesoViewModel2.numeroveces.value ?: "",
                        frecuencia_veces = quesoViewModel2.frecuencia.value ?: "",
                        gramos = quesoViewModel2.calcularGramosTotales(),
                        kcal = quesoViewModel2.calcularKcal(),
                        carbohidratos = quesoViewModel2.calcularCarbohidratos(),
                        proteinas = quesoViewModel2.calcularProteinas(),
                        grasas = quesoViewModel2.calcularGrasasTotales(),
                        alcohol = quesoViewModel2.calcularAlcohol(),
                        colesterol = quesoViewModel2.calcularColesterol(),
                        fibra = quesoViewModel2.calcularFibra()
                    )

                )
                val bundle = Bundle()
                bundle.putInt("encuestaid", quesoViewModel2.encuestaId)
                Toast.makeText(context, "Alimento cargado", Toast.LENGTH_SHORT).show()
                NavHostFragment.findNavController(this).navigate(R.id.action_quesoFragment2_to_mantecaFragment, bundle)

            }catch (e: Exception) {
                Log.e("QuesoFragment2", "Error insertando alimento: ${e.message}")
            }
        }
        binding.cancelar.setOnClickListener{
            NavHostFragment.findNavController(this).navigate(R.id.action_quesoFragment2_to_encuestaFragment)
        }
    }




}