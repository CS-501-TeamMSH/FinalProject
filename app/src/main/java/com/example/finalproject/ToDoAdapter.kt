import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.R

class ToDoAdapter(private val items: List<String>) :
    RecyclerView.Adapter<ToDoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }





    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkbox: CheckBox = itemView.findViewById(R.id.todoCheckbox)
        private val textView: TextView = itemView.findViewById(R.id.todoText)

        fun bind(item: String) {
            textView.text = item
            checkbox.isChecked = false
        }
    }
}
