package com.puccampinas.textrecon

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.graphics.Bitmap

import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : AppCompatActivity() {
    private lateinit var textViewPlaceholder: TextView
    private lateinit var recognizer: TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textViewPlaceholder = findViewById(R.id.textViewPlaceholder)
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        val buttonCamera = findViewById<Button>(R.id.buttonCamera)
        val buttonGallery = findViewById<Button>(R.id.buttonGallery)

        val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                recognizeText(imageBitmap)
            }
        }

        val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let {
                    val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                    recognizeText(imageBitmap)
                }
            }
        }

        buttonCamera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(takePictureIntent)
        }

        buttonGallery.setOnClickListener {
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageIntent.type = "image/*"
            pickImageLauncher.launch(pickImageIntent)
        }
    }

    private fun recognizeText(imageBitmap: Bitmap?) {
        imageBitmap?.let {
            val image = InputImage.fromBitmap(it, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    textViewPlaceholder.text = visionText.text
                }
                .addOnFailureListener { e ->
                    textViewPlaceholder.text = "Texto não reconhecido"
                }
        } ?: run {
            textViewPlaceholder.text = "Imagem não encontrada"
        }
    }
}
