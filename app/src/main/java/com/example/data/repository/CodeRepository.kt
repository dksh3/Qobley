package com.example.data.repository

import com.example.data.db.CodeDao
import com.example.data.model.CodeItem
import kotlinx.coroutines.flow.Flow

class CodeRepository(private val dao: CodeDao) {

    fun getAllCodes(): Flow<List<CodeItem>> = dao.getAll()

    fun getScannedHistory(): Flow<List<CodeItem>> = dao.getBySource("SCANNED")

    fun getSavedLibrary(): Flow<List<CodeItem>> = dao.getSavedLibrary()

    fun getFavorites(): Flow<List<CodeItem>> = dao.getFavorites()

    fun getByCategory(category: String): Flow<List<CodeItem>> = dao.getByCategory(category)

    fun getRecent(limit: Int = 10): Flow<List<CodeItem>> = dao.getRecent(limit)

    fun search(query: String): Flow<List<CodeItem>> = dao.search(query)

    suspend fun getById(id: Long): CodeItem? = dao.getById(id)

    suspend fun saveCode(item: CodeItem): Long = dao.insert(item)

    suspend fun saveBatch(items: List<CodeItem>) = dao.insertAll(items)

    suspend fun updateCode(item: CodeItem) = dao.update(item)

    suspend fun updateNotes(id: Long, notes: String?) = dao.updateNotes(id, notes)

    suspend fun toggleFavorite(item: CodeItem) {
        val newFav = !item.isFavorite
        dao.setFavorite(item.id, newFav)
    }

    suspend fun toggleSaved(item: CodeItem) {
        val newSaved = !item.isSaved
        dao.setSaved(item.id, newSaved)
    }

    suspend fun deleteCode(item: CodeItem) = dao.delete(item)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun deleteBatch(ids: List<Long>) = dao.deleteByIds(ids)

    suspend fun setFavoriteBatch(ids: List<Long>, favorite: Boolean) = dao.setFavoriteForIds(ids, favorite)

    suspend fun setCategoryBatch(ids: List<Long>, category: String) = dao.setCategoryForIds(ids, category)

    suspend fun clearHistory() = dao.clearScannedHistory()

    suspend fun clearAll() = dao.clearAll()
}
