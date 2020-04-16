package com.anoop.doordine.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.anoop.doordine.R
import com.anoop.doordine.adapter.RestaurantRecyclerAdapter
import com.anoop.doordine.model.Restaurant
import com.anoop.doordine.util.ConnectionManager
import kotlinx.android.synthetic.main.sort_radio_button.view.*
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */
class AllRestaurantsFragment : Fragment() {
    lateinit var recyclerRestaurant: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: RestaurantRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    lateinit var radioButtonView:View


    //initialize restaurant list:
    val restaurantList = arrayListOf<Restaurant>()

    var ratingComparator= Comparator<Restaurant> { rest1, rest2 ->

        if(rest1.rating.compareTo(rest2.rating,true)==0){
            rest1.name.compareTo(rest2.name,true)
        }
        else{
            rest1.rating.compareTo(rest2.rating,true)
        }

    }

    var costComparator= Comparator<Restaurant> { rest1, rest2 ->


        if(rest1.costForOne.compareTo(rest2.costForOne,true)==0){
            rest1.name.compareTo(rest2.name,true)
        }
        else{
            rest1.costForOne.compareTo(rest2.costForOne,true)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        val view=  inflater.inflate(R.layout.fragment_all_restaurants, container, false)

        recyclerRestaurant = view.findViewById(R.id.recyclerRestaurant)
        layoutManager = LinearLayoutManager(activity)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)





        progressLayout.visibility = View.VISIBLE //show progress bar

        //_________________________INTERNET___________________________________________________

        // Create a request queue (series of requests sent to the server via a JSON file to the API).
        // The API queries it out from the database and returns it to the APP (in the form  of another JSON file).


        if(activity!=null) {
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/restaurants/fetch_result/"
            if (ConnectionManager().checkConnectivity(activity as Context)) {
                //one JSON object is a block (like the book class). A JSON array is an array of JSON objects.
                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener {
                        //here we will handle the response: (i.e. parse the JSON file received by the API)
                        try {

                            val JsonObject = it.getJSONObject("data")
                            progressLayout.visibility = View.GONE //hide progress bar
                            val success = JsonObject.getBoolean("success")
                            if (success) {
                                //extract data from JSON array and insert it into the initialized ArrayList:

                                val dataReceived = JsonObject.getJSONArray("data")
                                for (i in 0 until dataReceived.length()) {
                                    val restJsonObject = dataReceived.getJSONObject(i)
                                    //parse each JSON object into a BOOK object:
                                    val bookObject = Restaurant(
                                        restJsonObject.getString("id"),
                                        restJsonObject.getString("name"),
                                        restJsonObject.getString("rating"),
                                        restJsonObject.getString("cost_for_one"),
                                        restJsonObject.getString("image_url")
                                    )
                                    restaurantList.add(bookObject)
                                    //connection to RestaurantRecyclerAdapter class (i.e. send it to the adapter)
                                    if (activity != null) {
                                        recyclerAdapter = RestaurantRecyclerAdapter(
                                            activity as Context,
                                            restaurantList
                                        )
                                        recyclerRestaurant.adapter = recyclerAdapter
                                        recyclerRestaurant.layoutManager = layoutManager
                                    }

                                }


                            } else {
                                //display error message
                                if (activity != null)
                                    Toast.makeText(
                                        activity as Context,
                                        "Some error occurred while parsing JSON file received",
                                        Toast.LENGTH_SHORT
                                    ).show()
                            }
                        } catch (e: JSONException) {
                            if (activity != null)
                                Toast.makeText(
                                    activity as Context,
                                    "Some unexpected error occurred while fetching response",
                                    Toast.LENGTH_SHORT
                                ).show()
                        }
                    },
                    Response.ErrorListener {
                        //here we will handle the errors
                        if (activity != null) //resolve crash.
                            Toast.makeText(
                                activity as Context,
                                "Volley error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                ) {
                    @Override
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "7b832cd6a75856"
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)
            } else {
                //there is no net
                val dialog = AlertDialog.Builder(activity as Context)
                dialog.setTitle("ERROR")
                dialog.setMessage("Internet Connection NOT Found")
                dialog.setPositiveButton("Open Settings") { text, listener ->
                    //use an implicit intent: (open things that are in the phone but outside the APP)
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    activity?.finish()
                }
                dialog.setNegativeButton("Exit") { text, listener ->
                    //do nothing
                    val dialog2 = AlertDialog.Builder(activity as Context)
                    dialog2.setTitle("EXIT")
                    dialog2.setMessage("Are you sure you want to exit?")
                    dialog2.setPositiveButton("Yes") { text, listener ->
                        ActivityCompat.finishAffinity(activity as Activity)
                    }
                    dialog2.setNegativeButton("No") { text, listener ->
                        //do nothing
                    }
                    dialog2.create()
                    dialog2.show()

                }
                dialog.create()
                dialog.show()
            }
        }
        //_________________________INTERNET___________________________________________________

        return view

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_all_restaurants,menu)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {

        val id=item.itemId

        when(id){

            R.id.sort->{
                radioButtonView= View.inflate(activity as Context,R.layout.sort_radio_button,null)//radiobutton view for sorting display
                androidx.appcompat.app.AlertDialog.Builder(activity as Context)
                    .setTitle("Sort By?")
                    .setView(radioButtonView)
                    .setPositiveButton("OK") { text, listener ->
                        if (radioButtonView.radio_high_to_low.isChecked) {
                            Collections.sort(restaurantList, costComparator)
                            restaurantList.reverse()
                            recyclerAdapter.notifyDataSetChanged()//updates the adapter
                        }
                        if (radioButtonView.radio_low_to_high.isChecked) {
                            Collections.sort(restaurantList, costComparator)
                            recyclerAdapter.notifyDataSetChanged()//updates the adapter
                        }
                        if (radioButtonView.radio_rating.isChecked) {
                            Collections.sort(restaurantList, ratingComparator)
                            restaurantList.reverse()
                            recyclerAdapter.notifyDataSetChanged()//updates the adapter
                        }
                    }
                    .setNegativeButton("CANCEL") { text, listener ->

                    }
                    .create()
                    .show()
            }
        }

        return super.onOptionsItemSelected(item)
    }




}
