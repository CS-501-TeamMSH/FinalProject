package com.example.finalproject

import ChecklistAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.squareup.picasso.Picasso

class FeedbackActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        val imgUrl = intent.getStringExtra("imgUrl")
        val classification = intent.getStringExtra("classification")
        val tag = intent.getStringExtra("tag")
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

        val checklistSegmentButton =
            findViewById<MaterialButtonToggleGroup>(R.id.checklistSegmentButton)

        // Default Checklist depending on tag
        when (tag) {
            "Office" -> {
                checklistSegmentButton.check(R.id.button1)
                updateChecklist(getOfficeChecklist())
            }

            "Kitchen" -> {
                checklistSegmentButton.check(R.id.button2)
                updateChecklist(getKitchenChecklist())
            }
            else -> {
                checklistSegmentButton.check(R.id.button3)
                updateChecklist(getOtherChecklist())
            }
        }

        checklistSegmentButton.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked && checkedId == group.checkedButtonId) {
                when (checkedId) {
                    R.id.button1 -> updateChecklist(getOfficeChecklist())
                    R.id.button2 -> updateChecklist(getKitchenChecklist())
                    R.id.button3 -> updateChecklist(getOtherChecklist())
                }
            }
        }


        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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
}