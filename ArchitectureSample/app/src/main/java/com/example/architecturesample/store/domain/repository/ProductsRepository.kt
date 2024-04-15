package com.example.architecturesample.store.domain.repository

import arrow.core.Either
import com.example.architecturesample.store.domain.model.NetworkError
import com.example.architecturesample.store.domain.model.Product

interface ProductsRepository {

    suspend fun getProducts(): Either<NetworkError, List<Product>>

}