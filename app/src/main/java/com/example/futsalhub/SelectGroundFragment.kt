package com.example.futsalhub

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futsalhub.databinding.FragmentSelectGroundBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SelectGroundFragment : Fragment() {
    private lateinit var binding: FragmentSelectGroundBinding
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=FragmentSelectGroundBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(context)
        recyclerView = binding.rvSelectGround
        recyclerView.layoutManager = layoutManager

        val dataList = mutableListOf<HashMap<String, Any?>>()

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        FirebaseFirestore.getInstance()
            .collection("FutsalGrounds")
            .whereEqualTo("groundAdminId", uid)
            .get().addOnSuccessListener { documents ->
                for (document in documents) {

                    val newData = HashMap<String, Any?>()
                    newData.putAll(document.data)
                    dataList.add(newData)

                }
                val adapter = SelectGroundAdapter(dataList, ::onItemClick)
                recyclerView.adapter = adapter
            }.addOnFailureListener { exception ->
                Log.i("oi21345", exception.toString())
            }

       return binding.root
    }

    private fun onItemClick(data: HashMap<String, Any?>) {
        setFragmentResult("selectKey", bundleOf("groundId" to data["groundId"]))
        findNavController().navigate(R.id.action_selectGroundFragment_to_viewBookingsFragment)
    }

}