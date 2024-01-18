package com.example.futsalhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ViewBookingsAdapter(private val dataList: List<HashMap<String, Any?>>) :
    RecyclerView.Adapter<ViewBookingsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tvBookingDate)
        val time: TextView = itemView.findViewById(R.id.tvBookingTime)
        val customerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val price: TextView = itemView.findViewById(R.id.tvGroundPrice)
        val transactionId: TextView = itemView.findViewById(R.id.tvTransaction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_bookings_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]

        holder.date.text = currentItem["bookingDate"].toString()
        holder.time.text = currentItem["bookingTime"].toString()
        holder.price.text = "₹"+currentItem["bookingPrice"].toString()
        holder.transactionId.text = currentItem["transactionId"].toString()
        holder.customerName.text = currentItem["customerName"].toString()
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
