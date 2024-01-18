package com.example.futsalhub

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // Define a LiveData to hold the data
    private val _sharedData = MutableLiveData<HashMap<String, String>>()
    val sharedData: LiveData<HashMap<String, String>> get() = _sharedData

    // Function to update the data
    fun updateData(data: HashMap<String, String>) {
        _sharedData.value = data
    }
}

