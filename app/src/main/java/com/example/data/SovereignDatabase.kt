package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Product::class, CartItem::class, Order::class, Setting::class],
    version = 1,
    exportSchema = false
)
abstract class SovereignDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
    abstract fun orderDao(): OrderDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: SovereignDatabase? = null

        fun getDatabase(context: Context): SovereignDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SovereignDatabase::class.java,
                    "sovereign_store_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
