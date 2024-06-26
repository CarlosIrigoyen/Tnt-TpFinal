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
import androidx.lifecycle.Observer
import androidx.fragment.app.viewModels

import androidx.navigation.fragment.NavHostFragment
import com.example.trabajofinal2024.databinding.FragmentLechePolvoEnteraBinding

class LechePolvoEnteraFragment : Fragment() {

    private lateinit var binding: FragmentLechePolvoEnteraBinding


   private val alimentoViewModel: AlimentoViewModel by viewModels() {
       AlimentoViewModel.AlimentoViewModelFactory((activity?.application as App).alimentoRepositorio)
   }


    val lechePolvoViewModel: LechePolvoViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_leche_polvo_entera,
            container,
            false
        )

        binding.lechepolvoviewmodel = lechePolvoViewModel
        binding.lifecycleOwner = this

        val encuestaid = arguments?.getInt("encuestaid")
        if (encuestaid != null) {
            lechePolvoViewModel.setEncuestaId(encuestaid)
        }

        return binding.root
    }


    private fun setupClickListeners(
        binding: FragmentLechePolvoEnteraBinding, viewModel: AlimentoViewModel
    ) {
        binding.siguiente.setOnClickListener {
            try {
                Log.d("LechePolvoEnteraFragment", "Boton siguiente presionado")
                viewModel.insert(
                    Alimento(
                        encuestaId = lechePolvoViewModel.encuestaId,
                        nombre_alimento = lechePolvoViewModel.alimento.value ?: "",
                        categoria = lechePolvoViewModel.categoria.value ?: "",
                        cantidad_alimento = lechePolvoViewModel.cantidad.value ?: "",
                        numero_veces = lechePolvoViewModel.numeroveces.value ?: "",
                        frecuencia_veces = lechePolvoViewModel.frecuencia.value ?: "",
                        gramos = lechePolvoViewModel.calcularGramosTotales(),
                        kcal = lechePolvoViewModel.calcularKcal(),
                        carbohidratos = lechePolvoViewModel.calcularCarbohidratos(),
                        proteinas = lechePolvoViewModel.calcularProteinas(),
                        grasas = lechePolvoViewModel.calcularGrasasTotales(),
                        alcohol = lechePolvoViewModel.calcularAlcohol(),
                        colesterol = lechePolvoViewModel.calcularColesterol(),
                        fibra = lechePolvoViewModel.calcularFibra()
                    )
                )
                val bundle = Bundle()
                bundle.putInt("encuestaid", lechePolvoViewModel.encuestaId)
                Toast.makeText(context, "Alimento cargado", Toast.LENGTH_SHORT).show()
                NavHostFragment.findNavController(this).navigate(R.id.action_lechePolvoEntera_to_lecheFluidaEntera, bundle)
            } catch (e: Exception) {
                Log.e("LechePolvoEnteraFragment", "Error insertando alimento: ${e.message}")
            }
        }
        binding.cancelar.setOnClickListener{
            NavHostFragment.findNavController(this).navigate(R.id.action_lechePolvoEntera_to_encuestaFragment)
        }
    }





    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        configurarNumeroVeces()
        configurarSpinner()
        lechePolvoViewModel.frecuencia.observe(viewLifecycleOwner, Observer {
            actualizarRadioGroup()
        })
        configurarRadioGroup()

        lechePolvoViewModel.cantidad.observe(viewLifecycleOwner, Observer {
            binding.spinnerOpciones.setSelection(lechePolvoViewModel.cantidadList.indexOf(it))
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
            val frecuenciaSeleccionada = lechePolvoViewModel.frecuencia.value ?: ""
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
                        lechePolvoViewModel.setNumeroVeces(s.toString())
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
                lechePolvoViewModel.setFrecuencia(frecuencia)
            }
        }

        fun configurarSpinner() {
            val spinner: Spinner = binding.spinnerOpciones
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                lechePolvoViewModel.cantidadList
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
                        lechePolvoViewModel.setCantidad(valorSeleccionado)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }




}