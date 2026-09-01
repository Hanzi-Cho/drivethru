package com.hanzi.drivethru.data.menu.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MenuItemEntity::class], version = 1, exportSchema = false)
abstract class DriveThruDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao

    companion object {
        @Volatile
        private var instance: DriveThruDatabase? = null

        fun getInstance(context: Context): DriveThruDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DriveThruDatabase::class.java,
                    "drivethru.db",
                ).build().also { instance = it }
            }
        }
    }
}
