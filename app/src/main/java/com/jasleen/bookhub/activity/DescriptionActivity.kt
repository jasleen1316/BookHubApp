package com.jasleen.bookhub.activity

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.jasleen.bookhub.R
import com.jasleen.bookhub.database.BookDatabase
import com.jasleen.bookhub.database.BookEntity
import com.squareup.picasso.Picasso
import org.json.JSONObject

class DescriptionActivity : AppCompatActivity() {

    lateinit var imgBookImage: ImageView
    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtBookPrice: TextView
    lateinit var txtBookRating: TextView
    lateinit var txtBookDesc: TextView
    lateinit var btnAddToFavs: Button
    lateinit var ProgressLayout: RelativeLayout
    lateinit var ProgressBar: ProgressBar
    var bookId : String? = "100"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        imgBookImage = findViewById(R.id.imgBookImage)
        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        txtBookDesc = findViewById(R.id.txtBookDesc)
        btnAddToFavs = findViewById(R.id.btnAddToFavs)
        ProgressLayout = findViewById(R.id.ProgressLayout)
        ProgressBar = findViewById(R.id.ProgressBar)


        ProgressLayout.visibility = View.VISIBLE
        ProgressBar.visibility = View.VISIBLE
        supportActionBar?.title = "Book Details"

        // RECEIVING DATA FROM DASHBOARD FRAGMENT

        if(intent != null){
             bookId = intent.getStringExtra("book_id")
        }else{
            finish()
            Toast.makeText(
                this@DescriptionActivity,
                "Description could not be displayed",
                Toast.LENGTH_LONG).show()
        }

        if(bookId == "100"){
            Toast.makeText(
                this@DescriptionActivity,
                "Description could not be displayed",
                Toast.LENGTH_LONG).show()
        }




        // FETCHING DATA FROM URL USING HTTP REQUESTS

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id", bookId)

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            url,
            jsonParams,
            Response.Listener {
                try{
                if (it.getBoolean("success")) {


                    val bookJsonObject = it.getJSONObject("book_data")
                    ProgressLayout.visibility = View.GONE

                    txtBookName.text = bookJsonObject.getString("name")
                    txtBookAuthor.text = bookJsonObject.getString("author")
                    txtBookPrice.text = bookJsonObject.getString("price")
                    txtBookRating.text = bookJsonObject.getString("rating")
                    txtBookDesc.text = bookJsonObject.getString("description")
                    Picasso.get().load(bookJsonObject.getString("image"))
                        .error(R.drawable.book_app_icon_web).into(imgBookImage)

                    val bookImageUrl = bookJsonObject.getString("image")
                    val bookEntity = BookEntity(
                        bookId?.toInt() as Int,
                        txtBookName.text.toString(),
                        txtBookAuthor.text.toString(),
                        txtBookRating.text.toString(),
                        txtBookPrice.text.toString(),
                        txtBookDesc.text.toString(),
                        bookImageUrl
                    )

                    val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                    val isFav = checkFav.get()
                    if(isFav == true){
                        btnAddToFavs.text = "Remove From Favourites"
                        val favColour = ContextCompat.getColor(applicationContext,
                            R.color.colorFavourites
                        )
                        btnAddToFavs.setBackgroundColor(favColour)
                    }else{
                        btnAddToFavs.text = "Add To Favourites"
                        val favColour = ContextCompat.getColor(applicationContext,
                            R.color.design_default_color_on_primary
                        )
                        btnAddToFavs.setBackgroundColor(favColour)
                    }

                    btnAddToFavs.setOnClickListener {
                        if(!DBAsyncTask(applicationContext, bookEntity, 1).execute().get()){
                            val async = DBAsyncTask(applicationContext, bookEntity, 2).execute()
                            val result = async.get()
                            if(result){
                                Toast.makeText(this@DescriptionActivity, "Book added to favourites", Toast.LENGTH_SHORT).show()

                                btnAddToFavs.text = "Remove From Favourites"
                                val favColour = ContextCompat.getColor(applicationContext,
                                    R.color.colorFavourites
                                )
                                btnAddToFavs.setBackgroundColor(favColour)
                            }else{
                                Toast.makeText(this@DescriptionActivity, "Some error occurred", Toast.LENGTH_SHORT).show()

                            }
                        }else{
                            val async = DBAsyncTask(applicationContext, bookEntity, 3).execute()
                            val result = async.get()
                            if(result){
                                Toast.makeText(this@DescriptionActivity, "Book removed from favourites", Toast.LENGTH_SHORT).show()

                                btnAddToFavs.text = "Add To Favourites"
                                val favColour = ContextCompat.getColor(applicationContext,
                                    R.color.design_default_color_on_primary
                                )
                                btnAddToFavs.setBackgroundColor(favColour)
                            }else{
                                Toast.makeText(this@DescriptionActivity, "Some error occurred", Toast.LENGTH_SHORT).show()

                            }
                        }
                    }

                }else{
                    Toast.makeText(this@DescriptionActivity, "Some error has occurred", Toast.LENGTH_LONG).show()
                }
            }catch(e: Exception){
                    Toast.makeText(this@DescriptionActivity, "Some error occurred", Toast.LENGTH_LONG).show()
                }

            },
            Response.ErrorListener {
                Toast.makeText(this@DescriptionActivity, "Volley error occurred", Toast.LENGTH_LONG).show()
            }
        ){
            //headers
            override fun getHeaders(): MutableMap<String, String>{
                val headers =   HashMap<String, String>()
                headers["Content-type"] = "application/json"
                headers["token"] = "046a59f668ea81"
                return headers
            }
        }

        queue.add(jsonObjectRequest)


    }

    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int): AsyncTask<Void, Void, Boolean>(){

        // Mode 1-> Check DB if the book is favourite or not
        // Mode 2-> Save the book into DB as favourite
        // Mode 3-> Remove the favourite book
        val db =  Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            when(mode){

                1 -> {
                    // Check DB if the book is favourite or not
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2 -> {
                    // Mode 2-> Save the book into DB as favourite
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3 -> {
                    // Mode 3-> Remove the favourite book
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }

            }

            return false
        }

    }

}