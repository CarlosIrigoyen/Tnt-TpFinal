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
import com.example.trabajofinal2024.databinding.FragmentSalameBinding
import com.example.trabajofinal2024.databinding.FragmentSalchichaBinding


class SalameFragment : Fragment() {

    private lateinit var binding: FragmentSalameBinding


    private val alimentoViewModel: AlimentoViewModel by viewModels() {
        AlimentoViewModel.AlimentoViewModelFactory((activity?.application as App).alimentoRepositorio)
    }

    val salameViewModel: SalameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_salame,
            container,
            false
        )

        binding.salameviewmodel = salameViewModel
        binding.lifecycleOwner = this

        val encuestaid = arguments?.getInt("encuestaid")
        if (encuestaid != null) {
            salameViewModel.setEncuestaId(encuestaid)
        }

        return binding.root
    }

    private fun setupClickListeners(
        binding: FragmentSalameBinding, viewModel: AlimentoViewModel
    ) {
        binding.siguiente.setOnClickListener {
            try {
                viewModel.insert(
                    Alimento(
                        encuestaId = salameViewModel.encuestaId,
                        nombre_alimento = salameViewModel.alimento.value ?: "",
                        categoria = salameViewModel.categoria.value ?: "",
                        cantidad_alimento = salameViewModel.cantidad.value ?: "",
                        numero_veces = salameViewModel.numeroveces.value ?: "",
                        frecuencia_veces = salameViewModel.frecuencia.value ?: "",
                        gramos = salameViewModel.calcularGramosTotales(),
                        kcal = salameViewModel.calcularKcal(),
                        carbohidratos = salameViewModel.calcularCarbohidratos(),
                        proteinas = salameViewModel.calcularProteinas(),
                        grasas = salameViewModel.calcularGrasasTotales(),
                        alcohol = salameViewModel.calcularAlcohol(),
                        colesterol = salameViewModel.calcularColesterol(),
                        fibra = salameViewModel.calcularFibra()
                    )
                )
                val bundle = Bundle()
                bundle.putInt("encuestaid", salameViewModel.encuestaId)
                Toast.makeText(context, "Alimento cargado", Toast.LENGTH_SHORT).show()
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_salameFragment_to_mortadelaFragment,bundle)
            } catch (e: Exception) {
                Log.e("SalameFragment", "Error insertando alimento: ${e.message}")
            }


        }
        binding.cancelar.setOnClickListener{
            NavHostFragment.findNavController(this).navigate(R.id.action_salameFragment_to_encuestaFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        configurarNumeroVeces()
        configurarSpinner()
        salameViewModel.frecuencia.observe(viewLifecycleOwner, Observer {
            actualizarRadioGroup()
        })
        configurarRadioGroup()

        salameViewModel.cantidad.observe(viewLifecycleOwner, Observer {
            binding.spinnerOpciones.setSelection(salameViewModel.cantidadList.indexOf(it))
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

        setupClickListeners(binding, alimentoViewModel)

    }

    fun actualizarRadioGroup() {
        val frecuenciaSeleccionada = salameViewModel.frecuencia.value ?: ""
        when (frecuenciaSeleccionada) {
            "Diaria" -> binding.frecuenciaGroup.check(R.id.diarioId)
            "Semanal" -> binding.frecuenciaGroup.check(R.id.semanalId)
            "Mensual" -> binding.frecuenciaGroup.check(R.id.mensualId)
            "Anual" -> binding.frecuenciaGroup.check(R.id.anualId)
            "Nunca" -> binding.frecuenciaGroup.check(R.id.nuncaId)
        }
    }


    fun configurarNumeroVeces() {
        binding.numeroid.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                Log.d("TextWatcher", "BeforeTextChanged producido")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d("TextWatcher", "onTextChanged lanzado")

            }

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    salameViewModel.setNumeroVeces(s.toString())
                }
            }
        })
    }

    fun configurarRadioGroup() {
        binding.frecuenciaGroup.setOnCheckedChangeListener { _, checkedId ->
            val frecuencia = when (checkedId) {
                R.id.diarioId -> "Diaria"
                R.id.semanalId -> "Semanal"
                R.id.mensualId -> "Mensual"
                R.id.anualId -> "Anual"
                R.id.nuncaId -> "Nunca"
                else -> ""
            }
            salameViewModel.setFrecuencia(frecuencia)
        }
    }

    fun configurarSpinner() {
        val spinner: Spinner = binding.spinnerOpciones
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            salameViewModel.cantidadList
        )
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                parent?.let {
                    val valorSeleccionado = parent.getItemAtPosition(position).toString()
                    salameViewModel.setCantidad(valorSeleccionado)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }


}