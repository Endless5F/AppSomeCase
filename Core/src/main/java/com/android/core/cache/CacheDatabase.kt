package com.android.core.cache

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.android.core.util.AppGlobals

/**
 *
 * @author jiaochengyun
 * @version
 * @since 2021/10/17
 */
@Database(entities = [Cache::class], version = 1)
abstract class CacheDatabase: RoomDatabase() {

    companion object {
        private var database: CacheDatabase

        init {
            val context = AppGlobals.get()!!.applicationContext
            database = Room.databaseBuilder(context, CacheDatabase::class.java, "cache_database").build()
        }

        fun get(): CacheDatabase {
            return database
        }
    }

    abstract val cacheDao: CacheDao
}