package com.anoop.doordine.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface RestaurantDao {

    @Insert
    fun insertRest(resEntity: RestaurantEntity)

    @Delete
    fun deleteRest(resEntity: RestaurantEntity)

    @Query("SELECT * FROM restaurants where user_id= :userId")
    fun getAllRests(userId: String?): List<RestaurantEntity>

    //retrieve info about a book given an ID.
    @Query("SELECT * FROM restaurants where res_id= :resId and user_id= :userId")
    fun getRestById(resId: String, userId: String): RestaurantEntity

}