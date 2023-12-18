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
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var messyText: TextView
    private lateinit var compliance: TextView

    private lateinit var signOut: TextView

    private lateinit var fabButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var result: TextView
    private val imageSize = 224

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage

    private lateinit var firebaseDB: FirebaseFirestore

    private val calendarIcon = Calendar.getInstance()
    //private val classification = intent.getStringExtra("classification")
    private var messyCount: Int=0

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

        val receivedDate = intent.getStringExtra("date")
        if (receivedDate != null) {
            date.text = receivedDate
            Log.d("Date", date.text.toString())
            fetchImageUrlsFromFirestore()
        }


        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        firebaseDB = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recycler)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

       // fetchImageUrlsFromFirestore()

        fabButton = findViewById(R.id.fabAdd)

        signOut = findViewById(R.id.signOutButton)

        compliance = findViewById(R.id.compliance)

        calendar = findViewById(R.id.calendarButton)

        messyText = findViewById(R.id.dashtitle)

        // Init Swipe Listener
        val swipeListener = GestureDetectorCompat(this, SwipeListener())

        recyclerView.setOnTouchListener { _, event ->
            swipeListener.onTouchEvent(event)
        }

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
                  //  Toast.makeText(this, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()
                    date.text = selectedDate
                    //Log.d("Main", messyCount.toString())
                    Log.d("String", selectedDate)
                    fetchImageUrlsFromFirestore()
                    //updateCalendarCircle(messyCount)


                },
                calendarIcon.get(Calendar.YEAR),
                calendarIcon.get(Calendar.MONTH),
                calendarIcon.get(Calendar.DAY_OF_MONTH)
            )



            datePickerDialog.datePicker.maxDate = currentDate.timeInMillis

            datePickerDialog.show()

        }



        compliance.setOnClickListener {
            val intent = Intent(this, ComplianceActivity::class.java)
            intent.putExtra("Count", messyCount.toString())
            intent.putExtra("Date", date.text.toString())

            Log.d("String", messyCount.toString())
            Log.d("String", date.text.toString())
            startActivity(intent)
            finish()
        }
//
//            //TODO("Implement Historical Compliance View")
//        }

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
        var mess = 0
        var messyItems = mutableListOf<Item>()
        val selectedDate = date.text
      //  Log.d("Date", selectedDate.toString())
        var cleanItems = mutableListOf<Item>()
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
                        val classification = document.getString("classification")?.let { capitalize(it) }
                        if(classification == "Messy") {
                            mess +=1
                        }
                        val tag = document.getString("tag")?.let { capitalize(it) }

                        imageUrl?.let {
                            noImageText.visibility = View.GONE
                            tag?.let { it1 ->
                                classification?.let { it2 ->
                                    val checkedItemsMap = document.getData()?.get("checkedItems") as? Map<String, Boolean>
                                    checkedItemsMap?.let { map ->
                                        for ((item, isChecked) in map) {
                                            Log.d("FeedbackActivity", "Checked Item: $item, isChecked: $isChecked")
                                        }
                                    }
                                    val imgId = document.id
                                    val item = Item(it1, it2, it, checkedItemsMap?: emptyMap(), imgId)
                                    if (it2 == "Messy") {
                                        messyItems.add(item)
                                    } else {
                                        cleanItems.add(item)
                                    }
                                }
                            }
                        }
                    }

                    if (messyItems.isEmpty() && cleanItems.isEmpty()) {
                        noImageText.text = "No Spaces Submitted"
                        noImageText.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE

                    } else {
                        val items = mutableListOf<Item>()
                        items.addAll(messyItems)
                        items.addAll(cleanItems)

                        val adapter = ImageAdapter(items) { selectedItem ->
                            val intent = Intent(this, FeedbackActivity::class.java)
                            intent.putExtra("imgUrl", selectedItem.imageUrl)
                            intent.putExtra("classification", selectedItem.classification)
                            intent.putExtra("tag", selectedItem.tag)
                            intent.putExtra("date", selectedDate.toString())
                            selectedItem.checkedItems?.let { map ->
                                for ((item, isChecked) in map) {
                                    Log.d("FeedbackActivity", "Checked Item: $item, isChecked: $isChecked")
                                }
                            }
                            val checklistBundle = Bundle().apply {
                                for ((key, value) in selectedItem.checkedItems) {
                                    putBoolean(key, value)
                                }
                            }
                            intent.putExtra("checklist", checklistBundle)
                            intent.putExtra("imgId", selectedItem.imgId)

                            startActivity(intent)
                        }

                        recyclerView.visibility = View.VISIBLE
                        recyclerView.adapter = adapter

                    }
                //    Log.d("String", mess.toString())
                    messyText.text = "Non-Compliant Spaces: $mess"
                    messyCount= mess
                    //"Uncomment below function if you like the sliding UI"
                   // startScrollingAnimation()

                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                }
        }
    }
    private fun startScrollingAnimation() {
        val messyText = findViewById<TextView>(R.id.dashtitle)

        // Calculate width of the screen
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()

        // Calculate width of the text
        val textWidth = messyText.paint.measureText(messyText.text.toString())

        // Create a translation animation that moves text from right to left
        val scrollAnimation = TranslateAnimation(
            screenWidth, -textWidth,  // start and end X coordinates
            0f, 0f                   // start and end Y coordinates (no vertical movement)
        )

        // Set animation properties
        scrollAnimation.duration = 8000  // Adjust the duration as needed
        scrollAnimation.repeatCount = Animation.INFINITE
        scrollAnimation.interpolator = LinearInterpolator()  // Linear movement

        // Set the animation to the TextView
        messyText.startAnimation(scrollAnimation)
    }


    fun capitalize(str: String): String {
        return str.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    // Swipe Gesture Helpers

    inner class SwipeListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {

            val diffX = e2.x - (e1?.x ?: 0f)
            val diffY = e2.y - (e1?.y ?: 0f)

            if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY) &&
                kotlin.math.abs(diffX) > SWIPE_THRESHOLD &&
                kotlin.math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                if (diffX > 0) {
                    // Swiped right
                    Log.d("SwipeListener", "Swiped right")
                    updateDate(-1) // Move to the previous date
                } else {
                    // Swiped left
                    Log.d("SwipeListener", "Swiped left")
                    updateDate(1) // Move to the next date
                }
                return true
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    private fun updateDate(days: Int) {
        val currentDate = calendarIcon.time
        val newDate = Calendar.getInstance()
        newDate.time = currentDate
        newDate.add(Calendar.DAY_OF_MONTH, days)

        val selectedDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(newDate.time)
        date.text = selectedDate
        fetchImageUrlsFromFirestore()
    }


}
