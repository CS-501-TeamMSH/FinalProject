package com.example.finalproject

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.ml.ModelUnquant
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

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

    private lateinit var fabButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var result: TextView
    private val imageSize = 224

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage

    private lateinit var firebaseDB: FirebaseFirestore

    private val calendarIcon = Calendar.getInstance()

    private lateinit var date: TextView
    private lateinit var currentUserID: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        date = findViewById<TextView>(R.id.date)

        val today = Date()
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val formattedDate: String = dateFormat.format(today)
        date.text = formattedDate


        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        firebaseDB = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        fetchImageUrlsFromFirestore()

        fabButton = findViewById(R.id.fabAdd)

        signOut = findViewById<TextView>(R.id.signOutButton)

        calendar = findViewById<ImageButton>(R.id.calendarButton)

        // Fetch image URLs from Firebase Firestore
        fetchImageUrlsFromFirestore()


        fabButton.setOnClickListener {
            showPictureDialog()
        }

        calendar.setOnClickListener {
            // Get the current date
            val currentDate = Calendar.getInstance()

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendarIcon.set(Calendar.YEAR, year)
                    calendarIcon.set(Calendar.MONTH, month)
                    calendarIcon.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val selectedDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                        .format(calendarIcon.time)
                    Toast.makeText(this, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()

                    date.text = selectedDate
                    fetchImageUrlsFromFirestore()
                },
                calendarIcon.get(Calendar.YEAR),
                calendarIcon.get(Calendar.MONTH),
                calendarIcon.get(Calendar.DAY_OF_MONTH)
            )

            // Set the maximum date to the current date to blur out dates after the current date
            datePickerDialog.datePicker.maxDate = currentDate.timeInMillis

            // Show the DatePickerDialog
            datePickerDialog.show()
        }



        signOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        val pictureDialogItems = arrayOf("Photo Gallery", "Take Picture")
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }

        val intent = Intent(this, ImageDetailActivity::class.java)
        startActivity(intent)
        finish()
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
            val intent = Intent(this, ImageDetailActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "New Picture")
            values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

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
        val noImageText = findViewById<TextView>(R.id.noImageText)
        val currentUserID = FirebaseAuth.getInstance().currentUser?.uid
        currentUserID?.let { uid ->
            firestoreDB.collection("images")
                .whereEqualTo("userId", uid)
                .whereEqualTo("timestamp", date.text.toString())
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val imageUrl = document.getString("imageUrl")
                        val classification = document.getString("classification")
                            ?.let { capitalize(it) }
                        val tag = document.getString("tag")?.let { capitalize(it) }


                        imageUrl?.let {
                            noImageText.visibility = View.GONE
                            tag?.let { it1 ->
                                classification?.let { it2 ->
                                    Item(it1, it2, it)
                                }?.let { it3 ->
                                    items.add(it3)
                                }
                            }
                        }
                    }

                    if (items.isEmpty()) {
                        noImageText.text = "No Spaces Submitted"
                        noImageText.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {

                        val adapter = ImageAdapter(items) { selectedItem ->
                            // Handle the click here, for example, navigate to a new activity
                            val intent = Intent(this, FeedbackActivity::class.java)
                            intent.putExtra("imgUrl", selectedItem.imageUrl)
                            intent.putExtra("classification", selectedItem.classification)
                            intent.putExtra("tag", selectedItem.tag)
                            startActivity(intent)
                        }

                        // Show RecyclerView and set the adapter when images are present
                        recyclerView.visibility = View.VISIBLE
                        recyclerView.adapter = adapter
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }

    fun capitalize(str: String): String {
        return str.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
