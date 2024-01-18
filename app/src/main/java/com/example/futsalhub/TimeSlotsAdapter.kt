package com.example.futsalhub

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.SortedMap

class TimeSlotsAdapter(
    private val context: Context,
    private val timeData: SortedMap<String, Any?>
) : BaseAdapter() {

    private var selectedPosition = -1

    private var onItemClick: ((timeSlot: String, price: String) -> Unit)? = null
    fun setOnItemClickListener(listener: (timeSlot: String, price: String) -> Unit) {
        this.onItemClick = listener
    }

    override fun getCount(): Int {
        return timeData.size
    }

    override fun getItem(position: Int): Any {
        val keysList = ArrayList(timeData.keys)
        return timeData[keysList[position]] ?: HashMap<String, Any?>()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var itemView = convertView
        val viewHolder: ViewHolder

        val timeSlot = getItem(position) as HashMap<String, Any?>
        val time = timeSlot["time"].toString()
        val price = timeSlot["price"].toString()
        val isBooked = timeSlot["booked"] as? Boolean ?: true
        val bTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))

        val dateNow = LocalDate.now()
        val timeNow = LocalTime.now()

        val timeZonedNow = ZonedDateTime.of(dateNow, timeNow, ZoneId.systemDefault())
        val zonedTime = ZonedDateTime.of(dateNow, bTime, ZoneId.systemDefault())

        val layoutRes = if (isBooked || timeZonedNow.isAfter(zonedTime)) {
            R.layout.time_slot_booked // Layout for booked items
        } else {
            R.layout.time_slot_item // Default layout for other items
        }

        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(layoutRes, parent, false)
            viewHolder = ViewHolder()
            viewHolder.timeTextView = itemView.findViewById(R.id.tvTime)
            viewHolder.priceTextView = itemView.findViewById(R.id.tvPrice)
            itemView.tag = viewHolder
        } else {
            viewHolder = itemView.tag as ViewHolder
        }

        viewHolder.timeTextView?.text = time
        viewHolder.priceTextView?.text = "₹$price"

        // Enable clicks for non-booked items
        if (!isBooked && !timeZonedNow.isAfter(zonedTime)) {
            itemView?.setOnClickListener {
                onItemClick?.invoke(time, price)
                if (selectedPosition != position) {
                    val previousSelectedPosition = selectedPosition
                    selectedPosition = position
                    notifyDataSetChanged() // Notify adapter of data changes
                }
            }

            // Set background and text colors based on selection
            if (position == selectedPosition) {
                itemView?.setBackgroundResource(R.drawable.time_selector)
            } else {
                itemView?.setBackgroundResource(R.color.bgblack)
            }
        } else {
            // Disable clicks for booked items
            itemView?.isClickable = false
            itemView?.isFocusable = false
            itemView?.setOnClickListener(null)
        }

        return itemView!!
    }

    private class ViewHolder {
        var timeTextView: TextView? = null
        var priceTextView: TextView? = null
    }
}
