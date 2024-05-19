package com.chewatabets.coroutinesdemo.marsphotos.presentations.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.chewatabets.coroutinesdemo.marsphotos.domain.models.MarsPhotosModel

@Composable
fun PhotoCard(modifier: Modifier = Modifier, photo : MarsPhotosModel){
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        AsyncImage(model = photo.imgSrc, contentDescription = "Mars Images")
    }
}