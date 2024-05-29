@file:OptIn(ExperimentalFoundationApi::class)

package com.bellon.statussaver.presentations.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bellon.statussaver.models.TabItem

@Composable
fun TabScreen(
    modifier: Modifier = Modifier,
    tabItems: List<TabItem>,
    content: @Composable (Int) -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val pagerState = rememberPagerState {
        tabItems.size
    }

    LaunchedEffect(selectedTabIndex) {
        if (!pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        selectedTabIndex = pagerState.currentPage
    }

    Column(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp)) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabItems.forEachIndexed { index, item ->
                Tab(text = { Text(text = item.title) },
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    icon = {
                        Icon(
                            imageVector = if (selectedTabIndex == index) {
                                item.selectedIcon
                            } else {
                                item.unselectedIcon
                            }, contentDescription = item.title
                        )
                    }
                )
            }
        }
        HorizontalPager(
            state = pagerState, modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            content(page)
        }
    }
}