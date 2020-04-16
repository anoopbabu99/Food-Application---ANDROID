package com.anoop.doordine.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anoop.doordine.R
import com.anoop.doordine.database.MenuItemEntity
import com.anoop.doordine.model.OrderHistoryRestaurant


class OrderHistoryAdapter(val context: Context,
                          val orderedRestaurantList:ArrayList<OrderHistoryRestaurant>,
                          val myNestedArrayList: ArrayList<ArrayList<MenuItemEntity>>):
                RecyclerView.Adapter<OrderHistoryAdapter.ViewHolderOrderHistoryRestaurant>() {



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderOrderHistoryRestaurant {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_order_history_single_row, parent, false)

        return ViewHolderOrderHistoryRestaurant(view)
    }

    override fun getItemCount(): Int {
        return orderedRestaurantList.size
    }

    override fun onBindViewHolder(holder: ViewHolderOrderHistoryRestaurant, position: Int) {
        val restaurantObject = orderedRestaurantList[position]

        //here binding happens:  (nested binding)
        holder.txtRestaurantName.text = restaurantObject.restaurantName
        var formatDate=restaurantObject.orderPlacedAt
        formatDate=formatDate.replace("-","/")//21-02-20 to 21/02/20
        formatDate=formatDate.substring(0,6)+"20"+formatDate.substring(6,8)//21/02/20 to 21/02/2020
        holder.txtDate.text =  formatDate

        var layoutManager = LinearLayoutManager(context)
        var orderedItemsAdapter: CartRecyclerAdapter

        orderedItemsAdapter = CartRecyclerAdapter(context, myNestedArrayList.get(position))
        holder.recyclerViewItemsOrdered.adapter = orderedItemsAdapter
        holder.recyclerViewItemsOrdered.layoutManager = layoutManager

    }

        class ViewHolderOrderHistoryRestaurant(view: View) : RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val recyclerViewItemsOrdered: RecyclerView =
            view.findViewById(R.id.recyclerViewItemsOrdered)
    }

}