package com.bellon.statussaver.presentations

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bellon.statussaver.R

@Composable
fun ImagePreview(modifier: Modifier = Modifier) {

    Column {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back Arrow"
            )
            Text(text = "ImagePreview")
        }
        Image(
            painter = painterResource(id = R.drawable.whatsapp_image),
            contentDescription = "Image",
            modifier = modifier
                .fillMaxSize()
                .weight(1f)
        )
        Row(horizontalArrangement = Arrangement.SpaceAround, modifier = modifier.fillMaxWidth()) {
            Text(text = "Share")
            Text(text = "Save")
            Text(text = "Delete")
        }
    }
}

