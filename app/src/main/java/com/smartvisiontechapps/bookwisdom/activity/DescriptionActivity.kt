package com.smartvisiontechapps.bookwisdom.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.DecorToolbar
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.smartvisiontechapps.bookwisdom.R
import com.smartvisiontechapps.bookwisdom.database.BookDatabase
import com.smartvisiontechapps.bookwisdom.database.BookEntity
import com.smartvisiontechapps.bookwisdom.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.util.Objects

class DescriptionActivity : AppCompatActivity() {

    lateinit var txtBookName:TextView
    lateinit var txtBookAuthor:TextView
    lateinit var txtBookPrice:TextView
    lateinit var txtBookRating: TextView
    lateinit var imgBookImage: ImageView
    lateinit var txtBookDesc:TextView
    lateinit var btnAddToFav:Button
    lateinit var progressBar: ProgressBar
    lateinit var progressLayout: RelativeLayout
    var bookId:String? = "100"

    lateinit var toolbar: Toolbar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        imgBookImage = findViewById(R.id.imgBookImage)
        txtBookDesc = findViewById(R.id.txtBookDesc)
        btnAddToFav = findViewById(R.id.btnAddToFav)
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.VISIBLE
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"


        if(intent != null){
            bookId = intent.getStringExtra("book_id")
        } else{
            finish()
            Toast.makeText(this@DescriptionActivity,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()
        }

        if(bookId == "100"){
            finish()
            Toast.makeText(this@DescriptionActivity,"Some unexpected error occurred",Toast.LENGTH_SHORT).show()
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"
        val jsonParams = JSONObject()
        jsonParams.put("book_id",bookId)

        if(ConnectionManager().checkConnectivity(this@DescriptionActivity)){

            val jsonRequest = object : JsonObjectRequest(com.android.volley.Request.Method.POST,url, jsonParams, Response.Listener {

                try{
                    val success = it.getBoolean("success")
                    if(success){
                        val bookJsonObject = it.getJSONObject("book_data")
                        progressLayout.visibility = View.GONE

                        val bookImageUrl = bookJsonObject.getString("image")

                        Picasso.get().load(bookJsonObject.getString("image")).error(R.drawable.default_book_cover).into(imgBookImage)
                        txtBookName.text = bookJsonObject.getString("name")
                        txtBookAuthor.text = bookJsonObject.getString("author")
                        txtBookPrice.text = bookJsonObject.getString("price")
                        txtBookRating.text = bookJsonObject.getString("rating")
                        txtBookDesc.text = bookJsonObject.getString("description")

                        val bookEntity = BookEntity(
                            bookId?.toInt() as Int,
                            txtBookName.text.toString(),
                            txtBookAuthor.text.toString(),
                            txtBookPrice.text.toString(),
                            txtBookRating.text.toString(),
                            txtBookDesc.text.toString(),
                            bookImageUrl
                        )

                        val checkFav = DBAsyncTask(applicationContext,bookEntity,1).execute()
                        val isFav = checkFav.get()

                        if(isFav){
                            btnAddToFav.text = "Remove from favourites"
                            val favColor = ContextCompat.getColor(applicationContext,R.color.colorFavourite)
                            btnAddToFav.setBackgroundColor(favColor)
                        }
                        else{
                            btnAddToFav.text = "Add to Favourites"
                            val favColor = ContextCompat.getColor(applicationContext,R.color.primary_color)
                            btnAddToFav.setBackgroundColor(favColor)
                        }

                        btnAddToFav.setOnClickListener {
                            if(! DBAsyncTask(applicationContext,bookEntity,1).execute().get()){
                                val async = DBAsyncTask(applicationContext,bookEntity,2).execute()
                                val result = async.get()
                                if(result){
                                    Toast.makeText(this@DescriptionActivity,"Book Added to favourites",Toast.LENGTH_SHORT).show()

                                    btnAddToFav.text = "Remove from favourites"
                                    val favColor = ContextCompat.getColor(applicationContext,R.color.colorFavourite)
                                    btnAddToFav.setBackgroundColor(favColor)
                                } else{
                                    Toast.makeText(this@DescriptionActivity,"Some error occurred",Toast.LENGTH_SHORT).show()
                                }
                            }
                            else{
                                val async = DBAsyncTask(applicationContext,bookEntity,3).execute()
                                val result = async.get()

                                if(result){
                                    Toast.makeText(this@DescriptionActivity,"Book removed from favourites",Toast.LENGTH_SHORT).show()
                                    btnAddToFav.text = "Add to favourites"
                                    val favColor = ContextCompat.getColor(applicationContext,R.color.primary_color)
                                    btnAddToFav.setBackgroundColor(favColor)
                                }
                                else{
                                    Toast.makeText(this@DescriptionActivity,"Some error occurred",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                    }
                    else{
                        Toast.makeText(this@DescriptionActivity,"No more details about this book available",Toast.LENGTH_SHORT).show()
                    }

                } catch(e:Exception){

                    Toast.makeText(this@DescriptionActivity,"Some error occurred",Toast.LENGTH_SHORT).show()

                }

            },Response.ErrorListener {
                Toast.makeText(this@DescriptionActivity,"Volley Error $it", Toast.LENGTH_SHORT).show()

            }){
                override fun getHeaders():MutableMap<String,String> {

                    val headers = HashMap<String,String>()
                    headers["Content-Type"] = "application/json"
                    headers["token"] = "4ea707fec3c1e6"
                    return headers
                }
            }
            queue.add(jsonRequest)

        }
        else{
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection Not Found")
            dialog.setPositiveButton("Open Settings"){ text,listener->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit"){text,listener->
                ActivityCompat.finishAffinity(this@DescriptionActivity)

            }
            dialog.create()
            dialog.show()
        }


    }

    class DBAsyncTask(val context:Context, val bookEntity:BookEntity, val mode:Int): AsyncTask<Void,Void,Boolean>(){

        /*
        Mode 1-> check db if the book is fav or not
        Mode 2-> save the book into db as favourite
        Mode 3-> Remove the fav book
         */


        val db = Room.databaseBuilder(context,BookDatabase::class.java,"books-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            when(mode){
                1->{
                    //check db if the book is fav or not

                    val book:BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2->{
                    //save the book into db as favourite

                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3->{

                    //Remove the fav book

                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true

                }
            }
            return false
        }

    }
}