package com.sttbandung.mycatalog

import android.app.ProgressDialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.util.Log
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlin.math.log


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var myAdapter: AdapterList
    private lateinit var itemList: MutableList<ItemList>
    private lateinit var db: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog

    private fun getData(){
     progressDialog.show()
        db.collection("news")
            .get().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    itemList.clear()
                    for (document in task.result){
                        val item = ItemList(
                            document.id,
                            document.getString("title") ?: "",
                            document.getString("desc") ?: "",
                            document.getString("imageUrl") ?: "",
                        )
                        itemList.add(item)
                        Log.d("data","${document.id} => ${document.data}")
                    }
                    myAdapter.notifyDataSetChanged()
                } else{
                    Log.w("data","Error getting document",task.exception)
                }
                progressDialog.dismiss()
            }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        val recyclerView = findViewById<RecyclerView>(R.id.rcvNews)
        val floatingActionButton = findViewById<FloatingActionButton>(R.id.floatAddNews)
        progressDialog = ProgressDialog(this@MainActivity).apply{
            setTitle("Loading...")
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemList = ArrayList()
        myAdapter = AdapterList(itemList)
        recyclerView.adapter = myAdapter

        myAdapter.setOnItemClickListener(object: AdapterList.OnItemClickListener{
            override fun onItemClick(item: ItemList) {
                val intent = Intent(this@MainActivity, NewsDetail::class.java).apply {
                    putExtra("id", item.id)
                    putExtra("title", item.judul)
                    putExtra("desc", item.subjudul)
                    putExtra("imageUrl", item.imageUrl)
                }
                startActivity(intent)
            }
        })
        floatingActionButton.setOnClickListener {
            val toAddPage = Intent(this@MainActivity, NewsAdd::class.java)
            startActivity(toAddPage)
        }

    }

    override fun onStart() {
        super.onStart()
        getData()
    }
}