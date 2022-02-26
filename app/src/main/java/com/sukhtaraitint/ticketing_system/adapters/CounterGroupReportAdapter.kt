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
import com.sukhtaraitint.ticketing_system.models.CounterGroupsReport

class CounterGroupReportAdapter(var context: Context) : RecyclerView.Adapter<CounterGroupReportAdapter.ViewHolder>() {

    var dataList = emptyList<CounterGroupsReport>()
    var onItemClickListener: OnItemClickListener? = null

    internal fun setDataList(dataList: List<CounterGroupsReport>) {
        this.dataList = dataList
        notifyDataSetChanged()
    }

    internal fun setOnItemClickListener(onItemClickListener: OnItemClickListener){
        this.onItemClickListener = onItemClickListener
    }

    // Provide a direct reference to each of the views with data items

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCounterGroupName: TextView
        var tvTotalTicket: TextView
        var tvTotalAmmount: TextView

        init {
            tvCounterGroupName = itemView.findViewById(R.id.tvCounterGroupName)
            tvTotalTicket = itemView.findViewById(R.id.tvTotalTicket)
            tvTotalAmmount = itemView.findViewById(R.id.tvTotalAmmount)
        }

    }

    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CounterGroupReportAdapter.ViewHolder {

        // Inflate the custom layout
        var view = LayoutInflater.from(parent.context).inflate(R.layout.item_counter_group_wise_report, parent, false)
        return ViewHolder(view)
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(holder: CounterGroupReportAdapter.ViewHolder, position: Int) {

        // Get the data model based on position
        var data = dataList[position]

        // Set item views based on your views and data model
        holder.tvCounterGroupName.text = data.name
        holder.tvTotalTicket.text = "Ticket : "+ data.total_ticket_sold_count
        holder.tvTotalAmmount.text = "Total Amount: " + data.total_ticket_sold_price

        holder.itemView.setOnClickListener{
            onItemClickListener!!.itemClick(position)
        }
    }

    //  total count of items in the list
    override fun getItemCount() = dataList.size
}