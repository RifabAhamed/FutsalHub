package com.example.futsalhub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.futsalhub.databinding.FragmentTermsAndConditionsBinding


class TermsAndConditionsFragment : Fragment() {
   private lateinit var binding: FragmentTermsAndConditionsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentTermsAndConditionsBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = "Terms and Conditions"

        return binding.root
    }


}