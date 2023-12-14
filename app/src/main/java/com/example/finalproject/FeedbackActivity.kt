package com.example.finalproject

import ChecklistAdapter
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class FeedbackActivity : AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        val imgUrl = intent.getStringExtra("imgUrl")
        val classification = intent.getStringExtra("classification")
        var img = findViewById<ImageView>(R.id.feedbackimage)
        var text = findViewById<TextView>(R.id.feedbacktext)

        Picasso.get().load(imgUrl).into(img)
        text.text = classification

        val recyclerview = findViewById<RecyclerView>(R.id.todoRecyclerView)
        recyclerview.layoutManager = LinearLayoutManager(this)

        val kitchenCleanlinessChecklist = listOf(
            "Wash dishes promptly",
            "Wipe surfaces regularly",
            "Organize storage spaces",
            "Dispose of trash regularly",
            "Sweep and mop the floors"
        )

        recyclerview.adapter = ChecklistAdapter(kitchenCleanlinessChecklist)



    }
}