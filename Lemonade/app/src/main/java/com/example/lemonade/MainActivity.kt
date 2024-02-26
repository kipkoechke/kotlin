package com.example.lemonade

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lemonade.ui.theme.LemonadeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LemonadeApp()
        }
    }
}

@Composable
fun LemonadeWithImageAndText(

) {
    var currentStep by remember {
        mutableStateOf(1)
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentStep) {
            1 -> LemonadeTextAndImage(
                drawableResourceId = R.drawable.lemon_tree,
                textLabelResourceId = R.string.tap_lemon_tree,
                contentDescriptionResourceId = R.string.lemon_tree,
                onImageClick = {
                    currentStep = 2

                })


            2 ->   LemonadeTextAndImage(
                drawableResourceId = R.drawable.lemon_squeeze,
                textLabelResourceId = R.string.keep_tapping_to_squeeze,
                contentDescriptionResourceId = R.string.lemon,
                onImageClick = {
                    currentStep = 3

                })

            3 ->   LemonadeTextAndImage(
                drawableResourceId = R.drawable.lemon_drink,
                textLabelResourceId = R.string.tap_to_drink,
                contentDescriptionResourceId = R.string.glass_of_lemonade,
                onImageClick = {
                    currentStep = 4

                })

            4 -> LemonadeTextAndImage(
                drawableResourceId = R.drawable.lemon_restart,
                textLabelResourceId = R.string.tap_empty_glass,
                contentDescriptionResourceId = R.string.empty_glass,
                onImageClick = {
                    currentStep = 1

                })
        }
    }

}

@Composable
fun LemonadeTextAndImage(
    textLabelResourceId: Int,
    drawableResourceId: Int,
    contentDescriptionResourceId: Int,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {

        Column(
            modifier = Modifier
                .wrapContentSize()
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onImageClick,
                shape = RoundedCornerShape(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Image(
                    // modifier = Modifier.clickable { step },
                    painter = painterResource(drawableResourceId),
                    contentDescription = stringResource(
                        contentDescriptionResourceId
                    )
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = stringResource(textLabelResourceId), fontSize = 18.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LemonadeApp() {
    LemonadeWithImageAndText()
}