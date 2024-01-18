package com.example.futsalhub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.futsalhub.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore


class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = "Edit Profile"

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setFragmentResultListener("requestKey") { _, bundle ->

            val uid = bundle.getString("uid").toString()
            val username = bundle.getString("userName").toString()
            binding.etEditName.setText(username)


            binding.etEditName.addTextChangedListener {
                binding.btnEditName.isClickable = true
                binding.btnEditName.alpha = 1f
            }

            binding.btnEditName.setOnClickListener {
                val uname = binding.etEditName.text.toString()

                val docRef = db.collection("Users").document(uid)
                docRef.update("userName", uname).addOnSuccessListener {
                    binding.tfEditName.helperText = "Name change successful"
                    removeNameSuccess()
                }.addOnFailureListener {
                    binding.tfEditName.error = "Name change failed"
                    removeNameError()
                }
            }

            binding.btnEditPassword.setOnClickListener {
                if (binding.etCurrentPass.text.toString()
                        .isNotEmpty() && binding.etEditPass.text.toString()
                        .isNotEmpty() && binding.etConfirmPass.text.toString().isNotEmpty()
                ) {
                    if (binding.etEditPass.text.toString() == binding.etConfirmPass.text.toString()) {
                        val user: FirebaseUser? = FirebaseAuth.getInstance().currentUser

                        val email = user?.email
                        val currentPassword = binding.etConfirmPass.text.toString()
                        val credential =
                            EmailAuthProvider.getCredential(email!!, currentPassword)

                        user.reauthenticate(credential)
                            .addOnCompleteListener { reAuthTask ->
                                if (reAuthTask.isSuccessful) {
                                    val newPasswordString = binding.etEditPass.text.toString()
                                    // User has been re-authenticated, proceed to update password

                                    user.updatePassword(newPasswordString)
                                        .addOnCompleteListener { passwordUpdateTask ->
                                            if (passwordUpdateTask.isSuccessful) {
                                                binding.tfConfirmPass.helperText =
                                                    "Password update successful"
                                                removePasswordSuccess()
                                            } else {
                                                binding.tfConfirmPass.error =
                                                    "Password update failed: ${passwordUpdateTask.exception?.message}"
                                                removePasswordError()
                                            }
                                        }
                                } else {
                                    binding.tfCurrentPass.error =
                                        "Re-authentication failed: ${reAuthTask.exception?.message}"
                                    removeCurrentPassError()
                                }
                            }
                    } else {
                        binding.tfConfirmPass.error =
                            "Password and Confirm Password do not match"
                        removePasswordError()
                    }
                } else {
                    if (binding.etCurrentPass.text.toString().isEmpty()) {
                        binding.tfCurrentPass.error = "Enter curtent password"
                        removeCurrentPassError()
                    }
                    if (binding.etEditPass.text.toString().isEmpty()) {
                        binding.tfEditPass.error = "Enter new password"
                        removeEditPasswordError()
                    }
                    if (binding.etConfirmPass.text.toString().isEmpty()) {
                        binding.tfConfirmPass.error = "Re-enter new password"
                        removePasswordError()
                    }
                }
            }
        }

        return binding.root
    }

    private fun removeEditPasswordError() {
        binding.etEditPass.addTextChangedListener {
            binding.tfEditPass.error = null
        }
    }

    private fun removeCurrentPassError() {
        binding.etCurrentPass.addTextChangedListener {
            binding.tfCurrentPass.error = null
        }
    }

    private fun removePasswordError() {
        binding.etConfirmPass.addTextChangedListener {
            binding.tfConfirmPass.error = null
        }
    }

    private fun removePasswordSuccess() {
        binding.etCurrentPass.addTextChangedListener {
            binding.btnEditName.isClickable=false
            binding.btnEditName.alpha=0.5f
            binding.tfConfirmPass.helperText = null
        }
    }

    private fun removeNameError() {
        binding.etEditName.addTextChangedListener {
            binding.tfEditName.error = null
        }
    }

    private fun removeNameSuccess() {
        binding.etEditName.addTextChangedListener {
            binding.tfEditName.helperText = null
        }
    }


}