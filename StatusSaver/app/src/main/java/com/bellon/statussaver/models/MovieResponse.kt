package com.bellon.statussaver.models

import com.google.gson.annotations.SerializedName

data class MovieResponse (

    @SerializedName("result") var result : ArrayList<MovieModel>

)