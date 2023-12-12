package com.example.finalproject

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val firestoreDB = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAdd: ImageButton
    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private var imageUri: Uri? = null

    private lateinit var calendar: ImageButton

    private lateinit var signOut: TextView


    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage

    private lateinit var firebaseDB: FirebaseFirestore

    private val calendarIcon = Calendar.getInstance()

    private lateinit var date: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        date = findViewById<TextView>(R.id.date)

        val today = Date()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val formattedDate: String = dateFormat.format(today)
        date.text = formattedDate
        fetchImageUrlsFromFirestore()

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        firebaseDB = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        buttonAdd = findViewById<ImageButton>(R.id.addImage)

        signOut = findViewById<TextView>(R.id.signOutButton)

        calendar = findViewById<ImageButton>(R.id.calendarButton)


        // Fetch image URLs from Firebase Firestore
        fetchImageUrlsFromFirestore()

        buttonAdd.setOnClickListener {
            showPictureDialog(buttonAdd)
        }
        calendar.setOnClickListener {

            val datePickerDialog = DatePickerDialog(
                this, { _, year, month, dayOfMonth ->
                    calendarIcon.set(Calendar.YEAR, year)
                    calendarIcon.set(Calendar.MONTH, month)
                    calendarIcon.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                    val selectedDate = SimpleDateFormat(
                        "MM/dd/yyyy",
                        Locale.getDefault()
                    ).format(calendarIcon.time)
                    Toast.makeText(this, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()
                    date.text = selectedDate
                    fetchImageUrlsFromFirestore()

                },
                calendarIcon.get(Calendar.YEAR),
                calendarIcon.get(Calendar.MONTH),
                calendarIcon.get(Calendar.DAY_OF_MONTH)

            )

            // Show the DatePickerDialog
            datePickerDialog.show()
        }


        signOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Handle Dashboard item click
                    // For example, do something for the Dashboard item
                    true
                }

//                R.id.navigation_profile -> {
//                    // Handle Upload Image item click
//                    // For example, navigate to MainActivity
//                    val intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                    true
//                }
                R.id.navigation_ToDo -> {
                    val intent = Intent(this, ToDoListActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }


    }

    private fun showPictureDialog(anchorView: View) {
        val popupMenu = PopupMenu(this, anchorView)
        popupMenu.menuInflater.inflate(R.menu.your_menu_resource, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_gallery -> {
                    choosePhotoFromGallery()
                    true
                }

                R.id.menu_take_picture -> {
                    takePhotoFromCamera()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }


    // Function to handle capturing an image from the camera
    private fun takePhotoFromCamera() {
        // Check camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

            // Start the camera intent
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        }
    }

    // Function to handle selecting an image from the gallery
    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
    }

    // Override onActivityResult to handle the result of image selection/capture
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    // Camera photo is captured
                    // You can use imageUri to do whatever you want with the image
                    imageUri?.let { uri ->
                        displayImage(uri)
                    }
                }

                GALLERY_REQUEST_CODE -> {
                    // Gallery photo is selected
                    imageUri = data?.data
                    // You can use imageUri to do whatever you want with the image
                    imageUri?.let { uri ->
                        displayImage(uri)
                    }
                }
            }
        }
    }

    private fun displayImage(imageUri: Uri?) {
        try {
            // Instead of resizing the image, directly pass the URI to ImageDetailActivity
            val intent = Intent(this, ImageDetailActivity::class.java).apply {
                putExtra("imageUri", imageUri.toString())
            }
            startActivity(intent)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun fetchImageUrlsFromFirestore() {
        val items = mutableListOf<Item>()
        //date needs to be global -- fixed
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        currentUserID?.let { uid ->
            firestoreDB.collection("images")
                .whereEqualTo("userId", uid)
                .whereEqualTo("timestamp", date.text.toString())
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val imageUrl = document.getString("imageUrl")
                        val text = document.getString("classification")
                        imageUrl?.let {
                            text?.let { it1 -> Item(it1, it) }?.let { it2 -> items.add(it2) }
                        }
                    }

                    val adapter: ImageAdapter
                    if (items.isEmpty()) {
                        val noImageText = findViewById<TextView>(R.id.noImageText)
                        noImageText.text = "No Spaces Submitted "
                        val noImageButton: Button = findViewById(R.id.noImageAddButton)
                        // noImageButton.visibility = View.VISIBLE
                        adapter = ImageAdapter(items) // Pass an empty list to the adapter
                    } else {
                        adapter = ImageAdapter(items)
                    }
                    recyclerView.adapter = adapter
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

}
