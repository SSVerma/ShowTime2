package com.ssverma.shared.data.local.db.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.ssverma.shared.data.local.db.entity.CustomListEntity
import com.ssverma.shared.data.local.db.entity.CustomListItemEntity
import kotlinx.coroutines.flow.Flow

data class CustomListWithItems(
    @Embedded val list: CustomListEntity,
    @Relation(
        parentColumn = "listId",
        entityColumn = "listId"
    )
    val items: List<CustomListItemEntity>
)

@Dao
interface CustomListDao {

    @Transaction
    @Query("SELECT * FROM custom_lists ORDER BY updatedAt DESC")
    fun getAllListsWithItemsFlow(): Flow<List<CustomListWithItems>>

    @Transaction
    @Query("SELECT * FROM custom_lists WHERE listId = :listId")
    fun getListWithItemsFlow(listId: String): Flow<CustomListWithItems?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: CustomListEntity)

    @Query("UPDATE custom_lists SET title = :title, description = :description, updatedAt = :updatedAt WHERE listId = :listId")
    suspend fun updateList(
        listId: String,
        title: String,
        description: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE custom_lists SET isPublic = :isPublic, updatedAt = :updatedAt WHERE listId = :listId")
    suspend fun updatePublicStatus(
        listId: String,
        isPublic: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM custom_lists WHERE listId = :listId")
    suspend fun deleteListById(listId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListItem(item: CustomListItemEntity)

    @Query("DELETE FROM custom_list_items WHERE listId = :listId AND mediaId = :mediaId")
    suspend fun deleteListItem(listId: String, mediaId: Int)

    @Query("SELECT listId FROM custom_list_items WHERE mediaId = :mediaId")
    fun getListIdsForMediaFlow(mediaId: Int): Flow<List<String>>

    @Query("SELECT listId FROM custom_list_items WHERE mediaId = :mediaId")
    suspend fun getListIdsForMedia(mediaId: Int): List<String>

    @Query("SELECT * FROM custom_lists")
    suspend fun getAllLists(): List<CustomListEntity>

    @Query("SELECT * FROM custom_list_items")
    suspend fun getAllListItems(): List<CustomListItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLists(lists: List<CustomListEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllListItems(items: List<CustomListItemEntity>)

    @Query("DELETE FROM custom_lists")
    suspend fun clearAllLists()

    @Query("DELETE FROM custom_list_items")
    suspend fun clearAllListItems()
}
