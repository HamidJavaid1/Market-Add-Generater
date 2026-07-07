package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketingDao {
    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId")
    suspend fun getProductById(productId: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("SELECT * FROM marketing_materials WHERE productId = :productId ORDER BY createdAt DESC")
    fun getMaterialsForProduct(productId: Int): Flow<List<MarketingMaterial>>

    @Query("SELECT * FROM marketing_materials WHERE productId = :productId AND medium = :medium LIMIT 1")
    suspend fun getMaterialForProductAndMedium(productId: Int, medium: String): MarketingMaterial?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MarketingMaterial): Long

    @Query("DELETE FROM marketing_materials WHERE id = :materialId")
    suspend fun deleteMaterialById(materialId: Int)
}
