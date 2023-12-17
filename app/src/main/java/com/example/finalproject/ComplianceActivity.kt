package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.finalproject.HistoryItem
import org.w3c.dom.Text

class ComplianceActivity : AppCompatActivity() {

    private val firestoreDB = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: Button
    private lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compliance)

        backButton = findViewById(R.id.backButton)
        textView = findViewById(R.id.historyTitle)

        recyclerView = findViewById(R.id.historyRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchHistoryFromFirebase()

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchHistoryFromFirebase() {
        val historyList = mutableListOf<HistoryItem>()
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        currentUserID?.let { uid ->
            firestoreDB.collection("images")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { result ->
                    val historyMap = mutableMapOf<String, Int>()

                    for (document in result) {
                        val timestamp = document.getString("timestamp")
                        val classification = document.getString("classification")

                        if (timestamp != null && classification == "Messy") {
                            historyMap[timestamp] = (historyMap[timestamp] ?: 0) + 1
                        }
                    }

                    Log.d("History Map", historyMap.toString())

                    for ((date, messyCount) in historyMap) {
                        historyList.add(HistoryItem(date,messyCount))
                    }

                    val adapter = ComplianceAdapter(historyList)
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = adapter

//                    // Update the total messy count
//                    val totalMessyCount = historyList.sumBy { it.messyCount }
//                    messyText.text = "Non-Compliant Spaces: $totalMessyCount"
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

}