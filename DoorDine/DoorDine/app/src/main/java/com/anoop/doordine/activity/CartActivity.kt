package com.anoop.doordine.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anoop.doordine.R
import com.anoop.doordine.adapter.CartRecyclerAdapter
import com.anoop.doordine.database.MenuItemDatabase
import com.anoop.doordine.database.MenuItemEntity
import com.anoop.doordine.util.ConnectionManager
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

class CartActivity : AppCompatActivity() {

    lateinit var toolbar:androidx.appcompat.widget.Toolbar
    lateinit var txtViewOrderingFrom: TextView
    lateinit var btnPlaceOrder: Button
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var menuAdapter: CartRecyclerAdapter
    lateinit var linearLayout: LinearLayout


    lateinit var progressLayout: RelativeLayout

    var totalAmount = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        //initialize:

        btnPlaceOrder=findViewById(R.id.btnPlaceOrder)
        txtViewOrderingFrom=findViewById(R.id.txtOrderingFrom)
        linearLayout=findViewById(R.id.linearLayout)
        toolbar=findViewById(R.id.toolBar)
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.INVISIBLE


        setToolBar()

        layoutManager = LinearLayoutManager(this@CartActivity)

        recyclerView = findViewById(R.id.recyclerViewCart)


        //grab items:_________________________________________________________________________

        var dbRestList = listOf<MenuItemEntity>()
        dbRestList = RestaurantMenuActivity.RetrieveItems(
            this@CartActivity
        ).execute().get()

        txtViewOrderingFrom.text= dbRestList.get(0).resName //set up restaurant name

        totalAmount = 0

        for(i in 0 until dbRestList.size)
        {
            totalAmount+=dbRestList.get(i).itemPrice.toInt()
        }

        var dbRestListId = mutableListOf<String>()

        for(i in 0 until dbRestList.size)
        {
            dbRestListId.add(dbRestList.get(i).itemId.toString())

        }


        //grab items________________________________________________________________________

        //we grabbed the list of items, now to display them on the screen:

        menuAdapter = CartRecyclerAdapter(
            this@CartActivity,//pass the relativelayout which has the button to enable it later
            dbRestList
        )//set the adapter with the data

        recyclerView.adapter = menuAdapter

        recyclerView.layoutManager = layoutManager

        //set up button:

        btnPlaceOrder.text="Place Order (Rs. "+ totalAmount+")"


        btnPlaceOrder.setOnClickListener {
            progressLayout.visibility = View.VISIBLE

            val sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
            if (ConnectionManager().checkConnectivity(this@CartActivity)) {


                try {

                    val foodJsonArray = JSONArray()

                    for (foodItem in dbRestListId) {
                        val singleItemObject = JSONObject()
                        singleItemObject.put("food_item_id", foodItem)
                        foodJsonArray.put(singleItemObject)
                    }

                    val sendOrder = JSONObject()

                    sendOrder.put("user_id", sharedPreferences.getString("userId", "0"))
                    sendOrder.put("restaurant_id", dbRestList.get(0).resId)
                    sendOrder.put("total_cost", totalAmount)
                    sendOrder.put("food", foodJsonArray)

                    val queue = Volley.newRequestQueue(this@CartActivity)

                    val url =  "http://13.235.250.119/v2/place_order/fetch_result/"
                    val jsonObjectRequest = @SuppressLint("ResourceType")
                    object : JsonObjectRequest(
                        Request.Method.POST,
                        url,
                        sendOrder,
                        Response.Listener {

                            val responseJsonObjectData = it.getJSONObject("data")

                            val success = responseJsonObjectData.getBoolean("success")

                            if (success) {



                                val intent= Intent(this@CartActivity,
                                    OrderPlacedActivity::class.java)

                                startActivity(intent)

                                finishAffinity()//destroy all previous activities


                            } else {
                                //display error message
                                Toast.makeText(
                                    this@CartActivity,
                                    "Some error occurred while parsing JSON file received",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        },
                        Response.ErrorListener
                        {
                            //display error message
                            Toast.makeText(
                                this@CartActivity,
                                "Volley error occurred",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    )
                    //body
                    {

                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-type"] = "application/json"
                            headers["token"] = "7b832cd6a75856"
                            return headers
                        }


                    }
                    queue.add(jsonObjectRequest)
                    //activity_cart_Progressdialog.visibility=View.INVISIBLE


                }
                catch (e: Exception) {
                    //display error message

                    Toast.makeText(
                        this@CartActivity,
                        "Some error occurred while parsing JSON file received 2",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            else{
                //there is no net
                val dialog = AlertDialog.Builder(this@CartActivity)
                dialog.setTitle("ERROR")
                dialog.setMessage("Internet Connection NOT Found")
                dialog.setPositiveButton("Open Settings"){text,listener->
                    //use an implicit intent: (open things that are in the phone but outside the APP)
                    val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }
                dialog.setNegativeButton("Exit"){text,listener->
                    //do nothing
                    val dialog2 = AlertDialog.Builder(this@CartActivity)
                    dialog2.setTitle("EXIT")
                    dialog2.setMessage("Are you sure you want to exit?")
                    dialog2.setPositiveButton("Yes"){text,listener->
                        ActivityCompat.finishAffinity(this@CartActivity)
                    }
                    dialog2.setNegativeButton("No"){text,listener->
                        //do nothing
                    }
                    dialog2.create()
                    dialog2.show()

                }
                dialog.create()
                dialog.show()
            }

        }

    }


    fun setToolBar(){
        setSupportActionBar(toolbar)
        supportActionBar?.title="My Cart"
        supportActionBar?.setHomeButtonEnabled(true)//enables the button on the tool bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)//displays the icon on the button
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id=item.itemId

        when(id){
            android.R.id.home->{
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    //_______________________________ITEM DB_________________________________________________
    class DBAsyncTask(val context: Context, val itemEntity: MenuItemEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>(){
        //mode 1: check DB if restaurant exists
        //mode 2: add the restaurant to DB
        //mode 3: remove restaurant from DB

        //initialize DB:
        val db = Room.databaseBuilder(context, MenuItemDatabase::class.java, "item-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when(mode){
                1->{
                    val book: MenuItemEntity? = db.menuItemDao().getItemById(itemEntity.itemId.toString())
                    db.close()
                    return book!=null

                }
                2->{
                    db.menuItemDao().insertRest(itemEntity)
                    db.close()
                    return true
                }
                3->{
                    db.menuItemDao().deleteRest(itemEntity)
                    db.close()
                    return true
                }
            }
            return false;
        }

    }
    //_______________________________ITEM DB_________________________________________________

    class RetrieveItems(val context: Context): AsyncTask<Void, Void, List<MenuItemEntity>>(){


        override fun doInBackground(vararg params: Void?): List<MenuItemEntity> {

            //initialize DB
            val db = Room.databaseBuilder(context, MenuItemDatabase::class.java, "item-db").build()
            return db.menuItemDao().getAllItems()
        }

    }


}
