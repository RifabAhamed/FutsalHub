package com.example.futsalhub

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.futsalhub.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        dismissLoading()

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            val password = binding.etLoginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if (it.isSuccessful) {
                        showLoading()
                        checkAccessLevel()
                    } else {
                        try {
                            throw it.exception!!
                        }  catch (e: FirebaseAuthInvalidCredentialsException) {
                            binding.tfLoginEmail.error = it.exception?.message.toString()
                            removeEmailError()
                            binding.tfLoginPassword.error = it.exception?.message.toString()
                            removePasswordError()
                        } catch (e: FirebaseAuthInvalidUserException) {
                            binding.tfLoginEmail.error = it.exception?.message.toString()
                            removeEmailError()
                        }  catch (e: Exception) {
                            Log.e(ContentValues.TAG, e.message!!)
                        }
                    }
                }

            } else {
                if (email.isEmpty()) {
                    binding.tfLoginEmail.error = "Enter email"
                    removeEmailError()
                }
                if (password.isEmpty()) {
                    binding.tfLoginPassword.error = "Enter password"
                    removePasswordError()
                }
            }
        }

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreen_to_registrationScreen)
        }

        binding.tvForgot.setOnClickListener {
            val email = binding.etLoginEmail.text.toString()
            if (email.isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            activity,
                            "Reset password link sent to your email",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        binding.tfLoginEmail.error = it.exception?.message.toString()
                        removeEmailError()
                    }
                }
            } else {
                binding.tfLoginEmail.error = "Enter your email"
                removeEmailError()
            }
        }
        return binding.root
    }

    private fun removeEmailError() {
        binding.etLoginEmail.addTextChangedListener {
            binding.tfLoginEmail.error = null
        }
    }

    private fun removePasswordError() {
        binding.etLoginPassword.addTextChangedListener {
            binding.tfLoginPassword.error = null
        }
    }

    private fun checkAccessLevel() {
        db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val ref = uid?.let { db.collection("Users").document(it) }
        ref?.get()?.addOnSuccessListener {
            if (it != null) {
                val accessLevel = it.data?.get("accessLevel").toString()
                if (accessLevel == "0") {
                    if (firebaseAuth.currentUser!!.isEmailVerified) {
                        val intent = Intent(activity, MainActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                        dismissLoading()
                    } else {
                        binding.tfLoginEmail.error = "Please verify your email"
                    }
                } else if (accessLevel == "1") {
                    val intent = Intent(activity, GroundAdminActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                    dismissLoading()
                } else {
                    val intent = Intent(activity, OverallAdminActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                    //dismissLoading()
                }
            }
        }
            ?.addOnFailureListener {
                Log.i("mytag", "fail")
            }
    }

    private fun showLoading() {
        binding.overlayView.visibility = View.VISIBLE

        // Disable interaction
        requireActivity().window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun dismissLoading() {
        binding.overlayView.visibility = View.GONE

        // Enable interaction
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }


    //remember login
    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            checkAccessLevel()
        }
    }
}