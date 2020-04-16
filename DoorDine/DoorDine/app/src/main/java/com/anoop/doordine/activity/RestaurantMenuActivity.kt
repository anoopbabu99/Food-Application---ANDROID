package com.anoop.doordine.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.anoop.doordine.R
import com.anoop.doordine.adapter.RestaurantMenuRecyclerAdapter
import com.anoop.doordine.database.MenuItemDatabase
import com.anoop.doordine.database.MenuItemEntity
import com.anoop.doordine.util.ConnectionManager
import org.json.JSONException

class RestaurantMenuActivity : AppCompatActivity() {
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    lateinit var recyclerRestaurant: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: RestaurantMenuRecyclerAdapter
    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    //lateinit var proceedToCartLayout:RelativeLayout
    lateinit var parent: RelativeLayout

    lateinit var btnProceedToCart:Button


    lateinit var restaurantName: String

    var resId: String? = "-1"
    val menuList = arrayListOf<com.anoop.doordine.model.MenuItem>()

    override fun onCreate(savedInstanceState: Bundle?) {


        //delete any menu item previously selected______________________________________
        var dbRestList = listOf<MenuItemEntity>()
        dbRestList = RetrieveItems(
            this@RestaurantMenuActivity
        ).execute().get()
        for(i in 0 until dbRestList.size)
            DBAsyncTask(
                this@RestaurantMenuActivity,
                dbRestList.get(i),
                3
            ).execute().get()
        //delete any menu item previously selected______________________________________

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)

        //proceedToCartLayout=findViewById(R.id.relativeLayoutProceedToCart)
        parent = findViewById(R.id.parent)
        btnProceedToCart=findViewById(R.id.btnProceedToCart)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "REST NAME"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)



        recyclerRestaurant = findViewById(R.id.recyclerRestaurantMenu)
        layoutManager = LinearLayoutManager(this@RestaurantMenuActivity)
        progressLayout = findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        progressBar = findViewById(R.id.progressBar)

        if (intent != null) {
            resId = intent.getStringExtra("res_id")
            supportActionBar?.title = intent.getStringExtra("res_name")
            restaurantName = intent.getStringExtra("res_name")


        } else {
            finish()
            Toast.makeText(this@RestaurantMenuActivity, "Some error occurred", Toast.LENGTH_SHORT)
                .show()
        } //error in intent

        if (resId == "-1") {
            finish()
            Toast.makeText(this@RestaurantMenuActivity, "Some error occurred", Toast.LENGTH_SHORT)
                .show()
        } //no book was fetched


        //_________________________INTERNET___________________________________________________

        // Create a request queue (series of requests sent to the server via a JSON file to the API).
        // The API queries it out from the database and returns it to the APP (in the form  of another JSON file).

        val queue = Volley.newRequestQueue(this@RestaurantMenuActivity)
        val url = "http://13.235.250.119/v2/restaurants/fetch_result/$resId"

        if (ConnectionManager().checkConnectivity(this@RestaurantMenuActivity)) {
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
                                val bookObject = com.anoop.doordine.model.MenuItem(
                                    restJsonObject.getString("id"),
                                    restJsonObject.getString("name"),
                                    restJsonObject.getString("cost_for_one"),
                                    restJsonObject.getString("restaurant_id")

                                )
                                menuList.add(bookObject)

                                //connection to RestaurantMenuRecyclerAdapter class (i.e. send it to the adapter)
                                recyclerAdapter = RestaurantMenuRecyclerAdapter(
                                    this@RestaurantMenuActivity,
                                    menuList,
                                    restaurantName,
                                    //proceedToCartLayout,
                                    parent,
                                    btnProceedToCart
                                )
                                recyclerRestaurant.adapter = recyclerAdapter
                                recyclerRestaurant.layoutManager = layoutManager


                            }


                        } else {
                            //display error message
                            Toast.makeText(
                                this@RestaurantMenuActivity,
                                "Some error occurred while parsing JSON file received",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            this@RestaurantMenuActivity,
                            "Some unexpected error occurred while fetching response",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                Response.ErrorListener {
                    //here we will handle the errors
                    //if(activity!=null) //resolve crash.
                    Toast.makeText(
                        this@RestaurantMenuActivity,
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
            val dialog = AlertDialog.Builder(this@RestaurantMenuActivity)
            dialog.setTitle("ERROR")
            dialog.setMessage("Internet Connection NOT Found")
            dialog.setPositiveButton("Open Settings") { text, listener ->
                //use an implicit intent: (open things that are in the phone but outside the APP)
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Exit") { text, listener ->
                //do nothing
                val dialog2 = AlertDialog.Builder(this@RestaurantMenuActivity)
                dialog2.setTitle("EXIT")
                dialog2.setMessage("Are you sure you want to exit?")
                dialog2.setPositiveButton("Yes") { text, listener ->
                    ActivityCompat.finishAffinity(this@RestaurantMenuActivity)
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




    override fun onBackPressed() {


        if(recyclerAdapter.getCounter()>0) {


            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this@RestaurantMenuActivity)
            alterDialog.setTitle("Alert!")
            alterDialog.setMessage("Going back will remove everything from cart")
            alterDialog.setPositiveButton("Okay") { text, listener ->
                //first delete everything in the database:
                var dbRestList = listOf<MenuItemEntity>()
                dbRestList = RetrieveItems(
                    this@RestaurantMenuActivity
                ).execute().get()
                for(i in 0 until dbRestList.size)
                    DBAsyncTask(
                        this@RestaurantMenuActivity,
                        dbRestList.get(i),
                        3
                    ).execute().get()



                super.onBackPressed()
            }
            alterDialog.setNegativeButton("No") { text, listener ->

            }
            alterDialog.show()
        }else{
            super.onBackPressed()
        }

    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id=item.itemId

        when(id){
            android.R.id.home->{
                if(recyclerAdapter.getCounter()>0) {

                    val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this@RestaurantMenuActivity)
                    alterDialog.setTitle("Alert!")
                    alterDialog.setMessage("Going back will remove everything from cart")
                    alterDialog.setPositiveButton("Okay") { text, listener ->
                        //first delete everything in the database:
                        var dbRestList = listOf<MenuItemEntity>()
                        dbRestList = RetrieveItems(
                            this@RestaurantMenuActivity
                        ).execute().get()
                        for(i in 0 until dbRestList.size)
                            DBAsyncTask(
                                this@RestaurantMenuActivity,
                                dbRestList.get(i),
                                3
                            ).execute().get()
                        super.onBackPressed()
                    }
                    alterDialog.setNegativeButton("No") { text, listener ->

                    }
                    alterDialog.show()
                }else{
                    super.onBackPressed()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }






    class RetrieveItems(val context: Context): AsyncTask<Void, Void, List<MenuItemEntity>>(){


        override fun doInBackground(vararg params: Void?): List<MenuItemEntity> {

            //initialize DB
            val db = Room.databaseBuilder(context, MenuItemDatabase::class.java, "item-db").build()
            return db.menuItemDao().getAllItems()
        }

    }
}


