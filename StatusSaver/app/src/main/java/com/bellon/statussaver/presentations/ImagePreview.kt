package com.bellon.statussaver.presentations

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bellon.statussaver.R

@Composable
fun ImagePreview(modifier: Modifier = Modifier) {
    val items = listOf(
        RepostShareAndSaveItem(Icons.Default.Favorite, "Repost"),
        RepostShareAndSaveItem(Icons.Default.Share, "Share"),
        RepostShareAndSaveItem(
            Icons.Default.Done,
            "Save"
        )
    )

    Column {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back Arrow"
            )
            Text(text = "Status Saver 💯")
        }
        Image(
            painter = painterResource(id = R.drawable.whatsapp_image),
            contentDescription = "Image",
            modifier = modifier
                .fillMaxSize()
                .weight(1f)
        )
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = modifier.fillMaxWidth()) {
            items.forEachIndexed{index, item -> 
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = item.icon, contentDescription =null )
                    Text(text = item.title)
                }
            }
        }
    }
}

data class RepostShareAndSaveItem(
    val icon: ImageVector,
    val title: String
)