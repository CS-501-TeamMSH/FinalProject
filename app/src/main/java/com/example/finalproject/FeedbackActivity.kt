package com.example.finalproject

import ChecklistAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONObject

class FeedbackActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: Button
    private var checkedItems: MutableSet<String> = mutableSetOf()
    private lateinit var checklistReference: DocumentReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        val imgUrl = intent.getStringExtra("imgUrl")
        val classification = intent.getStringExtra("classification")
        val tag = intent.getStringExtra("tag")
        val date = intent.getStringExtra("date")
        val imgId = intent.getStringExtra("imgId")
        Log.d("Date", date.toString())


        var img = findViewById<ImageView>(R.id.feedbackimage)
        var text = findViewById<TextView>(R.id.feedbacktext)
        val feedbackIcon = findViewById<ImageView>(R.id.feedbackClassificationIcon)
        val labelText = findViewById<TextView>(R.id.labelText)

        Picasso.get().load(imgUrl).into(img)
        text.text = classification
        labelText.text = tag

        val checklistBundle = intent.getBundleExtra("checklist")
        val checklist: Map<String, Boolean>? = checklistBundle?.let {
            val result = mutableMapOf<String, Boolean>()
            for (key in it.keySet()) {
                result[key] = it.getBoolean(key)
            }
            result
        }

        val checklistItems: List<ChecklistItem> = checklist?.map { entry ->
            ChecklistItem(entry.key, entry.value)
        } ?: emptyList()

        if (classification.equals("Messy")) {
            feedbackIcon.setImageResource(R.drawable.round_add_circle_24);
        } else {
            feedbackIcon.setImageResource(R.drawable.baseline_check_circle_24);
        }

        recyclerView = findViewById<RecyclerView>(R.id.todoRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

//        checklistReference = firestore.collection("images").document(tag.toString())


        recyclerView = findViewById<RecyclerView>(R.id.todoRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        backButton = findViewById<Button>(R.id.backButton)

        val firestore = FirebaseFirestore.getInstance()
//        checklistReference = firestore.collection("images").document()
        checklistReference = firestore.collection("images").document(imgId ?: "")
//        initializeChecklistFromStorage(checklistReference)



        if (classification.equals("Messy")) {

            Log.d("FeedbackAct", "Items in onCreateViewHolder: $checklistItems")
            recyclerView.adapter = ChecklistAdapter(checklistItems, checklistReference)

            // Default Checklist depending on tag
//            when (tag) {
//                "Office" -> {
//                    updateChecklist(getOfficeChecklist(), checklistReference)
//                }
//
//                "Kitchen" -> {
//                    updateChecklist(getKitchenChecklist(), checklistReference)
//                }
//
//                else -> {
//                    updateChecklist(getOtherChecklist(), checklistReference)
//                }
//            }

        } else {

//            when (tag) {
//                "Office" -> {
//                    updateChecklist(getOfficeChecklist())
//                }
//
//                "Kitchen" -> {
//                    updateChecklist(getKitchenChecklist())
//                }
//
//                else -> {
//                    updateChecklist(getOtherChecklist())
//                }
//            }
            val cleanMessage = getCleanMessage()
            val cleanMessageTextView = findViewById<TextView>(R.id.cleanTextResult)
            cleanMessageTextView.visibility = View.VISIBLE
            cleanMessageTextView.text = cleanMessage
        }


        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("date", date)
            startActivity(intent)
            finish()
        }
    }

//    private fun updateChecklist(checklist: List<String>) {
//        recyclerView.adapter = ChecklistAdapter(checklist)
//    }
    private fun updateChecklist(checklist: List<String>, documentReference: DocumentReference) {
        // Initialize the checklist items with the provided checked state
    //    val checklistItems = checklist.map { ChecklistItem(it, checkedItems?.get(it) ?: false) }
        val checklistItems = checklist.map { ChecklistItem(it, false) }
        recyclerView.adapter = ChecklistAdapter(checklistItems, documentReference)

    }
//    private fun initializeChecklistFromStorage(documentReference: DocumentReference) {
//        val imgId = documentReference.id
//        val storageReference = FirebaseStorage.getInstance().getReference("images/$imgId")
//
//        // Fetch the checklist state from Firebase Storage
//        storageReference.getBytes(Long.MAX_VALUE)
//            .addOnSuccessListener { checklistData ->
//                val checklistMap = String(checklistData).toMap() ?: emptyMap()
//
//                // Initialize the checklist items based on the fetched data
//                val checklistItems = getOfficeChecklist().map {
//                    ChecklistItem(it, checklistMap[it] == true)
//                }
//
//                // Set the initial state of the checkboxes
//                for (item in checklistItems) {
//                    if (item.isChecked) {
//                        checkedItems.add(item.text)
//                    }
//                }
//
//                recyclerView.adapter = ChecklistAdapter(checklistItems, documentReference)
//            }
//            .addOnFailureListener { e ->
//                Log.e("FeedbackActivity", "Error fetching checklist from Firebase Storage: ${e.message}")
//            }
//    }
//
//    fun String.toMap(): Map<String, Boolean> {
//        // Implement the conversion logic based on your specific data format
//        // This is just a simplified example assuming a JSON-like format
//        val json = JSONObject(this)
//        val map = mutableMapOf<String, Boolean>()
//        for (key in json.keys()) {
//            map[key] = json.getBoolean(key)
//        }
//        return map
//    }

    private fun getOfficeChecklist(): List<String> {
        return listOf(
            "Wipe surfaces regularly",
            "Organize storage spaces",
            "Dispose of trash regularly",
            "Sweep and mop the floors",
            "Check and refill office supplies"
        )
    }

    private fun getKitchenChecklist(): List<String> {
        return listOf(
            "Wash dishes promptly",
            "Wipe surfaces regularly",
            "Organize storage spaces",
            "Dispose of trash regularly",
            "Sweep and mop the floors"
        )
    }

    private fun getOtherChecklist(): List<String> {
        return listOf(
            "Wipe surfaces regularly",
            "Organize storage spaces",
            "Dispose of trash regularly",
            "Sweep and mop the floors",
            "Remove any trip hazards"
        )
    }

    private fun getCleanMessage(): String {
        return ("Maintain cleanliness & safety standards")

    }
}

