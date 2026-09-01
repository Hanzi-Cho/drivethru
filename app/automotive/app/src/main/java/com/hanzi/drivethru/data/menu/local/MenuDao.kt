package com.hanzi.drivethru.data.menu.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MenuDao {
    /**
     * Live view of a store's cached menu. Emits immediately with whatever rows already exist
     * (instant local-first read), then again whenever [replaceStoreMenu] commits a server refresh.
     */
    @Query("SELECT * FROM menu_items WHERE storeId = :storeId ORDER BY category, name")
    abstract fun observeMenuItems(storeId: String): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(items: List<MenuItemEntity>)

    @Query("DELETE FROM menu_items WHERE storeId = :storeId")
    abstract suspend fun deleteForStore(storeId: String)

    @Transaction
    open suspend fun replaceStoreMenu(storeId: String, items: List<MenuItemEntity>) {
        deleteForStore(storeId)
        insertAll(items)
    }
}
