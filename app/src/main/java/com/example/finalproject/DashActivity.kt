package com.example.finalproject

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash)

        val recyclerView: RecyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val items = listOf(
            Item("Item 1", "https://www.newportacademy.com/wp-content/uploads/NA_Messy-Room_Male_Hero_JPEG.jpg"),
            Item("Item 2", "https://www.newportacademy.com/wp-content/uploads/NA_Messy-Room_Male_Hero_JPEG.jpg"),

        )

        val adapter = ImageAdapter(items)
        recyclerView.adapter = adapter
    }
}