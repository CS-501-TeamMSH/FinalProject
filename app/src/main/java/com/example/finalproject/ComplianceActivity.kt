package com.example.finalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.w3c.dom.Text

class ComplianceActivity : AppCompatActivity() {

    private lateinit var button: Button

    private lateinit var dateText: TextView
    private lateinit var countText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compliance)

        val count = intent.getStringExtra("Count")
        val date = intent.getStringExtra("Date")

        dateText = findViewById(R.id.dateTextView)
        countText = findViewById(R.id.countTextView)


        dateText.text = date.toString()

        countText.text = count.toString()


//// Create a list with a single ComplianceItem using the received count and date
//        val itemList = mutableListOf<ComplianceItem>()
//        if (count != null && date != null) {
//            val complianceItem = ComplianceItem(date, count)
//            itemList.add(complianceItem)
//        }
//        val recyclerView: RecyclerView = findViewById(R.id.recycler)
//        val layoutManager = LinearLayoutManager(this)
//        val adapter = ComplianceAdapter(itemList)
//
//        recyclerView.layoutManager = layoutManager
//        recyclerView.adapter = adapter


        //Log.d("S", count.toString())
       // Log.d("S", date.toString())




        button = findViewById(R.id.backButton)

        button.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        Toast.makeText(this, "here", Toast.LENGTH_SHORT).show()
    }
}

data class ComplianceItem(val date: String, val count: String)


