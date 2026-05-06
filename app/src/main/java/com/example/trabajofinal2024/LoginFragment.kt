package com.example.trabajofinal2024

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleButton: SignInButton



    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account.idToken?.let {
                        firebaseAuthWithGoogle(it)
                    } ?: run {
                        Toast.makeText(requireContext(), "Token inválido", Toast.LENGTH_LONG).show()
                    }
                } catch (e: ApiException) {
                    Toast.makeText(requireContext(), "Google Sign-In falló", Toast.LENGTH_LONG).show()
                }
            }
        }
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val rootView = inflater.inflate(R.layout.fragment_login, container, false)

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        googleButton = rootView.findViewById(R.id.btnGoogle)

        val gsi = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gsi)

        googleButton.setOnClickListener {
            signInWithGoogle()
        }


        // Views (los IDs vienen de TU XML)
        emailEditText = rootView.findViewById(R.id.usuariotext)
        passwordEditText = rootView.findViewById(R.id.contraseñaeditText)
        loginButton = rootView.findViewById(R.id.ingresarid)

        loginButton.setOnClickListener {
            loginUser()
        }

        return rootView
    }

    private fun loginUser() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Ingrese usuario y contraseña", Toast.LENGTH_LONG).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Login exitoso", Toast.LENGTH_LONG).show()

                    // Navegar con Navigation Component
                    findNavController()
                        .navigate(R.id.action_loginFragment_to_encuestasListFragment)

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {

                    Toast.makeText(requireContext(), "Login con Google exitoso", Toast.LENGTH_LONG).show()

                    findNavController().navigate(
                        R.id.action_loginFragment_to_encuestasListFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(R.id.loginFragment, true)
                            .build()
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null && isAdded) {
            val navController = findNavController()

            if (navController.currentDestination?.id == R.id.loginFragment) {
                navController.navigate(R.id.action_loginFragment_to_encuestasListFragment)
            }
        }
    }





}