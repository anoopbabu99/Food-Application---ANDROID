package com.anoop.doordine.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.anoop.doordine.R
import com.anoop.doordine.R.layout.recycler_restaurant_single_row
import com.anoop.doordine.activity.RestaurantMenuActivity
import com.anoop.doordine.database.RestaurantDatabase
import com.anoop.doordine.database.RestaurantEntity
import com.anoop.doordine.model.Restaurant
import com.squareup.picasso.Picasso

class RestaurantRecyclerAdapter(val context: Context, val itemList: ArrayList<Restaurant>):
                    RecyclerView.Adapter<RestaurantRecyclerAdapter.RestaurantViewHolder>(){



    //create view holder (connection to recycler_dashboard_single_row xml file)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(recycler_restaurant_single_row, parent, false)

        return RestaurantViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: RestaurantRecyclerAdapter.RestaurantViewHolder, position: Int) {
        val entry = itemList[position]

        holder.txtResName.text = entry.name
        holder.txtPrice.text = "₹"+entry.costForOne+"/person"
        holder.txtstarCount.text = entry.rating
        //image is received in the form of a string URL; lets parse this URL:
        //picasso is used to populate images into image views.
        Picasso.get().load(entry.image).error(R.drawable.img_android_logo).into(holder.imgRes)

        //set up a click listener to the the view:
        holder.llContent.setOnClickListener {
            //make an intent to open the Menu for that restaurant:
            val intent = Intent(context, RestaurantMenuActivity::class.java)
            intent.putExtra("res_id", entry.resId)
            intent.putExtra("res_name", entry.name)
            context.startActivity(intent)
        }

        //set up favorites_______________________________________________________________
        var sharedPreferences: SharedPreferences
        sharedPreferences = context.getSharedPreferences("DoorDine Preferences", Context.MODE_PRIVATE)

        val resEntity = RestaurantEntity(
            entry.resId?.toInt() as Int,
            sharedPreferences.getString("userId", "American Airlines")?.toInt() as Int,
            entry.name.toString(),
            entry.costForOne.toString(),
            entry.rating.toString(),
            entry.image.toString()
        )

        val checkFav = DBAsyncTask(context,resEntity, 1).execute()
        val isFav = checkFav.get()

        if(isFav)
        {
            holder.imgHeartFull.visibility = View.VISIBLE
        }
        else{
            holder.imgHeartFull.visibility = View.GONE
        }


        holder.imgHeartBorder.setOnClickListener {

            if (!DBAsyncTask(context,resEntity,1).execute().get()) {
                val result = DBAsyncTask(context, resEntity, 2).execute().get()
                if(result)  holder.imgHeartFull.visibility = View.VISIBLE
            }
            else{
                val result = DBAsyncTask(context,resEntity, 3).execute().get()
                if(result) holder.imgHeartFull.visibility = View.GONE
            }
        }
        //set up favorites_______________________________________________________________

    }

    //_________________________INITIALIZE____________________________________
    class RestaurantViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtResName: TextView = view.findViewById(R.id.txtResName)
        val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        val txtstarCount:TextView = view.findViewById(R.id.txtstarCount)
        val imgRes: ImageView = view.findViewById(R.id.imgRes)
        val llContent: RelativeLayout = view.findViewById(R.id.llContent)
        val imgHeartBorder: ImageView = view.findViewById(R.id.imgHeart)
        val imgHeartFull: ImageView = view.findViewById(R.id.imgHeart2)

    }
    //_________________________INITIALIZE____________________________________
    //add to favorites:
    //void for params and progress (we will not display status) and boolean for result;
    //_______________________________FAVORITES DB_________________________________________________
    class DBAsyncTask(val context: Context, val resEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>(){
        //mode 1: check DB if restaurant exists
        //mode 2: add the restaurant to DB
        //mode 3: remove restaurant from DB

        //initialize DB:
        val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "rest-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when(mode){
                1->{
                    val book: RestaurantEntity? = db.restaurantDao().getRestById(resEntity.resId.toString(),
                        resEntity.userId.toString())
                    db.close()
                    return book!=null

                }
                2->{
                    db.restaurantDao().insertRest(resEntity)
                    db.close()
                    return true
                }
                3->{
                    db.restaurantDao().deleteRest(resEntity)
                    db.close()
                    return true
                }
            }
            return false;
        }

    }
    //_______________________________FAVORITES DB_________________________________________________

}

