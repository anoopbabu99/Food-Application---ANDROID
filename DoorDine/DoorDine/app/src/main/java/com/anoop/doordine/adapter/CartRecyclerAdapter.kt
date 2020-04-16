package com.anoop.doordine.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anoop.doordine.R
import com.anoop.doordine.database.MenuItemEntity

class CartRecyclerAdapter(val context: Context, val cartItems: List<MenuItemEntity>):
                RecyclerView.Adapter<CartRecyclerAdapter.ViewHolderCart>() {


    class ViewHolderCart(view: View): RecyclerView.ViewHolder(view){
        val txtOrderItem: TextView =view.findViewById(R.id.txtOrderItem)
        val txtOrderItemPrice: TextView =view.findViewById(R.id.txtOrderItemPrice)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCart {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.recycler_cart_single_row,parent,false)

        return ViewHolderCart(view)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    override fun onBindViewHolder(holder: ViewHolderCart, position: Int) {
        val cartItemObject=cartItems[position]


        holder.txtOrderItem.text=cartItemObject.itemName
        holder.txtOrderItemPrice.text="Rs. "+cartItemObject.itemPrice
    }


}