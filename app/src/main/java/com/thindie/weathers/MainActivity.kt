package com.thindie.weathers

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.thindie.weathers.feature.main.AppFlow
import com.thindie.weathers.feature.weather.WeatherFlow
import com.thindie.weathers.navigation.BackPress
import com.thindie.weathers.navigation.BackStack
import com.thindie.weathers.navigation.Router
import com.thindie.weathers.ui.theme.Colors
import com.thindie.weathers.ui.theme.Shapes
import com.thindie.weathers.ui.theme.WeathersTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val backStack = (application as WeatherApplication).backStack
        val router = Router(backStack)
        configureAppStart(router, backStack)
        setContent {
            WeathersTheme {
                BackHandler { router.sendEvent(BackPress) }
                val screen = router.currentScreen.collectAsState(null)
                val showBottomBar = router.currentFlow.map { it is AppFlow }.collectAsState(true)
                Box(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = screen.value) { currentScreen ->
                        currentScreen?.Content()
                    }
                    if (screen.value == null) {
                        FullScreenProgressIndicator()
                    }
                    AnimatedVisibility(
                        visible = showBottomBar.value,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            Text("Weather", modifier = Modifier.clickable(onClick = { WeatherFlow(router) }))
                            Text("Stob", modifier = Modifier.clickable(onClick = { }))
                        }
                    }
                }

                LaunchedEffect(router) {
                    router
                        .currentScreen
                        .onEach {
                            if (it == null) {
                                finish()
                            }
                        }
                        .launchIn(this)
                }
            }
        }
    }

    fun configureAppStart(
        router: Router,
        backStack: BackStack,
    ) {
        if (backStack.empty()) {
            AppFlow(router)
        }
    }
}

@Composable
fun FullScreenProgressIndicator() {
    val colors =
        listOf(
            Color.Transparent,
            Colors.outline.copy(alpha = 0.2f),
            Color.Transparent,
        )

    val transition = rememberInfiniteTransition()
    val offset =
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(2500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
        )

    val brush =
        Brush.linearGradient(
            colors = colors,
            start = Offset(-(1000 + offset.value * 2000), -(1000 + offset.value * 2000)),
            end = Offset(1000 + offset.value * 2000, 1000 + offset.value * 2000),
        )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawRect(brush, size = size)
                    drawContent()
                }
                .clickable(enabled = false, onClick = {}, indication = null, interactionSource = null),
    ) {
        CircularProgressIndicator(
            modifier =
                Modifier
                    .align(Center)
                    .background(Colors.background, shape = Shapes.medium)
                    .padding(24.dp),
            strokeWidth = 3.dp,
            strokeCap = StrokeCap.Round,
            color = Colors.onBackground,
        )
    }
}
