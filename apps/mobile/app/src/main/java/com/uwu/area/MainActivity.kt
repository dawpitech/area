package com.uwu.area

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
                    modifier = Modifier.width(280.dp),
                    drawerContainerColor = Color(0xFFF5F5F5)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = "Home",
                                    fontWeight = if (currentScreen == Screen.HOME) FontWeight.Bold else FontWeight.Normal,
                                    color = Color(0xFF1F2937)
                                )
                            },
                            selected = currentScreen == Screen.HOME,
                            onClick = {
                                currentScreen = Screen.HOME
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFFE5E7EB),
                                unselectedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = "Workflows",
                                    fontWeight = if (currentScreen == Screen.WORKFLOWS) FontWeight.Bold else FontWeight.Normal,
                                    color = Color(0xFF1F2937)
                                )
                            },
                            selected = currentScreen == Screen.WORKFLOWS,
                            onClick = {
                                currentScreen = Screen.WORKFLOWS
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color(0xFFE5E7EB),
                                unselectedContainerColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = "Log out",
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFDC2626)
                                )
                            },
                            selected = false,
                            onClick = {
                                onSignOut()
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = Color.Transparent,
                                unselectedContainerColor = Color.Transparent
                            )
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
                                    EditWorkflowScreen(
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
                                    CreateWorkflowScreen(
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