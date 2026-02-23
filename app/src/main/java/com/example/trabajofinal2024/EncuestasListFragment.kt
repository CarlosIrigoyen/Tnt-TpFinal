package com.example.trabajofinal2024

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class EncuestasListFragment : Fragment(R.layout.fragment_encuestas_list) {

    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory(
            (requireActivity().application as App).encuestaRepositorio
        )
    }

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var adapter: EncuestaAdapter
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerEncuestas)
        val textEmpty = view.findViewById<TextView>(R.id.textEmpty)
        val btnNuevaEncuesta = view.findViewById<Button>(R.id.btnNuevaEncuesta)
        val btnMapa = view.findViewById<Button>(R.id.btnMapa)
        val btnCerrarSesion = view.findViewById<Button>(R.id.btnCerrarSesion)
        val btnEstadisticas = view.findViewById<Button>(R.id.btnEstadisticas)

        val gsi = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gsi)

        adapter = EncuestaAdapter(
            onResumeClick = { encuesta ->
                // Navegar a FoodFragment para continuar
                val bundle = Bundle().apply {
                    putInt("encuestaid", encuesta.encuestaId)
                }
                findNavController().navigate(
                    R.id.action_encuestasList_to_foodFragment,
                    bundle
                )
            },
            onAbandonClick = { encuesta ->
                // Abandonar encuesta
                encuestaViewModel.abandonEncuesta(encuesta.encuestaId)
                Toast.makeText(
                    requireContext(),
                    "Encuesta #${encuesta.encuestaId} abandonada. Puedes reanudarla más tarde.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onReanudarClick = { encuesta ->
                // Reanudar encuesta abandonada
                encuestaViewModel.reanudarEncuesta(encuesta.encuestaId)
                Toast.makeText(
                    requireContext(),
                    "Encuesta #${encuesta.encuestaId} reanudada. Continuarás donde la dejaste.",
                    Toast.LENGTH_SHORT
                ).show()

                // Navegar a FoodFragment
                val bundle = Bundle().apply {
                    putInt("encuestaid", encuesta.encuestaId)
                }
                findNavController().navigate(
                    R.id.action_encuestasList_to_foodFragment,
                    bundle
                )
            }
        )

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Botón para ir a Mapa
        btnMapa.setOnClickListener {
            findNavController().navigate(R.id.action_encuestasList_to_mapFragment)
        }

        // Botón para ir a Estadísticas
        btnEstadisticas.setOnClickListener {
            findNavController().navigate(R.id.action_encuestasList_to_statsFragment)
        }

        btnCerrarSesion.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            googleSignInClient.signOut().addOnCompleteListener {
            findNavController().navigate(
                R.id.loginFragment,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.main_navigation, true)
                    .build())
            }
        }


        // BOTÓN NUEVA ENCUESTA
        btnNuevaEncuesta.setOnClickListener {
            findNavController().navigate(
                R.id.action_encuestasList_to_encuestaFragment
            )
        }

        if (currentUserUid == null) {
            findNavController().navigate(R.id.loginFragment)
            return
        }

        // OBSERVAR ENCUESTAS DEL USUARIO
        encuestaViewModel
            .getEncuestasPorUsuario(currentUserUid)
            .observe(viewLifecycleOwner) { list ->

                if (list.isNullOrEmpty()) {
                    recycler.visibility = View.GONE
                    textEmpty.visibility = View.VISIBLE
                } else {
                    recycler.visibility = View.VISIBLE
                    textEmpty.visibility = View.GONE
                    adapter.submitList(list)
                }
            }
    }
}