package com.anoop.doordine.database

import androidx.room.ColumnInfo
import androidx.room.Entity


//There are two primary keys because when two different users log in on the same device then their favorite preferences
//will be unique to their account.
//In the demo this feature is NOT present. eg. if userOne likes a restaurant this is reflected on another users account. (which should not happen)

@Entity(tableName = "restaurants", primaryKeys = arrayOf<String>("res_id","user_id"))


data class RestaurantEntity(
    @ColumnInfo(name = "res_id") val resId: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "res_name") val resName: String,
    @ColumnInfo(name = "res_price") val resPrice: String,
    @ColumnInfo(name = "res_rating") val resRating: String,
    @ColumnInfo(name = "res_image") val resImg: String
)