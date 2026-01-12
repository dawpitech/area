package com.uwu.area

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.core.net.toUri

@Composable
fun HomeScreen(token: String, email: String?, onSignOut: () -> Unit) {
    val context = LocalContext.current
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (email != null) "Hello $email, you are now logged in." else "Hello, you are now logged in.",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        com.uwu.area.ui.components.SimpleCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Connect your accounts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Link your favorite platforms to unlock more features.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                val services = listOf(
                    "GitHub" to {
                        if (loading) return@to
                        loading = true
                        error = null
                        scope.launch {
                            val redirectUri = "area://home"
                            val res = fetchGithubInit(token, redirectUri)
                            loading = false
                            res.fold(
                                onSuccess = { url ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        error = "Cannot open URL: ${e.message}"
                                    }
                                },
                                onFailure = { e ->
                                    error = e.message ?: "Failed to connect GitHub"
                                }
                            )
                        }
                    },
                    "Discord" to { error = "Discord connection not yet implemented" },
                    "Steam" to { error = "Steam connection not yet implemented" },
                    "Instagram" to { error = "Instagram connection not yet implemented" }
                )

                services.forEach { (name, onClick) ->
                    Spacer(modifier = Modifier.height(8.dp))
                    com.uwu.area.ui.components.SimpleButton(
                        onClick = onClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !loading || name != "GitHub"
                    ) {
                        Text(text = if (loading && name == "GitHub") "Connecting..." else "Connect $name")
                    }
                }
            }
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ErrorPopup(message = error!!, onDismiss = { error = null })
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
