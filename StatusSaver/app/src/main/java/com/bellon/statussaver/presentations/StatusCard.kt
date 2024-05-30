package com.bellon.statussaver.presentations

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bellon.statussaver.R

@Composable
fun StatusCard(modifier: Modifier = Modifier, onNavigateToStatusDetail: () -> Unit) {
    Card(
        modifier = modifier.clickable { onNavigateToStatusDetail() },
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box() {
            Image(
                painter = painterResource(id = R.drawable.whatsapp_image),
                contentDescription = "",
                modifier = modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.FillBounds
            )
            Box(
                modifier = Modifier
                    .align(Alignment.Center).padding(8.dp)
                    .size(36.dp) // adjust the size as needed
                    .background(Color.Transparent, CircleShape)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { /*TODO: Add functionality to play video*/ }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_play),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp).align(Alignment.Center)
                )
            }
            Box(
                modifier = Modifier.align(Alignment.BottomEnd)
                    .clickable {/*TODO*/ }
                    .size(32.dp)
                    .padding(bottom = 4.dp, end = 4.dp)
                    .background(
                        color = colorResource(id = R.color.colorPrimaryLight),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = colorResource(id = R.color.colorPrimaryLight),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_download),
                    contentDescription = "Download icon",
                    tint = colorResource(id = R.color.white),
                    modifier = Modifier.size(20.dp).align(Alignment.Center)

                )
            }
        }
    }
}
