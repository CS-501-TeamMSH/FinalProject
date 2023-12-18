import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.ChecklistItem
import com.example.finalproject.R
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ChecklistAdapter(private val items: List<ChecklistItem>,private val checklistReference: DocumentReference) :
    RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {

    private lateinit var firebaseDB: FirebaseFirestore
    private lateinit var firebaseStorage: FirebaseStorage
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.checklist_fragment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.textView.text = currentItem.text
        holder.checkbox.isChecked = currentItem.isChecked

        // Set a listener to handle checkbox state changes
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            items[position].isChecked = isChecked
            updateFirebaseChecklistItem(currentItem.text, isChecked)
            val txt = currentItem.text
            Log.d("ChecklistAdapter", "Item '$txt' state changed to $isChecked")
        }

    }

    private fun updateFirebaseChecklistItem(text: String, isChecked: Boolean) {
//        val checklistData = mapOf(text to isChecked)
        val checklistData = mapOf("checkedItems.$text" to isChecked)
        checklistReference.update(checklistData)
            .addOnSuccessListener {
                Log.d("ChecklistAdapter", "Item '$text' updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e("ChecklistAdapter", "Error updating item '$text': ${e.message}")
            }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkbox: CheckBox = itemView.findViewById(R.id.todoCheckbox)
        val textView: TextView = itemView.findViewById(R.id.todoText)

//        fun bind(item: String) {
//            textView.text = item
//            checkbox.isChecked = false
//        }
    }
}

