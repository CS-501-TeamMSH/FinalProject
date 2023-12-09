package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash)

        val date: TextView = findViewById<TextView>(R.id.date)
        val today = Date()
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val formattedDate: String = dateFormat.format(today)
        date.text = formattedDate

        val addButton: ImageButton = findViewById(R.id.addImage)
        addButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val recyclerView: RecyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        var items: List<Item> = emptyList()

        // Replace With Firebase Results
        items = listOf(
            Item("Item 1", "https://www.newportacademy.com/wp-content/uploads/NA_Messy-Room_Male_Hero_JPEG.jpg"),
            Item("Item 2", "https://www.newportacademy.com/wp-content/uploads/NA_Messy-Room_Male_Hero_JPEG.jpg"),
        )




        if (items.isEmpty()) {
            val noImageText = findViewById<TextView>(R.id.noImageText)
            noImageText.text = "No Spaces Submitted Today"
            val noImageButton: Button = findViewById(R.id.noImageAddButton)
            noImageButton.visibility = View.VISIBLE
            noImageButton.setOnClickListener {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        } else {
            val adapter = ImageAdapter(items)
            recyclerView.adapter = adapter
        }

    }
}