package com.anoop.doordine.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu")

data class MenuItemEntity (
    @PrimaryKey @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "res_id") val resId: Int,
    @ColumnInfo(name = "res_name") val resName: String,
    @ColumnInfo(name = "item_name") val itemName: String,
    @ColumnInfo(name = "item_price") val itemPrice: String
)