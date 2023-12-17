package com.example.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComplianceAdapter(private val historyList: List<HistoryItem>) :
    RecyclerView.Adapter<ComplianceAdapter.ComplianceViewHolder>() {
    inner class ComplianceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.historyDate)
        val icon: TextView = itemView.findViewById(R.id.historyStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplianceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_fragment, parent, false)
        return ComplianceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComplianceViewHolder, position: Int) {
        val item = historyList[position]
        holder.date.text = item.date
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

}