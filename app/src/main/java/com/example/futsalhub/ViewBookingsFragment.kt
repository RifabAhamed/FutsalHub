package com.example.futsalhub

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futsalhub.databinding.FragmentViewBookingsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ViewBookingsFragment : Fragment() {
    private lateinit var binding: FragmentViewBookingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentViewBookingsBinding.inflate(inflater, container, false)

        setFragmentResultListener("selectKey") { _, bundle ->
            val groundId = bundle.getString("groundId").toString()
            val dataList = mutableListOf<HashMap<String, Any?>>()

            FirebaseFirestore.getInstance()
                .collection("Bookings")
                .whereEqualTo("groundId", groundId)
                .orderBy("bookingDate", Query.Direction.DESCENDING)
                .orderBy("bookingTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val newData = HashMap<String, Any?>()

                        // Handle each document here
                        val data = document.data
                        // Access the fields within the document
                        newData.putAll(data)

                        dataList.add(newData)
                        Log.i("booking123", newData.toString())

                    }
                    //Log.i("booking123", dataList.toString())
                    val layoutManager = LinearLayoutManager(context)
                    val recyclerView = binding.rvViewBookings
                    recyclerView.layoutManager = layoutManager

                    val adapter = ViewBookingsAdapter(dataList)
                    recyclerView.adapter = adapter

                }.addOnFailureListener { exception ->
                    Log.i("booking123", "Error getting documents: ", exception)
                }
        }
        return binding.root
    }
}