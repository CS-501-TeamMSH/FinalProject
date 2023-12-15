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
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalproject.ml.ModelUnquant
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.sign

class DashActivity : AppCompatActivity() {
    private val firestoreDB = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var buttonAdd: ImageButton
    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST_CODE = 101
    private val GALLERY_REQUEST_CODE = 102
    private var imageUri: Uri? = null

    private lateinit var calendar: ImageButton

    private lateinit var signOut: TextView

    private lateinit var camera: Button
    private lateinit var gallery: Button
    private lateinit var fabButton: FloatingActionButton
    private lateinit var imageView: ImageView
    private lateinit var result: TextView
    private lateinit var save: Button
    private lateinit var retrieveButton: Button
    private val imageSize = 224

    private lateinit var title: TextView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage

    private lateinit var firebaseDB: FirebaseFirestore


    private val savedImageAndTextSet = HashSet<String>()
    private val savedImageHashes = HashSet<String>()

    private val calendarIcon = Calendar.getInstance()

    private val storedImages = mutableListOf<String>()
    private lateinit var date: TextView
    private lateinit var currentUserID: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash)

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

        buttonAdd = findViewById<ImageButton>(R.id.addImage)
        fabButton = findViewById(R.id.fabAdd)

        signOut = findViewById<TextView>(R.id.signOutButton)

        calendar = findViewById<ImageButton>(R.id.calendarButton)

        // Fetch image URLs from Firebase Firestore
        fetchImageUrlsFromFirestore()

        buttonAdd.setOnClickListener {
            showPictureDialog()
        }

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
        // pictureDialog.show()
        val intent = Intent(this, MainActivity::class.java)
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
            val intent = Intent(this, MainActivity::class.java)
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
//                        val intent = Intent(this, MainActivity::class.java)
//                        startActivity(intent)
//                        finish()
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
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            val resizedBitmap = resizeBitmap(bitmap, imageSize, imageSize)
            imageView.setImageBitmap(resizedBitmap)
            processImage(resizedBitmap)
            storeImageAndTextToFirestore(resizedBitmap, result.text.toString())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun getImageUri(inImage: Bitmap): Uri {
        val bytes = ByteBuffer.allocate(inImage.byteCount)
        inImage.copyPixelsToBuffer(bytes)
        val path = MediaStore.Images.Media.insertImage(
            contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }

    private fun processImage(bitmap: Bitmap) {
        val model = ModelUnquant.newInstance(applicationContext)

        // Convert Bitmap to ByteBuffer
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        inputFeature0.loadBuffer(byteBuffer)

        // Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        // Interpret the output tensor
        val classLabels = listOf("clean", "messy") // Replace with your actual class labels

        val maxIndex =
            outputFeature0.floatArray.indices.maxByOrNull { outputFeature0.floatArray[it] } ?: -1
        val predictedClassLabel = if (maxIndex != -1) {
            classLabels[maxIndex]
        } else {
            "Unknown"
        }
        val premiumLabels = listOf("high premium", "medium premium", "low premium")

        if (predictedClassLabel == classLabels[1]) { // Check if predicted class is "messy"
            val messyScore = outputFeature0.floatArray[1] * 100 // Get messy probability
            //   val highPremiumProbability = 0.7 * messyScore // Calculate high premium probability
            //     val mediumPremiumProbability = 0.4 * messyScore // Calculate medium premium probability
            //   val lowPremiumProbability = 0.1 * messyScore // Calculate low premium probability

            // Determine the predicted premium class based on probabilities
            var predictedPremiumLabel = "Unknown"
            if (messyScore > 0.7) {
                predictedPremiumLabel = premiumLabels[0] // Set high premium
            } else if (messyScore > 0.4) {
                predictedPremiumLabel = premiumLabels[1] // Set medium premium
            } else {
                predictedPremiumLabel = premiumLabels[2] // Set low premium
            }

            // Format and display the result
            val resultText =
                "$predictedClassLabel\n $messyScore%"    // Predicted Premium: $predictedPremiumLabel"
            result.text = Html.fromHtml(resultText, Html.FROM_HTML_MODE_COMPACT)
            result.gravity = Gravity.CENTER
            // storeClassificationText(resultText)
        } else {

            val cleanScore = String.format("%.1f%%", outputFeature0.floatArray[0] * 100)
            val resultText = "$predictedClassLabel"
            result.text = resultText
            result.gravity = Gravity.CENTER
            // storeClassificationText(resultText)
            //  Log.d("Reached here", "hi")
        }

        model.close()

    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer =
            ByteBuffer.allocateDirect(4 * 224 * 224 * 3) // Assuming FLOAT32, adjust if needed
        byteBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(224 * 224)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in pixels) {
            byteBuffer.putFloat((pixelValue shr 16 and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixelValue shr 8 and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }

        return byteBuffer
    }


    private fun storeImageAndTextToFirestore(bitmap: Bitmap, classificationText: String) {
        val storageRef = firebaseStorage.reference
        val pairUUID = UUID.randomUUID().toString() // Generate a single UUID for image-text pair
        val imagesRef =
            storageRef.child("images/$currentUserID/$pairUUID.jpg") // Store image in Firebase Storage
        val textRef =
            storageRef.child("classification/$currentUserID/$pairUUID.txt") // Store text in Firebase Storage

        // Convert the bitmap to bytes
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        // Store the image in Firebase Storage
        val uploadImageTask = imagesRef.putBytes(imageData)
        uploadImageTask.addOnSuccessListener { imageUploadTask ->
            imagesRef.downloadUrl.addOnSuccessListener { imageUrl ->
                Log.d("MainActivity", "Image uploaded to Firebase Storage. Image URL: $imageUrl")

                // Store the classification text in Firebase Storage
                val textBytes = classificationText.toByteArray()
                val uploadTextTask = textRef.putBytes(textBytes)
                uploadTextTask.addOnSuccessListener { textUploadTask ->
                    textRef.downloadUrl.addOnSuccessListener { textUrl ->
                        Log.d(
                            "MainActivity",
                            "Classification text uploaded to Firebase Storage. Text URL: $textUrl"
                        )

                        // Create a Firestore document reference
                        val docRef = firebaseDB.collection("images")
                            .document() // Use auto-generated ID for the document

                        // Create a data object to be stored in Firestore
                        val data = hashMapOf(
                            "imageUrl" to imageUrl.toString(), // Replace imageUrl with the actual URL obtained
                            "textUrl" to textUrl.toString(), // Replace textUrl with the actual URL obtained
                            "classification" to classificationText,
                            "userId" to currentUserID,
                            "timestamp" to System.currentTimeMillis() // Add current time as a timestamp
                        )

                        // Save the data into Firestore
                        docRef.set(data)
                            .addOnSuccessListener {
                                // Toast.makeText(this, "Uploaded Successfully", Toast.LENGTH_SHORT)
                                //     .show()
                                Log.d("MainActivity", "Image data saved successfully to Firestore")
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                                Log.e("MainActivity", "Error saving image data: ${e.message}")
                            }
                        //binding.progressBar.visibility = View.GONE
                        //binding.imageView.setImageResource(R.drawable.vector)
                    }.addOnFailureListener { textUrlFailure ->
                        Toast.makeText(
                            this,
                            "Error getting text URL: ${textUrlFailure.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("MainActivity", "Failed to get text URL: ${textUrlFailure.message}")
                    }
                }.addOnFailureListener { textUploadFailure ->
                    Toast.makeText(
                        this,
                        "Error uploading classification text: ${textUploadFailure.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "MainActivity",
                        "Error uploading classification text: ${textUploadFailure.message}"
                    )
                }
            }.addOnFailureListener { imageUrlFailure ->
                Toast.makeText(
                    this,
                    "Error getting image URL: ${imageUrlFailure.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("MainActivity", "Failed to get image URL: ${imageUrlFailure.message}")
            }
        }.addOnFailureListener { imageUploadFailure ->
            Toast.makeText(
                this,
                "Error uploading image: ${imageUploadFailure.message}",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("MainActivity", "Error uploading image: ${imageUploadFailure.message}")
        }
    }


    private fun fetchImageUrlsFromFirestore() {
        val items = mutableListOf<Item>()
        val noImageText = findViewById<TextView>(R.id.noImageText)
        // Date needs to be global -- fixed
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

                    val adapter: ImageAdapter
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
                            startActivity(intent)
                        }

                        // adapter = ImageAdapter(items)

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
