package com.anoop.doordine.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room

import com.anoop.doordine.R
import com.anoop.doordine.adapter.FavoriteRecyclerAdapter
import com.anoop.doordine.database.RestaurantDatabase
import com.anoop.doordine.database.RestaurantEntity

/**
 * A simple [Fragment] subclass.
 */
class FavoritesFragment : Fragment() {

    lateinit var recyclerFavorite: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: FavoriteRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
    lateinit var txtNoFavorites: TextView
    lateinit var gifHeart:pl.droidsonroids.gif.GifImageView




    //create bookList:
    var dbRestList = listOf<RestaurantEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_favorites, container, false)

        //initialize variables:
        recyclerFavorite = view.findViewById(R.id.recyclerFavorites)
        layoutManager = GridLayoutManager(activity as Context,1) // span count == # of items in a row.
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)
        gifHeart = view.findViewById(R.id.gifHeartBeating)
        txtNoFavorites = view.findViewById(R.id.txtNoFav)

        gifHeart.visibility = View.GONE
        txtNoFavorites.visibility = View.GONE


        progressLayout.visibility = View.VISIBLE //show progress bar (might need to delete)

        //a fragement can not access application context, but it can access the activity context

        dbRestList = RetrieveFavorites(activity as Context).execute().get()

        if(dbRestList.size ==0 ) {
            gifHeart.visibility = View.VISIBLE
            txtNoFavorites.visibility = View.VISIBLE
        }

        if(activity!=null){
            progressLayout.visibility = View.GONE
            recyclerAdapter = FavoriteRecyclerAdapter  (activity as Context, dbRestList)
            recyclerFavorite.adapter = recyclerAdapter
            recyclerFavorite.layoutManager = layoutManager
        }


        return view
    }

    class RetrieveFavorites(val context: Context): AsyncTask<Void, Void, List<RestaurantEntity>>(){


        override fun doInBackground(vararg params: Void?): List<RestaurantEntity> {
            //get all favorites given a user id (i.e. 2 or more users that login the same device must have custom favorites):
            var sharedPreferences:SharedPreferences
            sharedPreferences = context.getSharedPreferences("DoorDine Preferences", Context.MODE_PRIVATE)
            //initialize DB
            val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "rest-db").build()
            return db.restaurantDao().getAllRests(sharedPreferences.getString("userId", "American Airlines"))
        }

    }

}
