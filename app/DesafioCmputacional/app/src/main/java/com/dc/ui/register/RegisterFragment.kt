package com.dc.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dc.R // Importe seu R global
import com.dc.databinding.FragmentLoginBinding // Se estiver usando ViewBinding

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(
            R.layout.fragment_register,
            container, false
        )

        val btnToRegister : Button = root.findViewById(R.id.btnLogin)
        val btnSignup: Button = root.findViewById(R.id.btnCadastrar)
        val inpEmail: EditText = root.findViewById(R.id.txtEmail)
        val inpPassword: EditText = root.findViewById(R.id.txtPassword)
        val inpConfirmPassword: EditText = root.findViewById(R.id.txtConfirmPassword)

        btnSignup.setOnClickListener {
            Toast.makeText(requireContext(),
                "EMAIL: ${inpEmail.text} SENHA: ${inpPassword.text}",
                Toast.LENGTH_SHORT).show()
        }
        btnToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        return root
    }
}
