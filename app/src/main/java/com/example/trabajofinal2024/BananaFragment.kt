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
import com.example.trabajofinal2024.databinding.FragmentBananaBinding

class BananaFragment : Fragment() {

    private lateinit var binding: FragmentBananaBinding

    private val encuestaViewModel: EncuestaViewModel by viewModels() {
        EncuestaViewModelFactory((activity?.application as EncuestaApp).repositorio)
    }

    val bananaViewModel: BananaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_banana, container, false)

        binding.bananaviewmodel = bananaViewModel
        binding.lifecycleOwner = this

        setupClickListeners(binding, encuestaViewModel)


        return binding.root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        configurarNumeroVeces()
        configurarSpinner()
        bananaViewModel.frecuencia.observe(viewLifecycleOwner, Observer {
            actualizarRadioGroup()
        })
        configurarRadioGroup()

        bananaViewModel.cantidad.observe(viewLifecycleOwner, Observer {
            binding.spinnerOpciones.setSelection(bananaViewModel.cantidadList.indexOf(it))
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
        val frecuenciaSeleccionada =bananaViewModel.frecuencia.value ?: ""
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
                    bananaViewModel.setNumeroVeces(s.toString())
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
            bananaViewModel.setFrecuencia(frecuencia)
        }
    }

    fun configurarSpinner() {
        val spinner: Spinner = binding.spinnerOpciones
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bananaViewModel.cantidadList)
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
                    bananaViewModel.setCantidad(valorSeleccionado)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
    }

    private fun setupClickListeners(binding: FragmentBananaBinding, viewModel: EncuestaViewModel){
        binding.enviar.setOnClickListener{
            viewModel.insert(
                Encuesta(
                    nombre_alimento = bananaViewModel.alimento.value ?:"",
                    cantidad_alimento = bananaViewModel.cantidad.value ?:"",
                    numero_veces = bananaViewModel.numeroveces.value ?:"",
                    frecuencia_veces = bananaViewModel.frecuencia.value ?:""
                )
            )
            Toast.makeText(context, "Encuesta completada", Toast.LENGTH_SHORT).show()
            NavHostFragment.findNavController(this).navigate(R.id.action_bananaFragment_to_duraznoFragment)

        }
    }



}