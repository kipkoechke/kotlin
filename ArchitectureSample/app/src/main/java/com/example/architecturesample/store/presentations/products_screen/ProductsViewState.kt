package com.example.architecturesample.store.presentations.products_screen

import com.example.architecturesample.store.domain.model.Product

data class ProductsViewState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val error: String? = null
)
