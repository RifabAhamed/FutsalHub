package com.example.futsalhub

import android.app.SearchManager
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.clear
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.futsalhub.databinding.FragmentHomeBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin


class HomeFragment : Fragment() {
    var groundAdapter: GroundListAdapter? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: FragmentHomeBinding
    private lateinit var dataStore: DataStore<Preferences>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        dataStore = requireContext().createDataStore(name = "settings")

        customizeToolbar()

        // Set up RecyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = layoutManager

        val dataList = mutableListOf<HashMap<String, Any?>>()

        FirebaseFirestore.getInstance()
            .collection("FutsalGrounds")
            .orderBy("groundName").get().addOnSuccessListener { documents ->
                for (document in documents) {

                    val newData = HashMap<String, Any?>()
                    newData.putAll(document.data)
                    dataList.add(newData)

                }
                val adapter = MyAdapter(dataList, ::onItemClick)
                recyclerView.adapter = adapter
            }.addOnFailureListener { exception ->
                Log.i("oi21345", exception.toString())
            }

        //handle ground searches
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        binding.searchBar.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            //search when "enter" pressed
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchQuery ->
                    val searchQueryText = searchQuery.capitalizeWords()
                    val searchResultQuery: Query = createSearchQuery(searchQueryText)
                    createRecyclerView(searchResultQuery)
                }
                return false
            }

            //search on every letter entered
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { text ->
                    val searchQueryText = text.capitalizeWords()
                    val searchResultQuery: Query = createSearchQuery(searchQueryText)
                    createRecyclerView(searchResultQuery)
                }
                return false
            }
        })





        return binding.root
    }

    private fun createRecyclerView(query: Query) {
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<GroundListModel> =
            FirestoreRecyclerOptions.Builder<GroundListModel>()
                .setQuery(query, GroundListModel::class.java)
                .build()

        groundAdapter = GroundListAdapter(firestoreRecyclerOptions, ::handleUserData)
        groundAdapter!!.startListening()
        recyclerView.adapter = groundAdapter
    }

    private fun createSearchQuery(searchText: String): Query {
        return FirebaseFirestore.getInstance()
            .collection("FutsalGrounds")
            .orderBy("groundName")
            .startAt(searchText)
            .endAt("$searchText~")
    }

    private fun customizeToolbar() {
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.futsalhub_logo)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.title = ""
        (activity as AppCompatActivity).supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                resources.getColor(R.color.green)
            )
        )
    }

    // to calculate distance between coordinates (user and ground)
    private fun distance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = (sin(deg2rad(lat1))
                * sin(deg2rad(lat2))
                + (cos(deg2rad(lat1))
                * cos(deg2rad(lat2))
                * cos(deg2rad(theta))))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    fun String.capitalizeWords(): String = split(" ").map { it.capitalize() }.joinToString(" ")

    private fun handleUserData(data: GroundListModel) {
        setFragmentResult("requestKey", bundleOf("groundId" to data.groundId))
        findNavController().navigate(R.id.action_listScreen_to_groundScreen)
    }

    private fun onItemClick(data: HashMap<String, Any?>) {
        setFragmentResult("requestKey", bundleOf("groundId" to data["groundId"]))
        findNavController().navigate(R.id.action_listScreen_to_groundScreen)
    }

    private suspend fun clearDataFromStore() {
        lifecycleScope.launch {
            try {
                // Clear or delete the required keys or all data from DataStore
                dataStore.edit { preferences ->
                    preferences.clear() // Clear all data
                }
            } catch (e: Exception) {
                Log.e("DataStore", "Error clearing DataStore: ${e.message}")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        groundAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        groundAdapter?.stopListening()
    }

    override fun onPause() {
        super.onPause()

        // Delete DataStore data when user navigates away from this fragment
        lifecycleScope.launch {
            clearDataFromStore()
        }
    }

}