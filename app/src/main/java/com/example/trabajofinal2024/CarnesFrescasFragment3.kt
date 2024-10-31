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
import com.example.trabajofinal2024.databinding.FragmentCarnesFrescas3Binding

class CarnesFrescasFragment3 : Fragment() {

    private lateinit var binding: FragmentCarnesFrescas3Binding

    private val alimentoViewModel: AlimentoViewModel by viewModels() {
        AlimentoViewModel.AlimentoViewModelFactory((activity?.application as App).alimentoRepositorio)
    }

    val carnesFrescasViewModel3: CarnesFrescasViewModel3 by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_carnes_frescas3, container, false)

        binding.carnesfrescasviewmodel3 = carnesFrescasViewModel3
        binding.lifecycleOwner = this

        val encuestaid = arguments?.getInt("encuestaid")
        if (encuestaid != null) {
            carnesFrescasViewModel3.setEncuestaId(encuestaid)
        }


        return binding.root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        configurarNumeroVeces()
        configurarSpinner()
        carnesFrescasViewModel3.frecuencia.observe(viewLifecycleOwner, Observer {
            actualizarRadioGroup()
        })
        configurarRadioGroup()

        carnesFrescasViewModel3.cantidad.observe(viewLifecycleOwner, Observer {
            binding.spinnerOpciones.setSelection(carnesFrescasViewModel3.cantidadList.indexOf(it))
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
        val frecuenciaSeleccionada = carnesFrescasViewModel3.frecuencia.value ?: ""
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
                    carnesFrescasViewModel3.setNumeroVeces(s.toString())
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
            carnesFrescasViewModel3.setFrecuencia(frecuencia)
        }
    }

    fun configurarSpinner() {
        val spinner: Spinner = binding.spinnerOpciones
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, carnesFrescasViewModel3.cantidadList)
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
                    carnesFrescasViewModel3.setCantidad(valorSeleccionado)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun setupClickListeners(binding: FragmentCarnesFrescas3Binding, viewModel: AlimentoViewModel){
        binding.siguiente.setOnClickListener{
            try {
                viewModel.insert(
                    Alimento(
                        encuestaId = carnesFrescasViewModel3.encuestaId,
                        nombre_alimento = carnesFrescasViewModel3.alimento.value ?: "",
                        categoria = carnesFrescasViewModel3.categoria.value ?: "",
                        cantidad_alimento = carnesFrescasViewModel3.cantidad.value ?: "",
                        numero_veces = carnesFrescasViewModel3.numeroveces.value ?: "",
                        frecuencia_veces = carnesFrescasViewModel3.frecuencia.value ?: "",
                        gramos = carnesFrescasViewModel3.calcularGramosTotales(),
                        kcal = carnesFrescasViewModel3.calcularKcal(),
                        carbohidratos = carnesFrescasViewModel3.calcularCarbohidratos(),
                        proteinas = carnesFrescasViewModel3.calcularProteinas(),
                        grasas = carnesFrescasViewModel3.calcularGrasasTotales(),
                        alcohol = carnesFrescasViewModel3.calcularAlcohol(),
                        colesterol = carnesFrescasViewModel3.calcularColesterol(),
                        fibra = carnesFrescasViewModel3.calcularFibra()
                    )
                )
                val bundle = Bundle()
                bundle.putInt("encuestaid", carnesFrescasViewModel3.encuestaId)
                Toast.makeText(context, "Alimento cargado", Toast.LENGTH_SHORT).show()
                NavHostFragment.findNavController(this).navigate(R.id.action_carnesFrescasFragment3_to_salchichaFragment,bundle)

            }catch (e: Exception) {
                Log.e("CarnesFrescasFragment3", "Error insertando alimento: ${e.message}")
            }
        }
        binding.cancelar.setOnClickListener{
            NavHostFragment.findNavController(this).navigate(R.id.action_carnesFrescasFragment3_to_encuestaFragment)
        }
    }



}