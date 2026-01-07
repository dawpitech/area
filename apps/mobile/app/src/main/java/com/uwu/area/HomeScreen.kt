package com.uwu.area

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = if (email != null) "Hello $email, you are now logged in." else "Hello, you are now logged in.",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Connect your accounts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Link your favorite platforms to unlock more features.",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (loading) return@Button
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
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                        enabled = !loading
                    ) {
                        Text(text = if (loading) "Connecting..." else "Connect GitHub", color = Color.White)
                    }

                    Button(onClick = { error = "Discord connection not yet implemented" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5865F2))) {
                        Text(text = "Connect Discord", color = Color.White)
                    }
                    Button(onClick = { error = "Steam connection not yet implemented" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF171A21))) {
                        Text(text = "Connect Steam", color = Color.White)
                    }
                    Button(onClick = { error = "Instagram connection not yet implemented" }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD2A7B))) {
                        Text(text = "Connect Instagram", color = Color.White)
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
