package com.chewatabets.coroutinesdemo.marsphotos.domain.models

import com.google.gson.annotations.SerializedName


data class MarsPhotosModel(
    @SerializedName("id")
    val id: String,
    @SerializedName("img_src")
    val imgSrc: String
)