package com.example.finalproject

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView

class Profile : AppCompatActivity() {
    private lateinit var kitchenText: TextView
    private lateinit var officeText: TextView
    private lateinit var workText: TextView
    private var isKitchenPopupShowing = false
    private var kitchenPopupWindow: PopupWindow? = null

    private var isOfficePopupShowing = false
    private var officePopupWindow: PopupWindow? = null

    private var isWorkPopupShowing = false
    private var workPopupWindow: PopupWindow? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        kitchenText = findViewById(R.id.kitchentext)
        officeText = findViewById(R.id.officetext)
        workText = findViewById(R.id.worktext)

        kitchenText.setOnClickListener {
            //   Toast.makeText(this, "Kitchen TextView Clicked!", Toast.LENGTH_SHORT).show()
            toggleKitchenPopup(kitchenText)
        }

        officeText.setOnClickListener {
            toggleOfficePopup(officeText)
        }

        workText.setOnClickListener {
            toggleWorkPopup(workText)
        }


        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                    R.id.navigation_home -> {
                        val intent = Intent(this, DashActivity::class.java)
                        startActivity(intent)
                        true
                    }

                R.id.navigation_profile -> {
                    // Handle Upload Image item click
                    // For example, navigate to MainActivity
                    val intent = Intent(this, Profile::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }
    }


    private fun toggleKitchenPopup(view: View) {
        if (isKitchenPopupShowing) {
            kitchenPopupWindow?.dismiss()
            isKitchenPopupShowing = false
        } else {
            displayKitchenPopup(view)
        }
    }

   private fun toggleOfficePopup(view: View) {
        if (isOfficePopupShowing) {
            officePopupWindow?.dismiss()
            isOfficePopupShowing= false
        } else {
            displayOfficePopup(view)
        }
    }


   private fun toggleWorkPopup(view: View) {
        if (isWorkPopupShowing) {
            workPopupWindow?.dismiss()
            isWorkPopupShowing = false
        } else {
            displayWorkPopup(view)
        }
    }

    // Updated function to display the kitchen PopupWindow
    private fun displayKitchenPopup(anchorView: View) {
        val window = PopupWindow(this)
        val view = layoutInflater.inflate(R.layout.kitchen, null)
        window.contentView = view

        val listView = view.findViewById<ListView>(R.id.kitch)
        val items = arrayOf(
            "1. Clear Countertops",
            "2. No Visible Signs of Dirt or Mould",
            "3. Dishes Washed"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            window.dismiss()
            isKitchenPopupShowing = false
        }

        // Show the PopupWindow
        window.showAsDropDown(anchorView)
        isKitchenPopupShowing = true
        kitchenPopupWindow = window
    }


    private fun displayOfficePopup(anchorView: View) {
        val window = PopupWindow(this)
        val view = layoutInflater.inflate(R.layout.office, null)
        window.contentView = view

        val listView = view.findViewById<ListView>(R.id.office)
        val items = arrayOf(
            "1. Clear Countertops",
            "2. No Visible Signs of Dirt or Mould",
            "3. Dishes Washed"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            window.dismiss()
            isOfficePopupShowing = false
        }

        // Show the PopupWindow
        window.showAsDropDown(anchorView)
        isOfficePopupShowing = true
        officePopupWindow = window
    }


    private fun displayWorkPopup(anchorView: View) {
        val window = PopupWindow(this)
        val view = layoutInflater.inflate(R.layout.work, null)
        window.contentView = view

        val listView = view.findViewById<ListView>(R.id.work)
        val items = arrayOf(
            "1. Clear Countertops",
            "2. No Visible Signs of Dirt or Mould",
            "3. Dishes Washed"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            window.dismiss()
            isWorkPopupShowing = false
        }

        // Show the PopupWindow
        window.showAsDropDown(anchorView)
        isWorkPopupShowing = true
        workPopupWindow = window

    }
}



// Add your desired action here
