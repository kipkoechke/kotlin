package com.example.architecturesample.store.data.remote

import com.example.architecturesample.store.domain.model.Product
import retrofit2.http.GET

interface ProductsApi {
    @GET("products")
    suspend fun getProducts(): List<Product>
}