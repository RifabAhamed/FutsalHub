package com.example.futsalhub

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import com.example.futsalhub.databinding.FragmentEditDetailsBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class EditDetailsFragment : Fragment() {
    private lateinit var binding: FragmentEditDetailsBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditDetailsBinding.inflate(inflater, container, false)

        setFragmentResultListener("selectKey2") { _, bundle ->
            val groundId = bundle.getString("groundId").toString()
            db = FirebaseFirestore.getInstance()
            db.collection("FutsalGrounds").document(groundId).get()
                .addOnSuccessListener { document ->
                    val groundName = document.data?.get("groundName").toString()
                    val groundLocation = document.data?.get("location").toString()
                    val description = document.data?.get("description").toString()

                    var flag1 = "false"
                    var flag2 = "false"
                    var flag3 = "false"

                    binding.etEditGroundName.setText(groundName)
                    binding.etEditGroundLocation.setText(groundLocation)
                    binding.etEditGroundDescription.setText(description)

                    binding.etEditGroundName.addTextChangedListener {
                        flag1 = "true"
                        binding.btnEditDetails.isClickable = true
                        binding.btnEditDetails.alpha = 1f
                    }

                    binding.etEditGroundLocation.addTextChangedListener {
                        flag2 = "true"
                        binding.btnEditDetails.isClickable = true
                        binding.btnEditDetails.alpha = 1f
                    }

                    binding.etEditGroundDescription.addTextChangedListener {
                        flag3 = "true"
                        binding.btnEditDetails.isClickable = true
                        binding.btnEditDetails.alpha = 1f
                    }

                    binding.btnEditDetails.setOnClickListener {
                        val groundRef = db.collection("FutsalGrounds").document(groundId)

                        // Check the flags and update fields accordingly
                        if (flag1 == "true") {
                            val updatedGroundName = binding.etEditGroundName.text.toString()
                            groundRef.update("groundName", updatedGroundName).addOnSuccessListener {
                                binding.tfEditGroundName.helperText =
                                    "Ground name change successful"
                                removeNameSuccess()
                            }.addOnFailureListener {
                                binding.tfEditGroundName.error = "Ground name change failed"
                                removeNameError()
                            }
                        }

                        if (flag2 == "true") {
                            val updatedGroundLocation = binding.etEditGroundLocation.text.toString()
                            groundRef.update("groundLocation", updatedGroundLocation)
                                .addOnSuccessListener {
                                    binding.tfEditGroundLocation.helperText =
                                        "Ground location change successful"
                                    removeLocationSuccess()
                                }.addOnFailureListener {
                                    binding.tfEditGroundLocation.error =
                                        "Ground location change failed"
                                    removeLocationError()
                                }
                        }

                        if (flag3 == "true") {
                            val updatedDescription = binding.etEditGroundDescription.text.toString()
                            groundRef.update("description", updatedDescription)
                                .addOnSuccessListener {
                                    binding.tfEditGroundDescription.helperText =
                                        "Ground description change successful"
                                    removeDescriptionSuccess()
                                }.addOnFailureListener {
                                    binding.tfEditGroundDescription.error =
                                        "Ground description change failed"
                                    removeDescriptionError()
                                }
                        }

                        flag1 = "false"
                        flag2 = "false"
                        flag3 = "false"
                        binding.btnEditDetails.isClickable = false
                        binding.btnEditDetails.alpha = 0.5f
                    }
                }.addOnFailureListener {
                    Log.i("details123", it.toString())
                }



            binding.btnAdd.setOnClickListener {
                if (binding.etAddDate.text.toString().isNotEmpty() && binding.etAddTime.text.toString()
                        .isNotEmpty() && binding.etAddPrice.text.toString().isNotEmpty()
                ) {
                    val date = binding.etAddDate.text.toString()
                    val price = binding.etAddPrice.text.toString()
                    val time = binding.etAddTime.text.toString()

                    val data = hashMapOf(
                        time to hashMapOf(
                            "booked" to false,
                            "price" to price,
                            "time" to time,
                        )
                    )

                    // Update the document with the HashMap field
                    FirebaseFirestore.getInstance().collection("FutsalGrounds").document(groundId)
                        .collection("TimeSlots").document(date).set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            Log.e("edit123", "add success")
                            binding.tfAddPrice.helperText =
                                "Successful"
                            removeAddPriceSuccess()
                        }
                        .addOnFailureListener { e ->
                            binding.tfAddPrice.error = e.message.toString()
                            Log.e("edit123", e.toString())
                            removeAddPriceError()
                        }
                } else {
                    if (binding.etAddDate.text.toString().isEmpty()) {
                        binding.tfAddDate.error = "Enter date"
                        removeAddDateError()
                    }
                    if (binding.etAddTime.text.toString().isEmpty()) {
                        binding.tfAddTime.error = "Enter time"
                        removeAddTimeError()
                    }
                    if (binding.etAddPrice.text.toString().isEmpty()) {
                        binding.tfAddPrice.error = "Enter price"
                        removeAddPriceError()
                    }
                }

            }


            binding.btnRemove.setOnClickListener {
                if (binding.etRemoveDate.text.toString().isNotEmpty() && binding.etRemoveTime.text.toString()
                        .isNotEmpty()
                ) {
                    val date = binding.etRemoveDate.text.toString()
                    val time = binding.etRemoveTime.text.toString()

                    val updates = hashMapOf<String, Any>(
                        time to FieldValue.delete()
                    )
                    // Update the document with the HashMap field
                    val docRef =
                        FirebaseFirestore.getInstance().collection("FutsalGrounds").document(groundId)
                            .collection("TimeSlots").document(date)

                    docRef.update(updates)
                        .addOnSuccessListener {
                            Log.e("edit123", "add success")
                            binding.tfRemoveTime.helperText =
                                "Successful"
                            removeRemoveTimeSuccess()
                        }
                        .addOnFailureListener { e ->
                            binding.tfRemoveTime.error = e.message.toString()
                            Log.e("edit123", e.toString())
                            removeRemoveTimeError()
                        }
                } else {
                    if (binding.etRemoveDate.text.toString().isEmpty()) {
                        binding.tfRemoveDate.error = "Enter date"
                        removeRemoveDateError()
                    }
                    if (binding.etRemoveTime.text.toString().isEmpty()) {
                        binding.tfRemoveTime.error = "Enter time"
                        removeRemoveTimeError()
                    }
                }
            }


        }
        return binding.root
    }

    private fun removeRemoveTimeSuccess() {
        binding.etRemoveTime.addTextChangedListener {
            binding.tfRemoveTime.helperText = null
        }
    }

    private fun removeRemoveTimeError() {
        binding.etRemoveTime.addTextChangedListener {
            binding.tfRemoveTime.error = null
        }
    }

    private fun removeRemoveDateError() {
        binding.etRemoveDate.addTextChangedListener {
            binding.tfRemoveDate.error = null
        }
    }

    private fun removeAddPriceSuccess() {
        binding.etAddPrice.addTextChangedListener {
            binding.tfAddPrice.helperText = null
        }
    }

    private fun removeAddDateError() {
        binding.etAddDate.addTextChangedListener {
            binding.tfAddDate.error = null
        }
    }

    private fun removeAddTimeError() {
        binding.etAddTime.addTextChangedListener {
            binding.tfAddTime.error = null
        }
    }

    private fun removeAddPriceError() {
        binding.etAddPrice.addTextChangedListener {
            binding.tfAddPrice.error = null
        }
    }

    private fun removeNameSuccess() {
        binding.etEditGroundName.addTextChangedListener {
            binding.tfEditGroundName.helperText = null
        }
    }

    private fun removeLocationSuccess() {
        binding.etEditGroundLocation.addTextChangedListener {
            binding.tfEditGroundLocation.helperText = null
        }
    }

    private fun removeDescriptionSuccess() {
        binding.etEditGroundDescription.addTextChangedListener {
            binding.tfEditGroundDescription.helperText = null
        }
    }

    private fun removeNameError() {
        binding.etEditGroundName.addTextChangedListener {
            binding.tfEditGroundName.error = null
        }
    }

    private fun removeLocationError() {
        binding.etEditGroundLocation.addTextChangedListener {
            binding.tfEditGroundLocation.error = null
        }
    }

    private fun removeDescriptionError() {
        binding.etEditGroundDescription.addTextChangedListener {
            binding.tfEditGroundDescription.error = null
        }
    }
}