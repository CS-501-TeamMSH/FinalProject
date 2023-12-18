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

class ComplianceActivity : AppCompatActivity() {

    private val firestoreDB = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: Button
    private lateinit var textView: TextView
    private lateinit var countView: TextView

    private lateinit var messyTextView: TextView
    private lateinit var cleanTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compliance)

        backButton = findViewById(R.id.backButton)
        textView = findViewById(R.id.historyTitle)


        recyclerView = findViewById(R.id.historyRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        backButton = findViewById(R.id.backButton)
        textView = findViewById(R.id.historyTitle)

        //messyTextView = findViewById(R.id.messyTotal)
        // countView = findViewById(R.id.historyCount) // Add this line to reference the count TextView

        recyclerView = findViewById(R.id.historyRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val historyList = mutableListOf<HistoryItem>()
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid

        currentUserID?.let { uid ->
            firestoreDB.collection("images")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener { result ->
                    val historyMap = mutableMapOf<String, Int>()
                    val historyMapClean = mutableMapOf<String, Int>()

                    for (document in result) {
                        val timestamp = document.getString("timestamp")
                        val classification = document.getString("classification")

                        if (timestamp != null && classification == "messy") {
                            historyMap[timestamp] = (historyMap[timestamp] ?: 0) + 1
                        }

                        if (timestamp != null && classification == "clean") {
                            historyMapClean[timestamp] = (historyMapClean[timestamp] ?: 0) + 1
                        }

                    }

                    var totalMessyCount = 0
                    var totalCleanCount = 0

                    for ((_, messyCount) in historyMap) {
                        totalMessyCount += messyCount
                    }

                    //messyTextView.text = "Messy Total: $totalMessyCount"


                    for ((date, messyCount) in historyMap) {
                        val isMessy = messyCount > 0
                        Log.d("isMessy", isMessy.toString())
                        historyList.add(HistoryItem(date, messyCount, isMessy, false))
                        Log.d("History", "Date: $date, Messy Count: $messyCount pending items")
                    }

                    for ((date, cleanCount) in historyMapClean) {
                        if (!historyMap.containsKey(date)) {
                            val isClean = cleanCount > 0
                            Log.d("isClean", isClean.toString())
                            historyList.add(HistoryItem(date, cleanCount, false, isClean))
                            Log.d("History", "Date: $date, Clean Count: $cleanCount items")
                        }
                    }

                    historyList.sortWith(compareByDescending { it.date })

                    val adapter = ComplianceAdapter(historyList)
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = adapter
                }

                .addOnFailureListener { exception ->
                    exception.printStackTrace()

                }
        }

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
