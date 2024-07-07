package com.sttbandung.mycatalog

import androidx.activity.enableEdgeToEdge

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.CaseMap.Title
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import kotlin.math.log

class NewsAdd : AppCompatActivity() {
    private var id: String? = ""
    private var judul: String? = null
    private var deskripsi: String? = null
    private var image: String? = null

    companion object {
        private val PICK_IMAGE_REQUEST = 1
    }


    private lateinit var title: EditText
    private lateinit var desc: EditText
    private lateinit var imageView: ImageView
    private lateinit var saveNews: Button
    private lateinit var chooseImage: Button
    private var imageUri: Uri? = null

    private lateinit var dbNews: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_news_add)

        // Initialize Firebase
        dbNews = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize UI components
        title = findViewById(R.id.title)
        desc = findViewById(R.id.desc)
        imageView = findViewById(R.id.imageView)
        saveNews = findViewById(R.id.btnAdd)
        chooseImage = findViewById(R.id.btnChooseImage)
        progressDialog = ProgressDialog(this@NewsAdd).apply {
            setTitle("Loading...")
        }

        val updateOption = intent
        if (updateOption!=null){
            id = updateOption.getStringExtra("id")
            judul = updateOption.getStringExtra("title")
            deskripsi = updateOption.getStringExtra("desc")
            image = updateOption.getStringExtra("imageUrl")

            title.setText(judul);
            desc.setText(deskripsi)
            Glide.with(this).load(image).into(imageView)
        }

        chooseImage.setOnClickListener {
            // Add your logic here
            openFileChooser()
        }

        saveNews.setOnClickListener {
            // Add your logic here
            val newsTitle = title.text.toString().trim()
            val newsDesc = desc.text.toString().trim()
            if (newsTitle.isEmpty() || newsDesc.isEmpty()) {
                Toast.makeText(this@NewsAdd, "Title and description cannot be empty ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressDialog.show()

            Log.d("NewsAdd", "Progress dialog shown")
            if (imageUri != null) {
                Log.d("NewsAdd", "Uploading image to storage")
                uploadImageToStorage(newsTitle, newsDesc)
            } else {
                Log.d("NewsAdd", "Saving data without new image")
                saveData(newsTitle, newsDesc, image ?: "")
            }


        }

    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun uploadImageToStorage(newsTitle: String, newsDesc: String) {
       imageUri?.let { uri->
           val storageRef = storage.reference.child("news_images/" + System.currentTimeMillis() + ".jpg")
           storageRef.putFile(uri)
               .addOnSuccessListener {
                   storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                       val imageUrl = downloadUrl.toString()
                       saveData(newsTitle,newsDesc,imageUrl)
                   }
               }
               .addOnFailureListener { e ->
                   progressDialog.dismiss()
                   Toast.makeText(this@NewsAdd, "Failed to upload images", Toast.LENGTH_SHORT).show()
               }
       }
    }

    private fun saveData(newsTitle: String, newsDesc: String, imageUrl: String) {
        val news = HashMap<String, Any>()
        news["title"] = newsTitle
        news["desc"] = newsDesc
        news["imageUrl"] = imageUrl

        if (id!=null){
            dbNews.collection("news").document(id?:"")
                .update(news)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                     Toast.makeText(this@NewsAdd, "News update successfully", Toast.LENGTH_SHORT).show()
//
                    finish()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this@NewsAdd, "Error updating news: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.w("NewsAdd", "Error adding document", e)
                }
        }else{
            dbNews.collection("news")
                .add(news)
                .addOnSuccessListener { documentReference ->
                    progressDialog.dismiss()
                    Toast.makeText(this@NewsAdd, "News added successfully", Toast.LENGTH_SHORT).show()
                    title.setText("")
                    desc.setText("")
                    imageView.setImageResource(0) // Clear the ImageView
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this@NewsAdd, "Error adding news: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.w("NewsAdd", "Error adding document", e)
                }
        }

    }

}