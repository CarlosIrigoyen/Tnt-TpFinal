package com.example.trabajofinal2024

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class EncuestasListComposeFragment :
    Fragment(R.layout.fragment_placeholder_compose) {

    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory(
            (requireActivity().application as App).encuestaRepositorio
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val composeView = view.findViewById<ComposeView>(R.id.compose_view)

        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )

        composeView.setContent {

            EncuestasListComposeHost(
                encuestaViewModel = encuestaViewModel,

                onNavigateToFood = { encuestaId: Int ->
                    val bundle = Bundle().apply {
                        putInt("encuestaid", encuestaId)
                    }
                    findNavController().navigate(
                        R.id.action_encuestasList_to_foodFragment,
                        bundle
                    )
                },

                onNavigateToEncuesta = {
                    findNavController().navigate(
                        R.id.action_encuestasList_to_encuestaFragment
                    )
                },

                onNavigateToMap = {
                    findNavController().navigate(
                        R.id.action_encuestasList_to_mapFragment
                    )
                },

                onNavigateToStats = {
                    findNavController().navigate(
                        R.id.action_encuestasList_to_statsFragment
                    )
                },

                onSignOut = {
                    FirebaseAuth.getInstance().signOut()

                    findNavController().navigate(
                        R.id.loginFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.main_navigation, true)
                            .build()
                    )
                }
            )
        }
    }
}
