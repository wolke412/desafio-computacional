package com.dc.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.dc.R

class NotificationsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_notifications, container, false)

        val textView: TextView = root.findViewById(R.id.text_notifications)

        val notificationsModel = ViewModelProvider(this).get(NotificationsViewModel::class.java)
        notificationsModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        return root
    }
}
