package com.example.finalproject

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finalproject.ml.ModelUnquant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var result: TextView
    private val imageSize = 224

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseDB: FirebaseFirestore
    private lateinit var currentUserID: String

    private val savedImageAndTextSet = HashSet<String>()
    private val savedImageHashes = HashSet<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        val imageView = findViewById<ImageView>(R.id.imageView)
        result = findViewById(R.id.classificationTextView)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDB = FirebaseFirestore.getInstance()

        val currentUser = firebaseAuth.currentUser
        currentUserID = currentUser?.uid ?: ""

        val imageUriString = intent.getStringExtra("imageUri")

        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            imageView.setImageURI(imageUri)

            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            val resizedBitmap = resizeBitmap(bitmap, imageSize, imageSize)
            imageView.setImageBitmap(resizedBitmap)
            processImage(resizedBitmap)

            val saveButton = findViewById<Button>(R.id.saveButton)
            val cancelButton = findViewById<Button>(R.id.cancelButton)

            saveButton.setOnClickListener {
                preventFirebaseDuplicates(resizedBitmap, result.text.toString())

            }
            cancelButton.setOnClickListener {
                // Finish the activity when the cancel button is clicked
                finish()
            }
        } else {
            // Handle the case when the image URI is not available
            Log.e("ImageDetailActivity", "Image URI is null or empty.")
            finish()
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
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
        } else {

            val cleanScore = String.format("%.1f%%", outputFeature0.floatArray[0] * 100)
            val resultText = "$predictedClassLabel"
            result.text = resultText
            result.gravity = Gravity.CENTER
        }

        model.close()
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
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(imageBytes)

        // Convert byte array to hexadecimal representation
        return BigInteger(1, digest).toString(16).padStart(32, '0')
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
            //Toast.makeText(this, "Image and Text saved successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("MainActivity", "Image already exists.")
            Toast.makeText(this, "Image already exists!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeImageAndClassification(bitmap: Bitmap, classificationText: String) {
        val storageRef = firebaseStorage.reference
        val pairUUID = UUID.randomUUID().toString() // Generate a single UUID for image-text pair
        val imagesRef = storageRef.child("images/$currentUserID/$pairUUID.jpg")
        val textRef = storageRef.child("classification/$currentUserID/$pairUUID.txt")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        val uploadImageTask = imagesRef.putBytes(imageData)
        uploadImageTask.addOnSuccessListener { imageUploadTask ->
            imagesRef.downloadUrl.addOnSuccessListener { imageUrl ->
                val textBytes = classificationText.toByteArray()
                val uploadTextTask = textRef.putBytes(textBytes)
                uploadTextTask.addOnSuccessListener { textUploadTask ->
                    textRef.downloadUrl.addOnSuccessListener { textUrl ->
                        val docRef = firebaseDB.collection("images").document()
                        val timestamp = System.currentTimeMillis()
                        val date = Date(timestamp)
                        val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                        val formattedDate = formatter.format(date)

                        val data = hashMapOf(
                            "imageUrl" to imageUrl.toString(),
                            "textUrl" to textUrl.toString(),
                            "classification" to classificationText,
                            "userId" to currentUserID,
                            "timestamp" to formattedDate
                        )

                        docRef.set(data)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Image and Text saved successfully!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
            }
        }
    }
}


