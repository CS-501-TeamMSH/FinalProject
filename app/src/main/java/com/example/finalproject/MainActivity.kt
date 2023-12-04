package com.example.finalproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log

import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.ml.ModelUnquant
import com.google.firebase.auth.FirebaseAuth
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import android.text.Html
import android.view.Gravity
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var camera: Button
    private lateinit var gallery: Button
    private lateinit var imageView: ImageView
    private lateinit var result: TextView
    private lateinit var save: Button
    private lateinit var retrieveButton: Button
    private val imageSize = 224

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage


    private val savedImageAndTextSet = HashSet<String>()
    private val savedImageHashes = HashSet<String>()

    private val storedImages = mutableListOf<String>()
    private lateinit var currentUserID: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        camera = findViewById<Button>(R.id.button)
        gallery = findViewById<Button>(R.id.button2)

        result = findViewById(R.id.result)
        imageView = findViewById(R.id.imageView)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        val welcomeTextView = findViewById<TextView>(R.id.welcomeTextView)
        val username = intent.getStringExtra("USERNAME_EXTRA")
        val signOutButton = findViewById<Button>(R.id.buttonSignOut)

        val currentUser = firebaseAuth.currentUser
        currentUserID = currentUser?.uid ?: ""

        //Retrieve user's ID
        val saveButton = findViewById<Button>(R.id.saveImageButton)
        val retrieveButton = findViewById<Button>(R.id.retrieve)

        if (!username.isNullOrEmpty()) {
            welcomeTextView.text = "Welcome, $username"

            //Persistent storage
            val persistName = getSharedPreferences("user", MODE_PRIVATE)
            val editor = persistName.edit()
            editor.putString("Username", username)
            editor.apply()
        }

        //Retrieve stored name!
        val sharedPreferences = getSharedPreferences("user", MODE_PRIVATE)
        val savedUsername = sharedPreferences.getString("Username", "")



        if (!savedUsername.isNullOrEmpty()) {
            welcomeTextView.text = "Welcome, $savedUsername"
        }



        camera.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, 3)
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            }
        }


        gallery.setOnClickListener {
            val cameraIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(cameraIntent, 1)
        }


        signOutButton.setOnClickListener {
            // Sign out the current user and redirect to the login page
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity
        }

        saveButton.setOnClickListener {
            // Check if an image is displayed in the ImageView
            if (imageView.drawable != null) {
                // Get the drawable bitmap from ImageView
                val drawableBitmap = (imageView.drawable).toBitmap()

                // Store the bitmap image and the classification text
//                storeImageAndClassification(drawableBitmap, result.text.toString())

                preventFirebaseDuplicates(drawableBitmap, result.text.toString())
            } else {
                // Handle the case when no image is displayed
                Log.e("MainActivity", "No image to save.")
            }
        }

        retrieveButton.setOnClickListener {
            retrieveImageAndClassification()
        }


        //TODO: Prevent duplicate image + text from being saved

    }

    private fun retrieveImageAndClassification() {
        val storageRef = firebaseStorage.reference
        val imagesRef = storageRef.child("images/$currentUserID")  // retrieve based on user Id --> helps create independent views!
        val textRef = storageRef.child("classification/$currentUserID")

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val imageListResult = imagesRef.listAll().await()
                val textListResult = textRef.listAll().await()

                for (i in imageListResult.items.indices) {
                    val imageUrl = imageListResult.items[i].downloadUrl.await()
                    Log.d("MainActivity", "Retrieved image URL: $imageUrl")

                    // Load image
                    Picasso.get().load(imageUrl)
                        .error(android.R.drawable.ic_dialog_alert)
                        .into(imageView)

                    delay(2000) // 2-second delay

                    val textUrl = textListResult.items.getOrElse(i) {
                        textRef.child("default")
                            .also { Log.e("MainActivity", "No corresponding text found for image") }
                    }.downloadUrl.await()
                    Log.d("MainActivity", "Retrieved text URL: $textUrl")

                    // Load text
                    val bytes =
                        textRef.child(textListResult.items[i].name).getBytes(Long.MAX_VALUE).await()
                    val classificationText = String(bytes, Charsets.UTF_8)
                    result.text = classificationText

                    delay(2000)
                }
                Toast.makeText(this@MainActivity, "Finished retrieving images", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to retrieve image or text URLs: ${e.message}")
            }
        }

        Toast.makeText(this, "Retrieving images.", Toast.LENGTH_SHORT).show()
    }

    private fun loadExistingImageUUIDs() {
        val storageRef = firebaseStorage.reference
        val imagesRef = storageRef.child("images")

        imagesRef.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    // Get the full name of the image (with extension) and extract the UUID part
                    val fullName = item.name
                    val parts = fullName.split(".")
                    if (parts.isNotEmpty()) {
                        val uuid = parts[0] // Extract UUID part
                        savedImageAndTextSet.add(uuid)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to retrieve image UUIDs: ${e.message}")
            }
    }

    private fun loadExistingImageHashes() {
        val storageRef = firebaseStorage.reference
        val imagesRef = storageRef.child("images")

        imagesRef.listAll()
            .addOnSuccessListener { listResult ->
                for (item in listResult.items) {
                    // Fetch the image bytes
                    item.getBytes(Long.MAX_VALUE)
                        .addOnSuccessListener { bytes ->
                            // Calculate the hash of the image data
                            val imageHash = calculateHash(bytes)
                            savedImageHashes.add(imageHash)
                        }
                        .addOnFailureListener { e ->
                            Log.e("MainActivity", "Failed to fetch image bytes: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Failed to retrieve image list: ${e.message}")
            }
    }

    private fun calculateHash(imageBytes: ByteArray): String {
        // Use your preferred hashing algorithm (e.g., MD5 or SHA-256)
        // Here's an example using MD5 (for simplicity, use a better hashing algorithm in production)
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(imageBytes)

        // Convert byte array to hexadecimal representation
        return BigInteger(1, digest).toString(16).padStart(32, '0') // For MD5 hash (32 characters)
        // For SHA-256, replace "MD5" with "SHA-256" and adjust padding accordingly
    }


    private fun preventFirebaseDuplicates(bitmap: Bitmap, classificationText: String) {
        loadExistingImageUUIDs()
        loadExistingImageHashes()

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        // Calculate hash of the image data
        val imageHash = calculateHash(imageData)

        if (!savedImageHashes.contains(imageHash)) {
            // If the hash doesn't exist, store the image and update the hash set
            storeImageAndClassification(bitmap, classificationText)
            savedImageHashes.add(imageHash)
            Toast.makeText(this, "Image and Text saved successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("MainActivity", "Image already exists.")
            Toast.makeText(this, "Image already exists!", Toast.LENGTH_SHORT).show()
        }
    }

    // Add an additional parameter imageUUID for the image UUID
    private fun storeImageAndClassification(bitmap: Bitmap, classificationText: String) {
        val storageRef = firebaseStorage.reference
        val pairUUID = UUID.randomUUID().toString() // Generate a single UUID for image-text pair
        val imagesRef =
            storageRef.child("images/$currentUserID/$pairUUID.jpg") // creates separate folders for each user!!!
        val textRef =
            storageRef.child("classification/$currentUserID/$pairUUID.txt")

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
                        // Handle success
                    }.addOnFailureListener { textUrlFailure ->
                        Log.e("MainActivity", "Failed to get text URL: ${textUrlFailure.message}")
                        // Handle failure
                    }
                }.addOnFailureListener { textUploadFailure ->
                    Log.e(
                        "MainActivity",
                        "Error uploading classification text: ${textUploadFailure.message}"
                    )
                    // Handle failure
                }
            }.addOnFailureListener { imageUrlFailure ->
                Log.e("MainActivity", "Failed to get image URL: ${imageUrlFailure.message}")
                // Handle failure
            }
        }.addOnFailureListener { imageUploadFailure ->
            Log.e("MainActivity", "Error uploading image: ${imageUploadFailure.message}")
            // Handle failure
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1 -> {
                    // Gallery
                    val selectedImage: Uri? = data?.data
                    displayImage(selectedImage)
                }

                3 -> {
                    // Camera
                    val photo: Bitmap = data?.extras?.get("data") as Bitmap
                    val uri = getImageUri(photo)
                    displayImage(uri)
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

}
