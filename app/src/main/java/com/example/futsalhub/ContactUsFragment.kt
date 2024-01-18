package com.example.futsalhub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.futsalhub.databinding.FragmentContactUsBinding


class ContactUsFragment : Fragment() {
    private lateinit var binding: FragmentContactUsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactUsBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = "Contact Us"

        return binding.root
    }


}