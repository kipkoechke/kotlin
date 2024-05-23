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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bellon.statussaver.R

@Composable
fun StatusCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = painterResource(id = R.drawable.whatsapp_image),
                contentDescription = "",
                modifier = modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.FillBounds
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
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
                    modifier = Modifier.size(20.dp)

                )
            }
        }
    }
}
