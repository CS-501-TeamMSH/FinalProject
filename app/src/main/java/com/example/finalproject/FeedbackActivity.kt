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

class FeedbackActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: Button
    private var checkedItems: MutableSet<String> = mutableSetOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        val imgUrl = intent.getStringExtra("imgUrl")
        val classification = intent.getStringExtra("classification")
        val tag = intent.getStringExtra("tag")
        val date = intent.getStringExtra("date")
        Log.d("Date", date.toString())

        var img = findViewById<ImageView>(R.id.feedbackimage)
        var text = findViewById<TextView>(R.id.feedbacktext)
        val feedbackIcon = findViewById<ImageView>(R.id.feedbackClassificationIcon)
        val labelText = findViewById<TextView>(R.id.labelText)

        Picasso.get().load(imgUrl).into(img)
        text.text = classification
        labelText.text = tag

        if (classification.equals("Messy")) {
            feedbackIcon.setImageResource(R.drawable.round_add_circle_24);
        } else {
            feedbackIcon.setImageResource(R.drawable.baseline_check_circle_24);
        }

        recyclerView = findViewById<RecyclerView>(R.id.todoRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        backButton = findViewById<Button>(R.id.backButton)


        if (classification.equals("Messy")) {

            // Default Checklist depending on tag
            when (tag) {
                "Office" -> {
                    updateChecklist(getOfficeChecklist())
                }

                "Kitchen" -> {
                    updateChecklist(getKitchenChecklist())
                }

                else -> {
                    updateChecklist(getOtherChecklist())
                }
            }

        } else {

            when (tag) {
                "Office" -> {
                    updateChecklist(getOfficeChecklist())
                }

                "Kitchen" -> {
                    updateChecklist(getKitchenChecklist())
                }

                else -> {
                    updateChecklist(getOtherChecklist())
                }
            }
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

    private fun updateChecklist(checklist: List<String>) {
        recyclerView.adapter = ChecklistAdapter(checklist)
    }

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

