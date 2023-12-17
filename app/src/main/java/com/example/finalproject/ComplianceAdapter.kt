package com.example.finalproject
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComplianceAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<ComplianceAdapter.ComplianceViewHolder>() {

    inner class ComplianceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.historyDate)
        val messyCount: TextView = itemView.findViewById(R.id.historyCount)
        val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplianceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_fragment, parent, false)
        return ComplianceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComplianceViewHolder, position: Int) {
        val item = historyList[position]
        holder.date.text = item.date
        //holder.messyCount.text = item.messyCount.toString()

        // Set icon based on isMessy property
        if (item.isMessy) {
            holder.statusIcon.setImageResource(R.drawable.baseline_error_24)
        } else {
            holder.statusIcon.setImageResource(R.drawable.baseline_check_circle_24)
        }
    }

    override fun getItemCount(): Int {
        return historyList.size
    }
}
