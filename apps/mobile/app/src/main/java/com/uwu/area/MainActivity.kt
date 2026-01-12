package com.uwu.area

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uwu.area.ui.theme.AreaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AreaTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current

    LaunchedEffect(Unit) { TokenStore.init(context) }
    val tokenStore = TokenStore

    val prefs = remember {
        context.getSharedPreferences("area_prefs", MODE_PRIVATE)
    }

    var token by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var isInitialized by remember { mutableStateOf(false) }
    var workflowRefreshTrigger by remember { mutableStateOf(0) }
    var showCreate by remember { mutableStateOf(false) }
    var editWorkflow by remember { mutableStateOf<Workflow?>(null) }
    var showLogsWorkflow by remember { mutableStateOf<Workflow?>(null) }
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            token = tokenStore.getToken()
            email = prefs.getString("auth_email", null)
        }
        isInitialized = true
    }

    if (!isInitialized) return

    val onSignOut: () -> Unit = {
        scope.launch {
            tokenStore.clearToken()
            withContext(Dispatchers.IO) {
                prefs.edit().remove("auth_email").apply()
            }
            token = null
            email = null
            currentScreen = Screen.HOME
        }
    }

    if (token == null) {
        AuthHost(onAuthenticated = { receivedToken, receivedEmail ->
            token = receivedToken
            email = receivedEmail
        })
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface,
                    drawerShape = RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            com.uwu.area.ui.theme.Blue2734bd,
                                            com.uwu.area.ui.theme.Blue2734bd,
                                            com.uwu.area.ui.theme.Blue2734bd
                                        )
                                    )
                                )
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "AREA",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (email != null) {
                            val emailValue = email!!
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    com.uwu.area.ui.theme.Blue2734bd,
                                                    com.uwu.area.ui.theme.Blue2734bd
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emailValue.first().uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Welcome back!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = emailValue,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = "Home",
                                        fontWeight = if (currentScreen == Screen.HOME) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                selected = currentScreen == Screen.HOME,
                                onClick = {
                                    currentScreen = Screen.HOME
                                    scope.launch { drawerState.close() }
                                }
                            )

                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = "Workflows",
                                        fontWeight = if (currentScreen == Screen.WORKFLOWS) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                selected = currentScreen == Screen.WORKFLOWS,
                                onClick = {
                                    currentScreen = Screen.WORKFLOWS
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))

                        NavigationDrawerItem(
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Logout,
                                        contentDescription = "Logout",
                                        tint = com.uwu.area.ui.theme.ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Sign Out",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = com.uwu.area.ui.theme.ErrorRed
                                    )
                                }
                            },
                            selected = false,
                            onClick = {
                                onSignOut()
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color.Transparent,
                                unselectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    NavigationBar(
                        currentScreen = currentScreen,
                        email = email,
                        onMenuClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    when (currentScreen) {
                        Screen.HOME -> {
                            HomeScreen(
                                token = token!!,
                                email = email,
                                onSignOut = onSignOut
                            )
                        }

                        Screen.WORKFLOWS -> {
                            when {
                                editWorkflow != null -> {
                                    NewEditWorkflowScreen(
                                        token = token,
                                        workflow = editWorkflow!!,
                                        onClose = { editWorkflow = null },
                                        onSaved = { updatedWorkflow ->
                                            editWorkflow = null
                                            workflowRefreshTrigger++
                                        }
                                    )
                                }
                                showLogsWorkflow != null -> {
                                    WorkflowLogsScreen(
                                        token = token,
                                        workflowId = showLogsWorkflow!!.ID!!,
                                        workflowName = showLogsWorkflow!!.Name,
                                        onDismiss = { showLogsWorkflow = null }
                                    )
                                }
                                showCreate -> {
                                    NewCreateWorkflowScreen(
                                        token = token,
                                        onClose = { showCreate = false },
                                        onSaved = { _ ->
                                            showCreate = false
                                            workflowRefreshTrigger++
                                        }
                                    )
                                }
                                else -> {
                                    WorkflowListScreen(
                                        token = token,
                                        onOpenCreate = { showCreate = true },
                                        onEdit = { workflow -> editWorkflow = workflow },
                                        onShowLogs = { workflow -> showLogsWorkflow = workflow },
                                        refreshTrigger = workflowRefreshTrigger
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AreaTheme {
        AuthHost(onAuthenticated = { _, _ -> })
    }
}