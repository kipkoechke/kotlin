package com.example.architecturesample.store.data.repository

import arrow.core.Either
import com.example.architecturesample.store.data.mapper.toNetworkError
import com.example.architecturesample.store.data.remote.ProductsApi
import com.example.architecturesample.store.domain.model.NetworkError
import com.example.architecturesample.store.domain.model.Product
import com.example.architecturesample.store.domain.repository.ProductsRepository
import javax.inject.Inject

class ProductsRepositoryImpl   @Inject constructor(
    private val productsApi: ProductsApi
) : ProductsRepository {
    override suspend fun getProducts(): Either<NetworkError, List<Product>> {
        return Either.catch { productsApi.getProducts() }.mapLeft { it.toNetworkError() }
    }
}