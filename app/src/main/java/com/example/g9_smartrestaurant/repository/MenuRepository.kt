package com.example.g9_smartrestaurant.repository

import com.example.g9_smartrestaurant.model.Category
import com.example.g9_smartrestaurant.model.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MenuRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val menuCollection = firestore.collection("menu_items")
    private val categoryCollection = firestore.collection("categories")

    // --- PHẦN CHO CUSTOMER: LẤY DỮ LIỆU ---
    suspend fun getCategories(): Result<List<Category>> = runCatching {
        val snapshot = categoryCollection.orderBy("displayOrder").get().await()
        snapshot.toObjects(Category::class.java)
    }

    suspend fun getMenuItems(categoryId: String? = null): Result<List<MenuItem>> = runCatching {
        val query = if (categoryId.isNullOrBlank() || categoryId == "all") {
            menuCollection
        } else {
            menuCollection.whereEqualTo("categoryId", categoryId)
        }
        val snapshot = query.get().await()
        snapshot.documents.mapNotNull { doc ->
            doc.toObject(MenuItem::class.java)?.copy(id = doc.id)
        }
    }

    // --- PHẦN CHO MANAGER: CRUD MENU & DANH MỤC ---
    suspend fun addMenuItem(item: MenuItem): Result<String> = runCatching {
        val docRef = menuCollection.document()
        val itemWithId = item.copy(id = docRef.id)
        docRef.set(itemWithId).await()
        docRef.id
    }

    suspend fun updateMenuItem(item: MenuItem): Result<Unit> = runCatching {
        menuCollection.document(item.id).set(item).await()
    }

    suspend fun deleteMenuItem(itemId: String): Result<Unit> = runCatching {
        menuCollection.document(itemId).delete().await()
    }

    suspend fun toggleItemAvailability(itemId: String, isAvailable: Boolean): Result<Unit> = runCatching {
        menuCollection.document(itemId).update("isAvailable", isAvailable).await()
    }
}