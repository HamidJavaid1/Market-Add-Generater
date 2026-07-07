package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "marketing_materials",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"])]
)
data class MarketingMaterial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val medium: String,
    val headline: String,
    val bodyText: String,
    val base64Image: String?,
    val promptUsed: String,
    val createdAt: Long = System.currentTimeMillis()
)
