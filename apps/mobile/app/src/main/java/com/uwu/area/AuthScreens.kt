package com.uwu.area

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Close
import com.uwu.area.ui.theme.Black
import com.uwu.area.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import androidx.core.net.toUri
import java.net.URLEncoder
import kotlin.apply
import kotlin.io.inputStream

private const val ERROR_DISPLAY_MAX = 200

private fun extractErrorMessageFromJson(jo: JSONObject): String {
    try {
        if (jo.has("error")) {
            val err = jo.get("error")
            return when (err) {
                is String -> err
                is JSONObject -> {
                    val msg = err.optString("message", err.optString("error", err.optString("detail", "")))
                    if (msg.isNotBlank()) msg else "Unknown error"
                }
                is JSONArray -> {
                    val parts = mutableListOf<String>()
                    for (i in 0 until err.length()) {
                        try {
                            val el = err.get(i)
                            parts.add(el?.toString() ?: "")
                        } catch (_: Exception) { }
                    }
                    val joined = parts.joinToString("; ")
                    if (joined.isNotBlank()) joined else "Unknown error"
                }
                else -> err.toString().takeIf { it.isNotBlank() } ?: "Unknown error"
            }
        }
    } catch (_: JSONException) {
    }
    try {
        if (jo.has("message")) {
            val m = jo.optString("message", "")
            if (m.isNotBlank()) return m
        }
        if (jo.has("status")) {
            val s = jo.optString("status", "")
            if (s.isNotBlank() && !s.equals("ok", ignoreCase = true)) return s
        }
    } catch (_: JSONException) {
    }
    return "Unknown error"
}

suspend fun postJson(path: String, jsonBody: String): Result<String> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + path)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            conn.outputStream.use { os: OutputStream ->
                os.write(jsonBody.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }

            Log.d("Auth", "POST $path -> code=$code resp=$respText")

            var jo: JSONObject? = null
            try {
                jo = JSONObject(respText)
            } catch (_: Exception) {
            }

            if (jo != null && jo.has("error")) {
                val err = extractErrorMessageFromJson(jo)
                Log.d("Auth", "POST $path extracted error=$err")
                return@withContext Result.failure(Exception(err))
            }

            if (code in 200..299) {
                if (jo != null && jo.has("token")) {
                    val token = jo.optString("token", "")
                    Log.d("Auth", "POST $path extracted token=$token")
                    return@withContext Result.success(token)
                }
                if (jo != null) {
                    val status = jo.optString("status", jo.optString("message", ""))
                    if (status.equals("ok", ignoreCase = true)) {
                        Log.d("Auth", "POST $path status=ok")
                        return@withContext Result.success("ok")
                    }
                }
                return@withContext Result.success(respText)
            } else {
                var errMessage = respText
                if (jo != null) {
                    errMessage = extractErrorMessageFromJson(jo)
                    Log.d("Auth", "POST $path non-2xx extracted error=$errMessage")
                }
                return@withContext Result.failure(Exception(errMessage))
            }
        } catch (e: Exception) {
            Log.e("Auth", "network error", e)
            Result.failure(e)
        }
    }
}

suspend fun signup(email: String, password: String): Result<String> {
    val json = "{\"email\":\"$email\",\"password\":\"$password\"}"
    return postJson(ApiRoutes.SIGNUP, json)
}

suspend fun signin(email: String, password: String): Result<String> {
    val json = "{\"email\":\"$email\",\"password\":\"$password\"}"
    return postJson(ApiRoutes.SIGNIN, json)
}


@Composable
fun AuthHost(onAuthenticated: (token: String, email: String) -> Unit) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { TokenStore.init(context) }
    val tokenStore = TokenStore

    val prefs = remember {
        context.getSharedPreferences("area_prefs", MODE_PRIVATE)
    }
    val scope = rememberCoroutineScope()

    var mode by remember { mutableStateOf("signin") }

    val onAuthSuccess: (String, String) -> Unit = { token, email ->
        scope.launch {
            tokenStore.setToken(token) // ou saveToken selon l'API réelle
            withContext(Dispatchers.IO) {
                prefs.edit().putString("auth_email", email).apply()
            }
            onAuthenticated(token, email)
        }
    }

    if (mode == "signin") {
        SignInScreen(
            onSwitchToSignUp = { mode = "signup" },
            onSignedIn = { token, email -> onAuthSuccess(token, email) }
        )
    } else {
        SignUpScreen(
            onSwitchToSignIn = { mode = "signin" },
            onSignedUp = { token, email -> onAuthSuccess(token, email) }
        )
    }
}

@Composable
fun ErrorPopup(message: String, onDismiss: () -> Unit) {
    val shown = if (message.length > ERROR_DISPLAY_MAX) message.take(ERROR_DISPLAY_MAX) + "..." else message
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier
            .wrapContentWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(color = Color(0xFFFF3333), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = shown, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "X", color = Color.White, modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { onDismiss() })
            }
        }
    }
}

