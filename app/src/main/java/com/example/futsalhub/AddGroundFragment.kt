package com.example.futsalhub

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.futsalhub.databinding.FragmentAddGroundBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddGroundFragment : Fragment() {

    private lateinit var binding: FragmentAddGroundBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAddGroundBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.button.setOnClickListener {
            val grdname = binding.editTextText2.text.toString()
            val location = binding.editTextText3.text.toString()
            val grdtype = binding.editTextText10.text.toString()
            val minprice = binding.editTextText7.text.toString()
            val description = binding.editTextText4.text.toString()
            val name = binding.editTextText6.text.toString()

            if (name.isNotEmpty() && grdname.isNotEmpty() && location.isNotEmpty() && grdtype.isNotEmpty() && minprice.isNotEmpty() && description.isNotEmpty() ) {

               val groundAdminId = "K7i4IcZ9LvZE8rTyIpMbMkAkVLv1"
                   //binding.userId.toString()

                val user = hashMapOf(
                    "userId" to groundAdminId,
                    "userName" to name,
                    "accessLevel" to "1",
                )

                db.collection("Users")
                    .document(groundAdminId)
                    .set(user)
                    .addOnSuccessListener {
                        Toast.makeText(
                            activity,
                            "Registration successful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            activity,
                            "Error writing user data to Firestore",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Firestore", "Error writing user document", e)
                    }

                val newDocument = hashMapOf(
                    "groundName" to grdname,
                    "minPrice" to minprice,
                    "location" to location,
                    "ovrRating" to "0",
                    "groundType" to grdtype,
                    "description" to description,
                    "groundAdminId" to groundAdminId,
                    "ovrRatingCount" to "1"
                    //"groundId" is omitted here as it will be added later
                )

                db.collection("FutsalGrounds")
                    .add(newDocument)
                    .addOnSuccessListener { documentReference ->
                        val groundId = documentReference.id

                        // Now, add "groundId" to the newDocument
                        newDocument["groundId"] = groundId

                        // Update the futsal ground document with the groundId
                        db.collection("FutsalGrounds")
                            .document(documentReference.id)
                            .set(newDocument)
                            .addOnSuccessListener {
                                // Futsal ground document updated successfully
                                Toast.makeText(
                                    activity,
                                    "Ground Added Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    activity,
                                    "Unsuccessful",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                Log.e(
                                    "Firestore",
                                    "Error updating futsal ground document",
                                    e
                                )
                            }
                    }.addOnFailureListener {e->
                        Log.e(
                            "Firestore",
                            "Error adding futsal ground document",
                            e
                        )
                    }
            } else {
                Toast.makeText(
                    activity,
                    "Empty",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
        return binding.root
    }
}