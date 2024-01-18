package com.example.futsalhub

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class RemoveAdapter(
    private val dataList: List<HashMap<String, Any?>>) :
    RecyclerView.Adapter<RemoveAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onClick(position: Int, groundId: String)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groundName: TextView = itemView.findViewById(R.id.tvGroundName)
        val ovrRating: TextView = itemView.findViewById(R.id.tvGroundRating)
        val location: TextView = itemView.findViewById(R.id.tvGroundLocation)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currentItem = dataList[position]
                    val groundId = currentItem["groundId"].toString()
                    itemClickListener?.onClick(position, groundId)
                }
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

        // Set other views with respective fields from the HashMap
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

}