@Composable
fun SignInScreen(onSwitchToSignUp: () -> Unit, onSignedIn: (token: String, email: String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign In",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        com.uwu.area.ui.components.SimpleTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        com.uwu.area.ui.components.SimpleTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        com.uwu.area.ui.components.SimpleButton(
            onClick = {
                if (loading) return@SimpleButton
                loading = true
                error = null
                scope.launch {
                    val res = signin(email, password)
                    loading = false
                    res.fold(onSuccess = { token ->
                        if (token == "ok" || token.isBlank()) {
                            error = "Signin did not return a valid token"
                        } else {
                            onSignedIn(token, email)
                        }
                    }, onFailure = { e ->
                        error = e.message ?: "Unknown error"
                    })
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (loading) "Signing in..." else "Sign In")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onSwitchToSignUp) {
            Text("Create account")
        }
    }

    if (error != null) {
        ErrorPopup(message = error!!, onDismiss = { error = null })
    }
}

@Composable
fun SignUpScreen(onSwitchToSignIn: () -> Unit, onSignedUp: (token: String, email: String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        com.uwu.area.ui.components.SimpleTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        com.uwu.area.ui.components.SimpleTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        com.uwu.area.ui.components.SimpleButton(
            onClick = {
                if (loading) return@SimpleButton
                loading = true
                error = null
                scope.launch {
                    val res = signup(email, password)
                    if (res.isSuccess) {
                        val signupToken = res.getOrNull() ?: ""
                        if (signupToken.isNotBlank() && signupToken != "ok") {
                            loading = false
                            onSignedUp(signupToken, email)
                        } else {
                            val signRes = signin(email, password)
                            if (signRes.isSuccess) {
                                val token = signRes.getOrNull() ?: ""
                                if (token.isNotBlank() && token != "ok") {
                                    loading = false
                                    onSignedUp(token, email)
                                } else {
                                    loading = false
                                    error = "Signin did not return a valid token"
                                }
                            } else {
                                loading = false
                                error = signRes.exceptionOrNull()?.message ?: "Signin after signup failed"
                            }
                        }
                    } else {
                        loading = false
                        error = res.exceptionOrNull()?.message ?: "Signup failed"
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (loading) "Signing up..." else "Sign Up")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onSwitchToSignIn) {
            Text("Already have an account? Sign in")
        }
    }

    if (error != null) {
        ErrorPopup(message = error!!, onDismiss = { error = null })
    }
}

suspend fun getJson(path: String, token: String? = null): Result<String> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + path)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                if (!token.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (!token.isNullOrBlank()) {
                val masked = if (token.length > 8) token.take(4) + "..." + token.takeLast(4) else token
                Log.d("Auth", "GET $path sending Authorization: Bearer $masked")
            } else {
                Log.d("Auth", "GET $path no Authorization header")
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("Auth", "GET $path -> code=$code resp=$respText")

            if (code in 200..299) {
                try {
                    val jo = JSONObject(respText)
                    if (jo.has("url")) return@withContext Result.success(jo.optString("url", ""))
                } catch (_: Exception) {}
                return@withContext Result.success(respText)
            } else {
                try {
                    val jo = JSONObject(respText)
                    val err = extractErrorMessageFromJson(jo)
                    return@withContext Result.failure(Exception(err))
                } catch (_: Exception) {}
                return@withContext Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("Auth", "GET error", e)
            Result.failure(e)
        }
    }
}

suspend fun fetchGithubInit(token: String? = null, redirectUri: String? = null): Result<String> {
    return withContext(Dispatchers.IO) {
        try {
            val query = "?platform=mobile"
            val url = URL(ApiRoutes.BASE + ApiRoutes.GITHUB_INIT + query)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = false
                if (!token.isNullOrBlank()) {
                    setRequestProperty("Authorization", "Bearer $token")
                }
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = conn.responseCode
            Log.d("Auth", "GET ${ApiRoutes.GITHUB_INIT} -> code=$code")

            if (code in 300..399) {
                val location = conn.getHeaderField("Location")
                if (!location.isNullOrBlank()) {
                    Log.d("Auth", "Redirect location=$location")
                    return@withContext Result.success(location)
                } else {
                    return@withContext Result.failure(Exception("Redirect without Location header"))
                }
            }

            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("Auth", "GET ${ApiRoutes.GITHUB_INIT} resp=$respText")

            if (code in 200..299) {
                try {
                    val jo = JSONObject(respText)
                    if (jo.has("redirect_to")) {
                        return@withContext Result.success(jo.optString("redirect_to", ""))
                    }
                    if (jo.has("url")) {
                        return@withContext Result.success(jo.optString("url", ""))
                    }
                } catch (_: Exception) {}
                return@withContext Result.success(respText)
            } else {
                try {
                    val jo = JSONObject(respText)
                    val err = extractErrorMessageFromJson(jo)
                    return@withContext Result.failure(Exception(err))
                } catch (_: Exception) {}
                return@withContext Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("Auth", "fetchGithubInit error", e)
            Result.failure(e)
        }
    }
}
