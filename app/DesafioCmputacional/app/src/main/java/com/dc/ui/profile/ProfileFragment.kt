package com.dc.ui.profile

import android.content.Context
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.dc.AuthActivity
import com.dc.R
import com.dc.utils.SessionManager

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLogoutButton(view)
        setDataVisualization(view)
    }
    private fun setDataVisualization(view: View) {
        val emailContent = view.findViewById<TextView>(R.id.emailContent)
        emailContent.text = SessionManager.getInstance(requireContext()).getUserEmail()

    }
    private fun setLogoutButton(view: View) {
        val btnLogout = view.findViewById<Button>(R.id.logout_button)

        btnLogout.setOnClickListener {

            SessionManager.getInstance(requireContext()).logoutUser()

            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

}