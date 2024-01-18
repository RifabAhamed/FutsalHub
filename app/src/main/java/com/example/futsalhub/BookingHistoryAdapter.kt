package com.example.futsalhub

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale


class BookingHistoryAdapter(private val dataList: List<HashMap<String, Any?>>) :
    RecyclerView.Adapter<BookingHistoryAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onCancelBookingClick(position: Int, bookingId: String, bookingTime: String, bookingDate:String, groundId: String)
        fun onRatingBookingClick(position: Int, rating: Float,groundId: String, bookingId: String)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tvBookingDate)
        val time: TextView = itemView.findViewById(R.id.tvBookingTime)
        val location: TextView = itemView.findViewById(R.id.tvGroundLocation)
        val groundName: TextView = itemView.findViewById(R.id.tvGroundName)
        val price: TextView = itemView.findViewById(R.id.tvGroundPrice)
        val cancelBooking: Button = itemView.findViewById(R.id.btnCancelBooking)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)

        init {
            cancelBooking.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currentItem = dataList[position]
                    val bookingId = currentItem["bookingId"].toString()
                    val bookingTime = currentItem["bookingTime"].toString()
                    val bookingDate = currentItem["bookingDate"].toString()
                    val groundId = currentItem["groundId"].toString()

                    itemClickListener?.onCancelBookingClick(position, bookingId, bookingTime, bookingDate, groundId)
                }
            }

            ratingBar.setOnRatingBarChangeListener { ratingBar , rating, fromUser ->
                ratingBar.isEnabled = false

                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION && fromUser) {
                    val currentItem = dataList[position]
                    val bookingId = currentItem["bookingId"].toString()
                    val groundId = currentItem["groundId"].toString()
                    itemClickListener?.onRatingBookingClick(position, rating,groundId, bookingId)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.booking_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        val bookingTime: String = currentItem["bookingTime"].toString()
        val bookingDate: String = currentItem["bookingDate"].toString()
        val date =
            LocalDate.parse(bookingDate, DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH))
        val rating: String = currentItem["bookingRating"].toString()

        holder.date.text = bookingDate
        holder.location.text = currentItem["groundLocation"].toString()
        holder.price.text = "₹" + currentItem["bookingPrice"].toString()
        holder.time.text = bookingTime
        holder.groundName.text = currentItem["groundName"].toString()


        // Disable the cancel booking button 1 hour before bookingTime
        val currentTimeMillis = LocalTime.now().toSecondOfDay() * 1000
        val millis = LocalTime.parse(bookingTime)
        val bookingTimeMillis = millis.toSecondOfDay() * 1000
        val oneHourInMillis = 60 * 60 * 1000 // 1 hour in milliseconds

        val currentDate = LocalDate.now()

        if (currentDate == date) {
            if (bookingTimeMillis - currentTimeMillis <= oneHourInMillis) {
                holder.cancelBooking.isClickable = false
                holder.cancelBooking.alpha = 0.5f
            } else {
                holder.cancelBooking.isClickable = true
            }
        } else if (currentDate > date) {
            holder.cancelBooking.isClickable = false
            holder.cancelBooking.alpha = 0.5f
        } else if (currentDate < date) {
            holder.cancelBooking.isClickable = true
        }


        if (rating != "0") {
            holder.ratingBar.rating = rating.toFloat()
            holder.ratingBar.isEnabled = false
            Log.i("rate111", "1")

        } else {
            Log.i("rate111", "2")
            // Enable the rating bar after bookingTime
            if (currentDate == date) {
                if (currentTimeMillis > bookingTimeMillis) {
                    holder.ratingBar.isEnabled = true
                    holder.ratingBar.alpha = 1f
                } else {
                    holder.ratingBar.isEnabled = false
                    holder.ratingBar.alpha = 0.5f
                }
            } else if (currentDate > date) {
                holder.ratingBar.isEnabled = true
                holder.ratingBar.alpha = 1f
            } else if (currentDate < date) {
                holder.ratingBar.isEnabled = false
                holder.ratingBar.alpha = 0.5f
            }
        }

    }


    override fun getItemCount(): Int {
        return dataList.size
    }
}
