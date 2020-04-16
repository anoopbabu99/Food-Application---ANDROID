package com.anoop.doordine.adapter

import android.app.AlertDialog
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
import com.anoop.doordine.activity.MainActivity
import com.anoop.doordine.activity.RestaurantMenuActivity
import com.anoop.doordine.database.RestaurantDatabase
import com.anoop.doordine.database.RestaurantEntity
import com.squareup.picasso.Picasso

class FavoriteRecyclerAdapter (val context: Context, val restaurantList: List<RestaurantEntity>):
    RecyclerView.Adapter<FavoriteRecyclerAdapter.FavoriteViewHolder>(){



    //_________________________INITIALIZE____________________________________
    class FavoriteViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        val txtStarCount: TextView = view.findViewById(R.id.txtstarCount)
        val imgBook: ImageView = view.findViewById(R.id.imgProfilePicture)
        val llContent: RelativeLayout = view.findViewById(R.id.llContent)
        val gifHeartBeating: pl.droidsonroids.gif.GifImageView = view.findViewById(R.id.gifHeartBeating)
        val gifHeartBreaking: pl.droidsonroids.gif.GifImageView = view.findViewById(R.id.gifHeartBreaking)
    }
    //_________________________INITIALIZE____________________________________

    //create view holder (connection to favorites_dashboard_single_row xml file)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_favorite_single_row, parent, false)

        return FavoriteViewHolder(view)
    }

    override fun getItemCount(): Int {
        return restaurantList.size
    }

    //_________________________GRAB ENTRY____________________________________
    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val entry = restaurantList[position]
        holder.txtRestaurantName.text = entry.resName
        holder.txtPrice.text = "₹"+entry.resPrice+"/person"
        holder.txtStarCount.text = entry.resRating
        //image is received in the form of a string URL; lets parse this URL:
        // picasso is used to populate images into image views.
        Picasso.get().load(entry.resImg).error(R.drawable.img_android_logo).into(holder.imgBook)

        //set up a click listener to the the view:
        holder.llContent.setOnClickListener {
            //make an intent to open the Description Activity:
            val intent = Intent(context, RestaurantMenuActivity::class.java)
            intent.putExtra("res_id", entry.resId.toString())
            intent.putExtra("res_name", entry.resName)
            context.startActivity(intent)
        }

        //remove favorites_______________________________________________________________
        var sharedPreferences: SharedPreferences
        sharedPreferences = context.getSharedPreferences("DoorDine Preferences", Context.MODE_PRIVATE)
        val resEntity = RestaurantEntity(
            entry.resId?.toInt() as Int,
            sharedPreferences.getString("userId", "American Airlines")?.toInt() as Int,
            entry.resName.toString(),
            entry.resPrice.toString(),
            entry.resRating.toString(),
            entry.resImg.toString()
        )





        holder.gifHeartBeating.setOnClickListener {
            holder.gifHeartBeating.visibility = View.GONE
            holder.gifHeartBreaking.visibility = View.VISIBLE
            val dialog = AlertDialog.Builder(context)

            dialog.setTitle("Remove")
            dialog.setMessage("Are you sure you want to remove this restaurant from favorites? ")
            dialog.setPositiveButton("Yes"){text,listener->
                val result = RestaurantRecyclerAdapter.DBAsyncTask(context, resEntity, 3).execute().get()

                //refresh immediately on screen:
                //this is an intent created to re-enter the favorites fragment so that the user feels his action is
                // immediately reflected on the fragment.
                //(this feature is not in the demo).

                //notify main activity that the intent is from the favorites.
                sharedPreferences.edit().putBoolean("isOpen", true).apply()
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)

            }
            dialog.setNegativeButton("Undo"){text,listener->
                //do nothing
                holder.gifHeartBeating.visibility = View.VISIBLE
                holder.gifHeartBreaking.visibility = View.GONE
            }
            dialog.setCancelable(false)

            dialog.create()
            dialog.show()



        }
    }
    //remove favorites_______________________________________________________________


    //_________________________GRAB ENTRY____________________________________

    //add to favorites:
    //void for params and progress (we will not display status) and boolean for result;
    //_______________________________FAVORITES DB_________________________________________________
    class DBAsyncTask(val context: Context, val resEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>(){
        //mode 1: check DB if book exists
        //mode 2: add the book to DB
        //mode 3: remove book from DB

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