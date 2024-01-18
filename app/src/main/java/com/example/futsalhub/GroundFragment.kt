package com.example.futsalhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.example.futsalhub.databinding.CalendarBinding
import com.example.futsalhub.databinding.FragmentGroundBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.razorpay.Checkout
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


class GroundFragment : Fragment() {
    private lateinit var binding: FragmentGroundBinding
    private lateinit var groundId: String
    private lateinit var db: FirebaseFirestore
    private var selectedDate = LocalDate.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd")
    val dateFormatterInd = DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH)
    private lateinit var bookingDate: String
    private lateinit var bookingPrice: String
    private lateinit var bookingTime: String
    private lateinit var groundName: String
    private lateinit var groundLocation: String
    private lateinit var locationUrl: String
    private val sharedViewModel by activityViewModels<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ):

            View? {
        binding = FragmentGroundBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = "Book your ground"

        setFragmentResultListener("requestKey") { _, bundle ->
            groundId = bundle.getString("groundId").toString()

            FirebaseStorage.getInstance()
            db = FirebaseFirestore.getInstance()
            val ref = db.collection("FutsalGrounds").document(groundId)
            ref.get().addOnSuccessListener { document ->
                if (document != null) {
                    binding.tvDesc.text = document.data?.get("description").toString()
                    binding.tvOvr.text = document.data?.get("ovrRating").toString()

                    groundName = document.data?.get("groundName").toString()
                    groundLocation = document.data?.get("location").toString()

                    binding.tvGround.text = groundName
                    binding.tvLocation.text = groundLocation

                    locationUrl = document.data?.get("locationUrl").toString()

                    val groundImg = document.data?.get("groundImg").toString()
                    Glide.with(this).load(groundImg).into(binding.imageView)

                    val groundImg2 = document.data?.get("groundImg2").toString()
                    Glide.with(this).load(groundImg2).into(binding.imageView2)

                    val groundImg3 = document.data?.get("groundImg3").toString()
                    Glide.with(this).load(groundImg3).into(binding.imageView3)
                }
            }

            class DayViewContainer(view: View) : ViewContainer(view) {
                val bind = CalendarBinding.bind(view)
                lateinit var day: WeekDay

                init {
                    view.setOnClickListener {
                        if (bind.exSevenDayText.alpha.toInt() == 1) {
                            if (selectedDate != day.date) {
                                val oldDate = selectedDate
                                selectedDate = day.date
                                binding.exSevenCalendar.notifyDateChanged(day.date)
                                oldDate?.let { binding.exSevenCalendar.notifyDateChanged(it) }
                            }
                        }
                    }
                }

                fun bind(day: WeekDay) {
                    this.day = day
                    bind.exSevenDateText.text = dateFormatter.format(day.date)
                    bind.exSevenDayText.text =
                        day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)

                    val colorRes = if (day.date == selectedDate) {
                        R.color.black
                    } else {
                        R.color.green
                    }
                    bind.exSevenDateText.setTextColor(view.context.getColor(colorRes))
                    bind.exSevenDayText.setTextColor(view.context.getColor(colorRes))
                    bind.exSevenSelectedView.isVisible = day.date == selectedDate

                    if (bind.exSevenSelectedView.isVisible) {
                        bookingDate = day.date.format(dateFormatterInd)


                        db.collection("FutsalGrounds").document(groundId).collection("TimeSlots")
                            .document(bookingDate).get().addOnSuccessListener { document ->
                                val timeData = HashMap<String, Any?>()
                                val data = document.data

                                // Add data to the new data map
                                if (data != null) {
                                    timeData.putAll(data)
                                }

                                val gridView = binding.gvTimeSlots
                                val adapter =
                                    TimeSlotsAdapter(requireContext(), timeData.toSortedMap())
                                gridView.adapter = adapter

                                disableBookButton()

                                adapter.setOnItemClickListener { time, price ->
                                    enableBookButton()
                                    bookingTime=time
                                    bookingPrice = price
                                    binding.tvPrice.text = "₹$bookingPrice"
                                }
                            }.addOnFailureListener {
                                Log.i("1234", it.toString())
                            }
                    }
                }
            }

            binding.exSevenCalendar.dayBinder = object : WeekDayBinder<DayViewContainer> {
                override fun create(view: View) = DayViewContainer(view)
                override fun bind(container: DayViewContainer, data: WeekDay) {
                    container.bind(data)
                    container.bind.exSevenDayText.alpha =
                        if (LocalDate.now() <= data.date && LocalDate.now()
                                .plusDays(2) >= data.date
                        ) 1f else 0.3f
                    container.bind.exSevenDateText.alpha =
                        if (LocalDate.now() <= data.date && LocalDate.now()
                                .plusDays(2) >= data.date
                        ) 1f else 0.3f
                }
            }

            val currentDate = LocalDate.now()
            binding.exSevenCalendar.setup(
                currentDate.minusDays(2),
                currentDate.plusDays(4),
                currentDate.minusDays(2).dayOfWeek,
            )
            binding.exSevenCalendar.scrollToDate(LocalDate.now())
        }

        binding.tvDirections.setOnClickListener {
            openGoogleMapsNavigation(locationUrl)
        }

        binding.btnBook.setOnClickListener {
            try {
                val data = hashMapOf(
                    "groundLocation" to groundLocation,
                    "groundName" to groundName,
                    "groundId" to groundId,
                    "bookingTime" to bookingTime,
                    "bookingPrice" to bookingPrice,
                    "bookingDate" to bookingDate,
                )
                sharedViewModel.updateData(data)
                savePayment(bookingPrice.toInt())

            } catch (e: Exception) {
                Log.e("btnBookClick", "Exception: ${e.message}", e)
            }

        }

        Checkout.preload(requireActivity())

        return binding.root
    }

    private fun enableBookButton() {
        binding.btnBook.isClickable = true
        binding.btnBook.alpha = 1.0f
    }

    private fun disableBookButton() {
        binding.btnBook.isClickable = false
        binding.btnBook.alpha = 0.5f
    }

    private fun openGoogleMapsNavigation(locationUrl: String) {

        // Build the URI for Google Maps navigation
        val uri = Uri.parse(locationUrl)

        // Create an Intent with the ACTION_VIEW action and the URI
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)

        // Set the package to "com.google.android.apps.maps" to ensure it opens in Google Maps
        mapIntent.setPackage("com.google.android.apps.maps")

        // Check if there is an app that can handle the Intent
        if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
            // Start the activity
            startActivity(mapIntent)
        } else {
            // Handle the case where Google Maps is not installed
            // You may want to show a message or redirect to the Play Store to install Google Maps
        }
    }

    private fun savePayment(bookingPrice: Int) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_bTaaURb4EQKwlB")
        try {
            val options = JSONObject()
            options.put("name", "FutsalHub")
            options.put("description", "FutsalHub")
            options.put("theme.color", "#1ED660")
            options.put("theme.backdrop_color", "#121212")
            options.put("currency", "INR")
            options.put("amount", bookingPrice * 100)

            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val prefill = JSONObject()
            prefill.put("email", FirebaseAuth.getInstance().currentUser?.email.toString())
            options.put("prefill", prefill)

            checkout.open(requireActivity(), options)

            /*val payoutRequest = JSONObject()
            payoutRequest.put("account_number", "merchant_account_number")
            payoutRequest.put("amount", 1000) // Amount in paise
            payoutRequest.put("currency", "INR")
            payoutRequest.put("mode", "UPI")
            payoutRequest.put("purpose", "payout")
            payoutRequest.put("queue_if_low_balance", true)
            val payout = razorpay.Payouts.create(payoutRequest)*/

        } catch (e: Exception) {
            Log.i("exception123", e.toString())
        }
    }
    private fun fetchTimeSlotData() {
        // Fetch the updated time slot data after payment is completed
        db.collection("FutsalGrounds").document(groundId).collection("TimeSlots")
            .document(bookingDate).get().addOnSuccessListener { document ->
                val timeData = HashMap<String, Any?>()
                val data = document.data

                // Add data to the new data map
                if (data != null) {
                    timeData.putAll(data)
                }

                val gridView = binding.gvTimeSlots
                val adapter = TimeSlotsAdapter(requireContext(), timeData.toSortedMap())
                gridView.adapter = adapter

                disableBookButton()

                adapter.setOnItemClickListener { time, price ->
                    enableBookButton()
                    bookingTime = time
                    bookingPrice = price
                    binding.tvPrice.text = "₹$bookingPrice"
                }
            }.addOnFailureListener {
                Log.i("1234", it.toString())
            }
    }

    fun refreshFragment() {
        fetchTimeSlotData()
    }

}