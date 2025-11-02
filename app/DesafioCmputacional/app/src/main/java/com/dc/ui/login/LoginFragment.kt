package com.dc.ui.login

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dc.AuthActivity
import com.dc.MainActivity
import com.dc.R
import com.dc.api.ApiWrapper
import com.dc.entities.User
import com.dc.utils.SessionManager

class LoginFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(
            R.layout.fragment_login,
            container, false
        )

        val btnToRegister : Button = root.findViewById(R.id.btnRegister)
        val btnLogin: Button = root.findViewById(R.id.buttonLogin)
        val inpEmail: EditText = root.findViewById(R.id.txtEmail)
        val inpPassword: EditText = root.findViewById(R.id.txtPassword)

        btnLogin.setOnClickListener {

            val progressDialog = ProgressDialog(requireContext()).apply {
                setMessage("")
                setCancelable(false)
                show()
            }

            val u = ApiWrapper.call<User>(
                scope = viewLifecycleOwner.lifecycleScope,
                apiCall = { ApiWrapper.tryLogin(inpEmail.text.toString(), inpPassword.text.toString()) },
                onSuccess = { u ->
                    progressDialog.dismiss()
                    Log.d("Api", "Got user ${u.email} posts")

                    // --
                    SessionManager.getInstance(requireContext()).createLoginSession(
                        u.id, u.email
                    )

                    sendUserToMain()
                },
                onError = { err ->
                    progressDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Erro: ${err.error}",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("ApiError", err.error)
                }
            )
        }

        btnToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return root
    }

    private fun sendUserToMain() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
