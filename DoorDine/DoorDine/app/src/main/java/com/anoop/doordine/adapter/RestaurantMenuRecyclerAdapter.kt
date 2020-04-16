package com.anoop.doordine.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.anoop.doordine.R
import com.anoop.doordine.activity.CartActivity
import com.anoop.doordine.database.MenuItemDatabase
import com.anoop.doordine.database.MenuItemEntity
import com.anoop.doordine.model.MenuItem

class RestaurantMenuRecyclerAdapter (val context: Context, val itemList: ArrayList<MenuItem>,
                                     val restaurantName: String, /*val proceedToCartPassed: RelativeLayout,*/
                                     var parent:RelativeLayout,
                                     val btnProceedToCart:Button):
    RecyclerView.Adapter<RestaurantMenuRecyclerAdapter.RestaurantMenuViewHolder>(){

    var count = 0
    //lateinit var proceedToCart:RelativeLayout


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            RestaurantMenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_restaurant_menu_single_row, parent, false)

        return RestaurantMenuRecyclerAdapter.RestaurantMenuViewHolder(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: RestaurantMenuRecyclerAdapter.RestaurantMenuViewHolder, position: Int) {
        val entry = itemList[position]
        holder.txtItemName.text = entry.name
        holder.txtPrice.text = "Rs. "+entry.costForOne
        holder.txtItemNo.text = (position+1).toString()

        //set up cart_______________________________________________________________
        val itemEntity = MenuItemEntity(
            entry.id?.toInt() as Int,
            entry.res_id?.toInt() as Int,
            restaurantName,
            entry.name,
            entry.costForOne
        )

        //proceedToCart = proceedToCartPassed
        btnProceedToCart.visibility=View.VISIBLE

        val checkFav = DBAsyncTask(context, itemEntity, 1).execute()
        val isFav = checkFav.get()

        if(isFav)
        {
            holder.btnAddToCart.text = "Remove from Cart"
            holder.btnAddToCart.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimaryDark))
        }
        else{
            holder.btnAddToCart.text = "Add to Cart"
            holder.btnAddToCart.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary))
        }

        holder.btnAddToCart.setOnClickListener {
            if (!DBAsyncTask(context, itemEntity, 1).execute().get()) {
                val result = DBAsyncTask(context, itemEntity, 2).execute().get()
                if(result)  {
                    holder.btnAddToCart.text = "Remove from Cart"
                    holder.btnAddToCart.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimaryDark))
                    count++
                }
            }
            else{
                val result = RestaurantMenuRecyclerAdapter.DBAsyncTask(context, itemEntity, 3).execute().get()
                if(result) {
                    holder.btnAddToCart.text = "Add to Cart"
                    holder.btnAddToCart.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary))
                    count--
                }
            }

            if(count>0){
                //proceedToCart.visibility=View.VISIBLE - read note (xyz) below
                btnProceedToCart.visibility=View.VISIBLE
                if(count == 1)
                    btnProceedToCart.text = "Proceed to Cart " + "("+count.toString()+" item)"
                else btnProceedToCart.text = "Proceed to Cart " + "("+count.toString()+" items)"
                btnProceedToCart.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary))

            }
            else{
                //proceedToCart.visibility=View.INVISIBLE - read note (xyz) below
                btnProceedToCart.visibility = View.VISIBLE
                btnProceedToCart.text = "Cart is Empty"
                btnProceedToCart.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimaryDark))
            }
        }
        //set up cart_______________________________________________________________

        btnProceedToCart.setOnClickListener(View.OnClickListener {
            if(count>0) {
                val intent = Intent(context, CartActivity::class.java)
                context.startActivity(intent)
            }
            else{
                //xyz : IN THE CASE WHERE THE CART IS EMPTY:
                // It is not functioning as a button (it could well be replaced by text view).
                // It is not functioning as a button in this case as there is no corresponding action being performed when it is pressed.
                // I know this doesn't follow the design specifications but I felt notifying the cart was empty looked nice (UI perspective).
                // At first I made the button appear as and when an item was pressed and disappear if there were no items.
            }
        })

    }

    //_________________________INITIALIZE____________________________________
    class RestaurantMenuViewHolder(view: View): RecyclerView.ViewHolder(view){
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        val txtItemNo: TextView = view.findViewById(R.id.txtItemNo)
        val btnAddToCart: Button = view.findViewById(R.id.btnAddToCart)
    }
    //_________________________INITIALIZE____________________________________

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

    fun getCounter():Int{
        return count
    }

}