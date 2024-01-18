package com.example.futsalhub

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.futsalhub.databinding.FragmentRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationFragment : Fragment() {
    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnRegister.setOnClickListener {
            val name = binding.etRegisterName.text.toString()
            val email = binding.etRegisterEmail.text.toString()
            val password = binding.etRegisterPassword.text.toString()
            val confirmPassword = binding.etRegisterConfirmPassword.text.toString()


            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { it ->
                            if (it.isSuccessful) {
                                firebaseAuth.currentUser?.sendEmailVerification()
                                    ?.addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            val cid = firebaseAuth.currentUser?.uid
                                            // Store customer data in db
                                            val user = hashMapOf(
                                                "userId" to cid,
                                                "userName" to name,
                                                "accessLevel" to "0",
                                            )

                                            if (cid != null) {
                                                db.collection("Users")
                                                    .document(cid)
                                                    .set(user)
                                                    .addOnSuccessListener {
                                                        Log.d(
                                                            ContentValues.TAG,
                                                            "DocumentSnapshot added"
                                                        )
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.w(
                                                            ContentValues.TAG,
                                                            "Error adding document",
                                                            e
                                                        )
                                                    }
                                            }

                                            Toast.makeText(
                                                activity,
                                                "Link sent to your email. Please verify your email",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            findNavController().navigate(R.id.action_registrationScreen_to_loginScreen)
                                        } else {
                                            Toast.makeText(
                                                activity,
                                                "Registration Failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                try {
                                    throw it.exception!!
                                } catch (e: FirebaseAuthWeakPasswordException) {
                                    binding.tfRegisterPassword.error =
                                        it.exception?.message.toString()
                                    removePasswordError()
                                } catch (e: FirebaseAuthInvalidCredentialsException) {
                                    binding.tfRegisterEmail.error = it.exception?.message.toString()
                                    removeEmailError()
                                } catch (e: FirebaseAuthUserCollisionException) {
                                    binding.tfRegisterEmail.error = it.exception?.message.toString()
                                    removeEmailError()
                                } catch (e: Exception) {
                                    Log.e(ContentValues.TAG, e.message!!)
                                }
                            }
                        }
                } else {
                    binding.tfRegisterConfirmPassword.error =
                        "Password and Confirm Password do not match"
                    removeConfirmPasswordError()
                }
            } else {
                if (name.isEmpty()) {
                    binding.tfRegisterName.error = "Enter name"
                    removeNameError()
                }
                if (email.isEmpty()) {
                    binding.tfRegisterEmail.error = "Enter email"
                    removeEmailError()
                }
                if (password.isEmpty()) {
                    binding.tfRegisterPassword.error = "Enter password"
                    removePasswordError()
                }
                if (confirmPassword.isEmpty()) {
                    binding.tfRegisterConfirmPassword.error = "Enter confirm password"
                    removeConfirmPasswordError()
                }
            }
        }

        binding.tvSignIn.setOnClickListener {
            it.findNavController().navigate(R.id.action_registrationScreen_to_loginScreen)
        }

        return binding.root
    }

    private fun removeNameError() {
        binding.etRegisterName.addTextChangedListener {
            binding.tfRegisterName.error = null
        }
    }

    private fun removeEmailError() {
        binding.etRegisterEmail.addTextChangedListener {
            binding.tfRegisterEmail.error = null
        }
    }

    private fun removePasswordError() {
        binding.etRegisterPassword.addTextChangedListener {
            binding.tfRegisterPassword.error = null
        }
    }

    private fun removeConfirmPasswordError() {
        binding.etRegisterConfirmPassword.addTextChangedListener {
            binding.tfRegisterConfirmPassword.error = null
        }
    }
}

