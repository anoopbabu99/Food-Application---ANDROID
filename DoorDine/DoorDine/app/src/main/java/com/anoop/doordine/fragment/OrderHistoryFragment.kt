package com.anoop.doordine.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.anoop.doordine.adapter.OrderHistoryAdapter
import com.anoop.doordine.database.MenuItemEntity
import com.anoop.doordine.model.OrderHistoryRestaurant
import com.anoop.doordine.util.ConnectionManager
import org.json.JSONException

/**
 * A simple [Fragment] subclass.
 */
class OrderHistoryFragment : Fragment() {

    lateinit var sharedPreferences: SharedPreferences

    lateinit var layoutManager1: RecyclerView.LayoutManager
    lateinit var menuAdapter1: OrderHistoryAdapter
    lateinit var recyclerViewAllOrders:RecyclerView
    lateinit var order_activity_history_Progressdialog: RelativeLayout
    lateinit var order_history_fragment_no_orders:RelativeLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view= inflater.inflate(R.layout.fragment_order_history, container, false)

        recyclerViewAllOrders=view.findViewById(R.id.recyclerViewAllOrders)

        order_activity_history_Progressdialog=view.findViewById(R.id.order_activity_history_Progressdialog)

        order_history_fragment_no_orders=view.findViewById(R.id.order_history_fragment_no_orders)

        //connect adapter to OrderHistoryAdapter:

        layoutManager1= LinearLayoutManager(activity as Context)

        val orderedRestaurantList=ArrayList<OrderHistoryRestaurant>()
        val myNestedArrayList= ArrayList<ArrayList<MenuItemEntity>>()

        sharedPreferences = this.getActivity()!!.getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)

        val user_id=sharedPreferences.getString("userId","000")

        if(activity!=null) {
            //_________________________INTERNET___________________________________________________

            // Create a request queue (series of requests sent to the server via a JSON file to the API).
            // The API queries it out from the database and returns it to the APP (in the form  of another JSON file).
            //order_activity_history_Progressdialog.visibility=View.VISIBLE
            val queue = Volley.newRequestQueue(activity as Context)
            val url = "http://13.235.250.119/v2/orders/fetch_result/$user_id"
            if (ConnectionManager().checkConnectivity(activity as Context)) {

                order_activity_history_Progressdialog.visibility = View.VISIBLE


                //one JSON object is a block (like the book class). A JSON array is an array of JSON objects.
                val jsonObjectRequest = object : JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    Response.Listener {
                        //here we will handle the response: (i.e. parse the JSON file received by the API)
                        try {
                            val responseJsonObjectData = it.getJSONObject("data")

                            val success = responseJsonObjectData.getBoolean("success")

                            if (success) {
                                val data = responseJsonObjectData.getJSONArray("data")

                                if (data.length() == 0) {//no items present display toast
                                    order_history_fragment_no_orders.visibility = View.VISIBLE

                                } else {
                                    order_history_fragment_no_orders.visibility = View.INVISIBLE

                                    myNestedArrayList.clear()

                                    for (i in 0 until data.length()) {
                                        val restaurantItemJsonObject = data.getJSONObject(i)

                                        val eachRestaurantObject = OrderHistoryRestaurant(
                                            restaurantItemJsonObject.getString("order_id"),
                                            restaurantItemJsonObject.getString("restaurant_name"),
                                            restaurantItemJsonObject.getString("total_cost"),
                                            restaurantItemJsonObject.getString("order_placed_at")
                                                .substring(0, 10)
                                        )// only date is extracted

                                        orderedRestaurantList.add(eachRestaurantObject)

                                        val foodOrderedJsonArray =
                                            restaurantItemJsonObject.getJSONArray("food_items")

                                        var orderItemsPerRestaurant = ArrayList<MenuItemEntity>()

                                        for (j in 0 until foodOrderedJsonArray.length())//loop through all the items
                                        {

                                            val eachFoodItem =
                                                foodOrderedJsonArray.getJSONObject(j)//each food item
                                            val itemObject = MenuItemEntity(
                                                eachFoodItem.getString("food_item_id").toInt(),
                                                23, //not relevant
                                                "xyz", //not relevant
                                                eachFoodItem.getString("name"),
                                                eachFoodItem.getString("cost")
                                            )
                                            orderItemsPerRestaurant.add(itemObject)
                                        }

                                        myNestedArrayList.add(orderItemsPerRestaurant)

                                        if (activity != null) {
                                            menuAdapter1 = OrderHistoryAdapter(
                                                activity as Context,
                                                orderedRestaurantList,
                                                myNestedArrayList
                                            )//set up the adapter and pass the data.


                                            recyclerViewAllOrders.adapter =
                                                menuAdapter1//bind the  recyclerView to the adapter.

                                            recyclerViewAllOrders.layoutManager =
                                                layoutManager1 //bind the  recyclerView to the layoutManager.

                                        }
                                    }


                                }
                            } else {
                                //display error message
                                Toast.makeText(
                                    activity as Context,
                                    "Some error occurred while parsing JSON file received",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: JSONException) {
                            Toast.makeText(
                                activity as Context,
                                "Some unexpected error occurred while fetching response",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        order_activity_history_Progressdialog.visibility = View.INVISIBLE

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

            //_________________________INTERNET___________________________________________________

        }

        return view
    }



}
