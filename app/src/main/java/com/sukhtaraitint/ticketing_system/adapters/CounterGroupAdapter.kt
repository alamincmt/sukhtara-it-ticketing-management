package com.sukhtaraitint.ticketing_system.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sukhtaraitint.ticketing_system.R
import com.sukhtaraitint.ticketing_system.listeners.OnItemClickListener
import com.sukhtaraitint.ticketing_system.models.CounterGroups

class CounterGroupAdapter(var context: Context) : RecyclerView.Adapter<CounterGroupAdapter.ViewHolder>() {

    var dataList = emptyList<CounterGroups>()
    var onItemClickListener: OnItemClickListener? = null

    internal fun setDataList(dataList: List<CounterGroups>) {
        this.dataList = dataList
    }

    internal fun setOnItemClickListener(onItemClickListener: OnItemClickListener){
        this.onItemClickListener = onItemClickListener
    }

    // Provide a direct reference to each of the views with data items

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var counterGroupName: TextView

        init {
            counterGroupName = itemView.findViewById(R.id.counterGroupName)
        }

    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterGroupAdapter.ViewHolder {

        // Inflate the custom layout
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_counter_group, parent, false)
        return ViewHolder(view)
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: CounterGroupAdapter.ViewHolder, position: Int) {

        // Get the data model based on position
        var data = dataList[position]

        // Set item views based on your views and data model
        holder.counterGroupName.text = data.name

        holder.itemView.setOnClickListener{
            onItemClickListener!!.itemClick(position)
        }
    }

    //  total count of items in the list
    override fun getItemCount() = dataList.size
}