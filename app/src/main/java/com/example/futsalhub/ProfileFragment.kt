package com.example.futsalhub

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.example.futsalhub.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var userName :String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = "Profile"

        firebaseAuth = FirebaseAuth.getInstance()

        db = FirebaseFirestore.getInstance()
        val uid = firebaseAuth.currentUser?.uid

        if (uid != null) {
            db.collection("Users").document(uid)
                .get().addOnSuccessListener {
                    if (it.data != null) {
                        binding.tvEmail.text=firebaseAuth.currentUser?.email.toString()
                        binding.tvName.text = it.data?.get("userName").toString()
                        userName=it.data?.get("userName").toString()
                    }
                }
        }

       binding.tvEditProfile.setOnClickListener{
           setFragmentResult(
               "requestKey",
               bundleOf(
                   "uid" to uid,
                   "userName" to userName,
                   )
           )
           findNavController().navigate(R.id.action_profileScreen_to_editProfileFragment)
       }

        binding.tvTerms.setOnClickListener{
            findNavController().navigate(R.id.action_profileScreen_to_termsAndConditionsFragment)
        }

        binding.tvContactUs.setOnClickListener{
            findNavController().navigate(R.id.action_profileScreen_to_contactUsFragment)
        }

        binding.tvLogOut.setOnClickListener {
            showLogoutDialog()
        }
        return binding.root
    }
    private fun showLogoutDialog() {
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

    private fun logoutUser(){

        firebaseAuth.signOut()
        val intent = Intent(activity, StartActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}

