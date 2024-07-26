package com.bellon.statussaver.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                ),
                title = { Text("Status Saver") },
                actions = {
                    IconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "Settings",
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
            )
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Save Statuses in Folder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "/storage/emulated/0/Documents/Status Saver",
                    fontSize = 12.sp,
                    color = Color(0xFFBABABA),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Rate us",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "Please support our work by your rating.",
                    fontSize = 12.sp,
                    color = Color(0xFFBABABA),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

            }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "About",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Text(
                    text = "Version: 1.0.0",
                    fontSize = 12.sp,
                    color = Color(0xFFBABABA),
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

            }
        }
    }


}