@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.callcenter.smartclass.ui.home

import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assistant
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.YoutubeSearchedFor
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.callcenter.smartclass.R
import com.callcenter.smartclass.ui.LocalNavAnimatedVisibilityScope
import com.callcenter.smartclass.ui.LocalSharedTransitionScope
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.components.smartclassDivider
import com.callcenter.smartclass.ui.navigation.MainDestinations
import com.callcenter.smartclass.ui.options.AIInteraction
import com.callcenter.smartclass.ui.theme.AlphaNearOpaque
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.Ocean8
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationBar(
    modifier: Modifier = Modifier,
    navigateTo: (String) -> Unit
) {
    val sharedElementScope = LocalSharedTransitionScope.current ?: throw IllegalStateException("No shared element scope")
    val navAnimatedScope = LocalNavAnimatedVisibilityScope.current ?: throw IllegalStateException("No nav scope")

    var showMoreContent by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (showMoreContent) -90f else 0f, label = ""
    )

    val currentTime = remember { mutableStateOf(getCurrentTime()) }
    val pingTime = remember { mutableStateOf("... ms") }
    val showAIInteraction = remember { mutableStateOf(false) }
    val signalColor = remember { mutableStateOf(Color.Green) }

    val handleClose = {
        showAIInteraction.value = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime.value = getCurrentTime()
            delay(1000)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val isConnected = checkInternetConnectivity()
            signalColor.value = if (isConnected) Color.Green else Color.Red
            pingTime.value = "${checkPing()} ms"
            delay(5000)
        }
    }

    with(sharedElementScope) {
        with(navAnimatedScope) {
            Column(
                modifier = modifier
                    .renderInSharedTransitionScopeOverlay()
                    .animateEnterExit(
                        enter = slideInVertically { -it * 2 },
                        exit = slideOutVertically { -it * 2 }
                    )
            ) {
                TopAppBar(
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SignalCellularAlt,
                                contentDescription = "Signal Status",
                                tint = signalColor.value,
                                modifier = Modifier.size(24.dp)
                            )

                            Text(
                                text = pingTime.value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = smartclassTheme.colors.textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 8.dp)
                            )

                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentTime.value,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = smartclassTheme.colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(onClick = {
                                showMoreContent = !showMoreContent
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.ExpandMore,
                                    tint = Ocean8,
                                    contentDescription = stringResource(R.string.ExpandMore),
                                    modifier = Modifier.rotate(rotationAngle)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors().copy(
                        containerColor = smartclassTheme.colors.uiBackground.copy(alpha = AlphaNearOpaque),
                        titleContentColor = smartclassTheme.colors.textSecondary
                    )
                )
                smartclassDivider()

                // Konten tambahan yang akan muncul saat tombol ditekan
                AnimatedVisibility(
                    visible = showMoreContent,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeOut(animationSpec = tween(durationMillis = 300))
                ) {
                    DeliveryOptionsPanel(
                        onDismiss = { showMoreContent = false },
                        onOptionSelected = { option ->
                            when (option) {
                                1 -> navigateTo(MainDestinations.AI_INTERACTION_ROUTE)
                                2 -> navigateTo(MainDestinations.YOUTUBE_HEALTH_ROUTE)
                                // Tambahkan opsi lain jika perlu
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAIInteraction.value) {
        AnimatedVisibility(
            visible = showAIInteraction.value,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AIInteraction(onClose = handleClose)
        }
    }
}

suspend fun checkInternetConnectivity(): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("1.1.1.1", 53), 1500)
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}

suspend fun checkPing(): Long {
    return withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("1.1.1.1", 53), 1500)
                val endTime = System.currentTimeMillis()
                endTime - startTime
            }
        } catch (_: Exception) {
            999
        }
    }
}

fun getCurrentTime(): String {
    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date())
}

@Composable
fun DeliveryOptionsPanel(
    onDismiss: () -> Unit,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    smartclassCard(
        modifier = modifier
            .wrapContentWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xff00a1c7)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "~ Select Option ~",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xfff2ffff),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            listOf(
                Icons.Filled.Assistant to "Generative AI" to 1,
                Icons.Filled.YoutubeSearchedFor to "YouTube Health" to 2
            ).forEach { (pair, option) ->
                val (icon, text) = pair
                TextButton(
                    onClick = {
                        onOptionSelected(option)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFFFF))
            ) {
                Text("Close", color = Color(0xff00a1c7))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Preview("default")
@Preview("dark theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview("large font", fontScale = 2f)
@Composable
fun PreviewDestinationBar() {
    smartclassTheme {
        DestinationBar(
            modifier = Modifier.fillMaxWidth(),
            navigateTo = {}
        )
    }
}
