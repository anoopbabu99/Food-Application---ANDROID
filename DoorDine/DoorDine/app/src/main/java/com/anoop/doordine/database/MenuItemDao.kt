package com.anoop.doordine.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MenuItemDao {
    @Insert
    fun insertRest(itemEntity: MenuItemEntity)

    @Delete
    fun deleteRest(itemEntity: MenuItemEntity)

    @Query("SELECT * FROM menu")
    fun getAllItems(): List<MenuItemEntity>

    //retrieve info about a book given an ID.
    @Query("SELECT * FROM menu where item_id = :itemId")
    fun getItemById(itemId: String): MenuItemEntity
}