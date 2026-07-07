package com.example.data

import kotlinx.coroutines.flow.Flow

class MarketingRepository(private val dao: MarketingDao) {
    val allProducts: Flow<List<Product>> = dao.getAllProducts()

    suspend fun getProductById(id: Int): Product? = dao.getProductById(id)

    suspend fun insertProduct(product: Product): Long = dao.insertProduct(product)

    suspend fun updateProduct(product: Product) = dao.updateProduct(product)

    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)

    fun getMaterialsForProduct(productId: Int): Flow<List<MarketingMaterial>> =
        dao.getMaterialsForProduct(productId)

    suspend fun getMaterialForProductAndMedium(productId: Int, medium: String): MarketingMaterial? =
        dao.getMaterialForProductAndMedium(productId, medium)

    suspend fun insertMaterial(material: MarketingMaterial): Long = dao.insertMaterial(material)

    suspend fun deleteMaterialById(materialId: Int) = dao.deleteMaterialById(materialId)
}
