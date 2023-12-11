package com.example.finalproject

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class ChecklistActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)

        val imageView = findViewById<ImageView>(R.id.imageChecklist)

        var item = "https://www.newportacademy.com/wp-content/uploads/NA_Messy-Room_Male_Hero_JPEG.jpg"
        Picasso.get().load(item).into(imageView)


    }

}