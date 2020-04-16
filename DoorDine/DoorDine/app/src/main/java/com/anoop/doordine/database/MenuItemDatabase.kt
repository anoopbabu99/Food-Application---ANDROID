package com.anoop.doordine.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MenuItemEntity::class],version = 1)
abstract class MenuItemDatabase: RoomDatabase(){

    abstract fun menuItemDao(): MenuItemDao

}