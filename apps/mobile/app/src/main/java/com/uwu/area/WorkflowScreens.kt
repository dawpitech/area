package com.uwu.area

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.util.Log
import kotlin.text.set
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.ArrowDropDown
import com.uwu.area.ui.theme.*
data class Workflow(
    val ID: Int? = null,
    val Name: String = "",
    val ActionName: String = "",
    val ActionParameters: List<String> = emptyList(),
    val ModifierName: String = "",
    val ModifierParameters: List<String> = emptyList(),
    val ReactionName: String = "",
    val ReactionParameters: List<String> = emptyList(),
    val Active: Boolean = true
)

@Composable
fun WorkflowListScreen(
    token: String?,
    onOpenCreate: () -> Unit,
    onEdit: (Workflow) -> Unit = {},
    onDeleted: (Int) -> Unit = {},
    onShowLogs: (Workflow) -> Unit = {},
    refreshTrigger: Int = 0
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var workflows by remember { mutableStateOf<List<Workflow>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit, refreshTrigger) {
        loading = true
        fetchWorkflows(token).fold(onSuccess = { list ->
            workflows = list
            loading = false
        }, onFailure = { e ->
            error = e.message ?: "Error"
            loading = false
        })
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenCreate) {
                Icon(Icons.Default.Add, contentDescription = "Create")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(workflows) { wf ->
                        com.uwu.area.ui.components.WorkflowCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { wf.ID?.let { onEdit(wf) } }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = wf.Name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        com.uwu.area.ui.components.StatusIndicator(
                                            isActive = wf.Active
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (wf.Active) "Active" else "Inactive",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row {
                                    IconButton(onClick = {
                                        val workflowId: Int? = wf.ID
                                        if (workflowId != null) {
                                            scope.launch {
                                                try {
                                                    val res = toggleWorkflowActiveApi(token, workflowId, !wf.Active)
                                                    res.fold(onSuccess = { updatedWorkflow ->
                                                        val index = workflows.indexOfFirst { it.ID == workflowId }
                                                        if (index != -1) {
                                                            val newList = workflows.toMutableList()
                                                            newList[index] = updatedWorkflow
                                                            workflows = newList
                                                        }
                                                    }, onFailure = { e ->
                                                        val errorMsg = e.message ?: "Toggle failed"
                                                        error = errorMsg
                                                        Toast.makeText(context, "Failed to toggle workflow: $errorMsg", Toast.LENGTH_LONG).show()
                                                    })
                                                } catch (e: Exception) {
                                                    val errorMsg = "Unexpected error during toggle"
                                                    error = errorMsg
                                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }) {
                                        Icon(
                                            if (wf.Active) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (wf.Active) "Deactivate workflow" else "Activate workflow",
                                            tint = if (wf.Active) ErrorRed else ActiveGreen
                                        )
                                    }
                                    IconButton(onClick = { onShowLogs(wf) }) {
                                        Icon(Icons.Default.BugReport, contentDescription = "View Logs")
                                    }
                                    IconButton(onClick = {
                                        val workflowId: Int? = wf.ID
                                        if (workflowId != null) {
                                            scope.launch {
                                                try {
                                                    val res = deleteWorkflowApi(token, workflowId)
                                                    res.fold(onSuccess = {
                                                        val index = workflows.indexOfFirst { it.ID == workflowId }
                                                        if (index != -1) {
                                                            val newList = workflows.toMutableList()
                                                            newList.removeAt(index)
                                                            workflows = newList
                                                            onDeleted(workflowId)
                                                        }
                                                    }, onFailure = { e ->
                                                        val errorMsg = e.message ?: "Delete failed"
                                                        error = errorMsg
                                                        Toast.makeText(context, "Failed to delete workflow: $errorMsg", Toast.LENGTH_LONG).show()
                                                    })
                                                } catch (e: Exception) {
                                                    val errorMsg = "Unexpected error during delete"
                                                    error = errorMsg
                                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (error != null) {
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter)) {
                    Text(text = error!!)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkflowScreen(
    token: String?,
    onClose: () -> Unit,
    onSaved: (Workflow) -> Unit
) {
    NewCreateWorkflowScreen(token, onClose, onSaved)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowLogsScreen(
    token: String?,
    workflowId: Int,
    workflowName: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var logs by remember { mutableStateOf<List<LogEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(workflowId) {
        loading = true
        fetchWorkflowLogs(token, workflowId).fold(onSuccess = { logList ->
            logs = logList.sortedByDescending { parseTimestamp(it.Timestamp) }
            loading = false
        }, onFailure = { e ->
            error = e.message ?: "Error loading logs"
            loading = false
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Workflow Logs")
                        Text(
                            text = workflowName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = "Error",
                        tint = Color(0xFFFF6B6B),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else if (logs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "No logs",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No logs available for this workflow",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { log ->
                        LogEntryCard(log)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterInputField(
    parameterTechnicalName: String,
    parameterPrettyName: String,
    value: String,
    onValueChange: (String) -> Unit,
    availableOutputs: List<OutputInfo>,
    allowOutputSelection: Boolean = true,
    showOnlyActionOutputs: Boolean = false,
    modifier: Modifier = Modifier
) {
    val displayOutputs = if (showOnlyActionOutputs) {
        availableOutputs.filter { it.Source == "action" }
    } else {
        availableOutputs
    }
    var outputExpanded by remember { mutableStateOf(false) }

    val displayValue = if (value.startsWith("#")) {
        val outputName = value.substring(1)
        displayOutputs.find { it.Name == outputName }?.PrettyName ?: value
    } else {
        value
    }

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("$parameterPrettyName:", modifier = Modifier.width(120.dp))
            OutlinedTextField(
                value = displayValue,
                onValueChange = { newValue ->
                    val matchingOutput = displayOutputs.find { it.PrettyName == newValue }
                    if (matchingOutput != null) {
                        onValueChange("#${matchingOutput.Name}")
                    } else {
                        onValueChange(newValue)
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(if (allowOutputSelection) "Enter value or select output" else "Enter value") },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                trailingIcon = if (allowOutputSelection) {
                    {
                        IconButton(onClick = { outputExpanded = !outputExpanded }) {
                            Icon(
                                if (outputExpanded) Icons.Default.Close else Icons.Default.ArrowDropDown,
                                contentDescription = "Select output"
                            )
                        }
                    }
                } else null
            )
        }

        if (outputExpanded && displayOutputs.isNotEmpty() && allowOutputSelection) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(start = 124.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Available Outputs:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    displayOutputs.forEach { output ->
                        TextButton(
                            onClick = {
                                onValueChange("#${output.Name}")
                                outputExpanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(output.PrettyName, textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogEntryCard(log: LogEntry) {
    val (backgroundColor, icon, iconTint, textColor) = when (log.Type.lowercase()) {
        "error" -> Quad(Color(0xFFFFEBEE), Icons.Default.Error, Color(0xFFD32F2F), Color.Black)
        "warn", "warning" -> Quad(Color(0xFFFFF8E1), Icons.Default.Warning, Color(0xFFF57C00), Color.Black)
        "info" -> Quad(Color(0xFFE3F2FD), Icons.Default.Info, Color(0xFF1976D2), Color.Black)
        else -> Quad(Color(0xFFF5F5F5), Icons.Default.Info, Color(0xFF757575), Color.Black)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                icon,
                contentDescription = log.Type,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.Message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatTimestamp(log.Timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

private data class Quad<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

private fun parseTimestamp(timestamp: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.parse(timestamp)?.time ?: 0L
    } catch (e: Exception) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'", Locale.getDefault())
            sdf.parse(timestamp.replace("T", " "))?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = sdf.parse(timestamp)
        val outputFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (e: Exception) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = sdf.parse(timestamp.replace("T", " "))
            val outputFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: timestamp
        } catch (e: Exception) {
            timestamp
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkflowScreen(
    token: String?,
    workflow: Workflow,
    onClose: () -> Unit,
    onSaved: (Workflow) -> Unit
) {
    NewEditWorkflowScreen(token, workflow, onClose, onSaved)
}
