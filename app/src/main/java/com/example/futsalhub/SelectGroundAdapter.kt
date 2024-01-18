package com.example.futsalhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class SelectGroundAdapter(private val dataList: List<HashMap<String, Any?>>, private val onItemClick: (HashMap<String, Any?>) -> Unit) :
    RecyclerView.Adapter<SelectGroundAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groundName: TextView = itemView.findViewById(R.id.tvGroundName)
        val ovrRating: TextView = itemView.findViewById(R.id.tvGroundRating)
        val location: TextView = itemView.findViewById(R.id.tvGroundLocation)

        // views for each field you want to display

        init {
            itemView.setOnClickListener {
                val item = dataList[adapterPosition]
                onItemClick(item)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.select_ground_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]

        // Set data to views in the ViewHolder
        holder.groundName.text = currentItem["groundName"].toString()
        holder.ovrRating.text = currentItem["ovrRating"].toString()
        holder.location.text = currentItem["location"].toString()

    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
