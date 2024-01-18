package com.example.futsalhub

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.futsalhub.databinding.FragmentGroundAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth


class GroundAdminDashboard : Fragment() {
    private lateinit var binding: FragmentGroundAdminDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroundAdminDashboardBinding.inflate(inflater, container, false)

        binding.cvView.setOnClickListener {
            findNavController().navigate(R.id.action_groundAdminDashboard_to_selectGroundFragment)
        }

        binding.cvEdit.setOnClickListener {
            findNavController().navigate(R.id.action_groundAdminDashboard_to_selectGroundFragmentTwo)
        }

        binding.cvLogOut.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Logout")
            builder.setMessage("Are you sure you want to log out?")
            builder.setPositiveButton("Logout") { _, _ ->
                // Perform logout action
                logoutUser()
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
        return binding.root
    }
    private fun logoutUser() {
        firebaseAuth = FirebaseAuth.getInstance()

        firebaseAuth.signOut()
        val intent = Intent(activity, StartActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}