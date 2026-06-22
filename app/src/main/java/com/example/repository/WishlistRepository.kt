package com.example.repository

import android.util.Log
import com.example.models.WishlistInsert
import com.example.models.WishlistItem
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.ktor.client.plugins.HttpTimeout

@OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)
object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://maugcaqkzbngspputogl.supabase.co",
        supabaseKey = "sb_publishable_IqSgJknUJRwtri9rKNvO2w_fMbkx-60"
    ) {
        install(Postgrest)
        httpConfig {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000L
                connectTimeoutMillis = 60_000L
                socketTimeoutMillis = 60_000L
            }
        }
    }
}

class WishlistRepository {

    suspend fun saveToWishlist(item: WishlistItem): Result<WishlistItem> {
        return try {
            val insertData = WishlistInsert(
                commonName = item.commonName,
                latinName = item.latinName,
                description = item.description,
                matchPercentage = item.matchPercentage,
                tags = item.tags,
                scanMode = item.scanMode
            )
            val insertedItem = SupabaseClient.client.from("wishlist").insert(insertData) {
                select()
            }.decodeSingle<WishlistItem>()
            Result.success(insertedItem)
        } catch (e: Exception) {
            Log.e("WishlistRepository", "Error saving to wishlist: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getWishlist(): Result<List<WishlistItem>> {
        return try {
            val items = SupabaseClient.client.from("wishlist")
                .select()
                .decodeList<WishlistItem>()
            Result.success(items)
        } catch (e: Exception) {
            Log.e("WishlistRepository", "Error loading wishlist: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteFromWishlist(id: Long): Result<Unit> {
        return try {
            SupabaseClient.client.from("wishlist")
                .delete {
                    filter {
                        eq("id", id)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WishlistRepository", "Error deleting from wishlist: ${e.message}", e)
            Result.failure(e)
        }
    }
}
