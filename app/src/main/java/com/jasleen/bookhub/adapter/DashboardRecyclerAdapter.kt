package com.jasleen.bookhub.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jasleen.bookhub.activity.DescriptionActivity
import com.jasleen.bookhub.R
import com.jasleen.bookhub.model.Book
import com.squareup.picasso.Picasso

class DashboardRecyclerAdapter(val context: Context, val itemList: ArrayList<Book>) : RecyclerView.Adapter<DashboardRecyclerAdapter.DashboardViewHolder>(){

    class DashboardViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtRecyclerRowItem : TextView = view.findViewById(R.id.txtRecyclerRowItem)
        val imgRecyclerBookIcon: ImageView = view.findViewById(R.id.imgRecyclerBookIcon)
        val txtRecyclerAuthorName: TextView = view.findViewById(R.id.txtRecyclerAuthorName)
        val txtRecyclerCost: TextView = view.findViewById(R.id.txtRecyclerCost)
        val imgRecyclerStarIcon: ImageView = view.findViewById(R.id.imgRecyclerStarIcon)
        val txtRecyclerRating: TextView = view.findViewById(R.id.txtRecyclerRating)
        val llContent : RelativeLayout = view.findViewById(R.id.llContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_dashboard_single_row, parent, false)
        return DashboardViewHolder(view)
    }


    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) {
        val book = itemList[position]
        holder.txtRecyclerRowItem.text = book.bookName
        holder.txtRecyclerAuthorName.text = book.bookAuthor
        holder.txtRecyclerCost.text = book.bookPrice
        holder.txtRecyclerRating.text = book.bookRating
        Picasso.get().load(book.bookImage).error(R.drawable.book_app_icon_web).into(holder.imgRecyclerBookIcon)

        holder.llContent.setOnClickListener {
            val intent = Intent(context, DescriptionActivity::class.java)
            intent.putExtra("book_id", book.bookId)
            context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

}