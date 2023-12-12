package com.example.finalproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ToDoAdapter : RecyclerView.Adapter<ToDoAdapter.ViewHolder>() {

    private var todoItems: List<ToDoItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todoItem = todoItems[position]
        holder.bind(todoItem)
    }

    override fun getItemCount(): Int {
        return todoItems.size
    }

    fun updateData(newTodoItems: List<ToDoItem>) {
        todoItems = newTodoItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val todoTitle: TextView = itemView.findViewById(R.id.todoTextView)
        private val todoCheckbox: CheckBox = itemView.findViewById(R.id.todoCheckBox)

        fun bind(todoItem: ToDoItem) {
            todoTitle.text = todoItem.text
            todoCheckbox.isChecked = todoItem.checked
        }
    }
}
