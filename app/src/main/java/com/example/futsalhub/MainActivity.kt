package com.example.futsalhub

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class MainActivity : AppCompatActivity(), PaymentResultListener {
    private lateinit var navController: NavController
    private val sharedViewModel by viewModels<SharedViewModel>()
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setting toolbar as the actionbar as there is no actionbar (actionbar is needed for setupActionBarWithNavController)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_container
        ) as NavHostFragment
        navController = navHostFragment.navController

        // Setup the bottom navigation view with navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        // Setup the ActionBar with navController and 3 top level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.listScreen, R.id.bookingHistoryScreen, R.id.profileScreen)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // make bottom nav visible only on certain pages
        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
            if (nd.id == R.id.groundScreen || nd.id == R.id.editProfileFragment || nd.id == R.id.termsAndConditionsFragment || nd.id == R.id.contactUsFragment) {
                bottomNavigationView.visibility = View.GONE
            } else {
                bottomNavigationView.visibility = View.VISIBLE
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    override fun onPaymentSuccess(p0: String?) {
        try {
            sharedViewModel.sharedData.observe(this, Observer { originalData ->

                val data: MutableMap<String, Any> = originalData.toMutableMap()

                // Handle the transferred data in the activity
                // You can perform any necessary actions with the key-value pairs here
                val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
                FirebaseFirestore.getInstance().collection("Users").document(uid).get()
                    .addOnSuccessListener { document ->
                        val userName = document.data?.get("userName").toString()

                        //add doc in booking
                        val docRef =
                            FirebaseFirestore.getInstance().collection("Bookings").document()

                        val bookingId = docRef.id
                        data["bookingId"] = bookingId
                        data["customerName"] = userName
                        data["customerId"] = uid
                        data["bookingRating"] = "0"
                        data["transactionId"] = p0.toString()

                        val bookingTime = data["bookingTime"].toString()
                        val bookingDate = data["bookingDate"].toString()
                        val groundId = data["groundId"].toString()
                        val groundName = data["groundName"].toString()


                        //add booking doc
                        docRef.set(data).addOnSuccessListener {
                            Log.i("booking123", "success")
                        }.addOnFailureListener {
                            Log.i("booking123", it.message.toString())

                        }

                        //update boolean value of booked timeslot
                        val fieldPath = FieldPath.of(bookingTime, "booked")
                        val updateTask = FirebaseFirestore.getInstance().collection("FutsalGrounds")
                            .document(groundId).collection("TimeSlots")
                            .document(bookingDate).update(fieldPath, true)

                        updateTask.addOnSuccessListener {
                            refreshTimeSlots()
                        }.addOnFailureListener {
                            Log.i("update123", it.message.toString())
                        }


                        val emailTemplate = """
    Dear $userName,
    
    We are thrilled to inform you that your ground booking at $groundName has been successfully confirmed! We appreciate your choice and look forward to providing you with a fantastic experience.
    
    Booking Details:
    
    Venue: $groundName
    Date: $bookingDate
    Time: $bookingTime
    Booking ID: $bookingId
    
    Please make sure to review the details above to ensure they align with your expectations. If you have any special requests or additional requirements, please feel free to contact us.
    
    We hope you enjoy your time at $groundName and have a memorable experience. Thank you for choosing us for your event, and we look forward to serving you.
    
    Best regards,
    
    FutsalHub Admin
    futsalhub123@gmail.com
""".trimIndent()

                        lifecycleScope.launch {
                            sendEmail(emailTemplate)
                        }

                    }.addOnFailureListener {
                        Log.i("pay123", it.message.toString())
                    }
            })

        } catch (e: Exception) {
            Log.e("pay1234", e.message, e)
        }
    }

    private suspend fun sendEmail(emailTemplate: String) = withContext(Dispatchers.IO) {
        // Replace these with your actual email and password
        val username = "rifabahamed@gmail.com"
        val password = "blackwhite"

        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.port"] = "587"

        val session = Session.getInstance(props,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })

        try {
            val message = MimeMessage(session)
            message.setFrom(InternetAddress("rifabahamed@gmail.com"))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(FirebaseAuth.getInstance().currentUser?.email.toString()))
            message.subject = "Booking Successful"
            message.setText(emailTemplate)

            Transport.send(message)
            Log.e("email123","done")

        } catch (e: MessagingException) {
            Log.e("email123", e.message, e)
        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        try {
            Log.i("pay123", p1.toString())
        } catch (e: Exception) {
            Log.e("pay1233", e.message, e)
        }
    }

    private fun refreshTimeSlots() {
        // Find the fragment by its tag or ID
        val fragment = supportFragmentManager.fragments[0].childFragmentManager.fragments[0] as GroundFragment
        fragment.refreshFragment()
    }


}


