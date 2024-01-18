package com.example.futsalhub

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.futsalhub.databinding.FragmentOverallAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth


class OverallAdminDashboard : Fragment() {
    private lateinit var binding: FragmentOverallAdminDashboardBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOverallAdminDashboardBinding.inflate(inflater, container, false)
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

        binding.cvRemove.setOnClickListener {
            findNavController().navigate(R.id.action_overallAdminDashboard_to_removeGroundFragment)
        }

        binding.cvAdd.setOnClickListener {
            findNavController().navigate(R.id.action_overallAdminDashboard_to_addGroundFragment)
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