package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CodeItem
import kotlinx.coroutines.flow.Flow

@Dao
interface CodeDao {
    @Query("SELECT * FROM codes ORDER BY createdAt DESC")
    fun getAll(): Flow<List<CodeItem>>

    @Query("SELECT * FROM codes WHERE source = :source ORDER BY createdAt DESC")
    fun getBySource(source: String): Flow<List<CodeItem>>

    @Query("SELECT * FROM codes WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<CodeItem>>

    @Query("SELECT * FROM codes WHERE isSaved = 1 OR isFavorite = 1 OR source = 'GENERATED' ORDER BY createdAt DESC")
    fun getSavedLibrary(): Flow<List<CodeItem>>

    @Query("SELECT * FROM codes WHERE category = :category ORDER BY createdAt DESC")
    fun getByCategory(category: String): Flow<List<CodeItem>>

    @Query("""
        SELECT * FROM codes 
        WHERE title LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%' 
           OR format LIKE '%' || :query || '%'
           OR category LIKE '%' || :query || '%'
           OR (notes IS NOT NULL AND notes LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun search(query: String): Flow<List<CodeItem>>

    @Query("SELECT * FROM codes ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 10): Flow<List<CodeItem>>

    @Query("SELECT * FROM codes ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSync(limit: Int = 10): List<CodeItem>

    @Query("SELECT * FROM codes WHERE isFavorite = 1 ORDER BY createdAt DESC LIMIT :limit")
    fun getFavoritesSync(limit: Int = 10): List<CodeItem>

    @Query("SELECT * FROM codes WHERE isSaved = 1 OR isFavorite = 1 OR source = 'GENERATED' ORDER BY createdAt DESC LIMIT :limit")
    fun getSavedLibrarySync(limit: Int = 20): List<CodeItem>

    @Query("SELECT * FROM codes ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentList(limit: Int = 10): List<CodeItem>

    @Query("SELECT * FROM codes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CodeItem?

    @Query("SELECT * FROM codes WHERE id = :id LIMIT 1")
    fun getByIdSync(id: Long): CodeItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CodeItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CodeItem>)

    @Update
    suspend fun update(item: CodeItem)

    @Query("UPDATE codes SET notes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE codes SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE codes SET isSaved = :isSaved, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setSaved(id: Long, isSaved: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE codes SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun setFavoriteForIds(ids: List<Long>, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE codes SET category = :category, updatedAt = :updatedAt WHERE id IN (:ids)")
    suspend fun setCategoryForIds(ids: List<Long>, category: String, updatedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(item: CodeItem)

    @Query("DELETE FROM codes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM codes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM codes WHERE source != 'GENERATED' AND isSaved = 0 AND isFavorite = 0")
    suspend fun clearScannedHistory()

    @Query("DELETE FROM codes WHERE source = :source")
    suspend fun clearBySource(source: String)

    @Query("DELETE FROM codes")
    suspend fun clearAll()
}
