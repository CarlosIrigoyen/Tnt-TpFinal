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
import com.example.trabajofinal2024.databinding.FragmentLecheFluidaEnteraBinding
import com.example.trabajofinal2024.databinding.FragmentSalchichaBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class SalchichaFragment : Fragment() {
    private lateinit var binding: FragmentSalchichaBinding


    private val alimentoViewModel: AlimentoViewModel by viewModels() {
        AlimentoViewModel.AlimentoViewModelFactory((activity?.application as App).alimentoRepositorio)
    }

    val salchichaViewModel: SalchichaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_salchicha,
            container,
            false
        )

        binding.salchichaviewmodel = salchichaViewModel
        binding.lifecycleOwner = this

        val encuestaid = arguments?.getInt("encuestaid")
        if (encuestaid != null) {
            salchichaViewModel.setEncuestaId(encuestaid)
        }


        return binding.root
    }

    private fun setupClickListeners(
        binding: FragmentSalchichaBinding, viewModel: AlimentoViewModel
    ) {
        binding.siguiente.setOnClickListener {
            try {
                viewModel.insert(
                    Alimento(
                        encuestaId = salchichaViewModel.encuestaId,
                        nombre_alimento = salchichaViewModel.alimento.value ?: "",
                        categoria = salchichaViewModel.categoria.value ?: "",
                        cantidad_alimento = salchichaViewModel.cantidad.value ?: "",
                        numero_veces = salchichaViewModel.numeroveces.value ?: "",
                        frecuencia_veces = salchichaViewModel.frecuencia.value ?: "",
                        gramos = salchichaViewModel.calcularGramosTotales(),
                        kcal = salchichaViewModel.calcularKcal(),
                        carbohidratos = salchichaViewModel.calcularCarbohidratos(),
                        proteinas = salchichaViewModel.calcularProteinas(),
                        grasas = salchichaViewModel.calcularGrasasTotales(),
                        alcohol = salchichaViewModel.calcularAlcohol(),
                        colesterol = salchichaViewModel.calcularColesterol(),
                        fibra = salchichaViewModel.calcularFibra()
                    )
                )
                val bundle = Bundle()
                bundle.putInt("encuestaid", salchichaViewModel.encuestaId)
                Toast.makeText(context, "Alimento ingresado", Toast.LENGTH_SHORT).show()
                NavHostFragment.findNavController(this)
                    .navigate(R.id.action_salchichaFragment_to_salameFragment, bundle)
            } catch (e: Exception) {
                Log.e("SalchichaFragment", "Error insertando alimento: ${e.message}")
            }


        }
        binding.cancelar.setOnClickListener{
            NavHostFragment.findNavController(this).navigate(R.id.action_salchichaFragment_to_encuestaFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        configurarNumeroVeces()
        configurarSpinner()
        salchichaViewModel.frecuencia.observe(viewLifecycleOwner, Observer {
            actualizarRadioGroup()
        })
        configurarRadioGroup()

        salchichaViewModel.cantidad.observe(viewLifecycleOwner, Observer {
            binding.spinnerOpciones.setSelection(salchichaViewModel.cantidadList.indexOf(it))
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
        val frecuenciaSeleccionada = salchichaViewModel.frecuencia.value ?: ""
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
                    salchichaViewModel.setNumeroVeces(s.toString())
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
            salchichaViewModel.setFrecuencia(frecuencia)
        }
    }

    fun configurarSpinner() {
        val spinner: Spinner = binding.spinnerOpciones
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            salchichaViewModel.cantidadList
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
                    salchichaViewModel.setCantidad(valorSeleccionado)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

}