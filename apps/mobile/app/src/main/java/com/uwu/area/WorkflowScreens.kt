package com.uwu.area

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.text.KeyboardOptions
import kotlin.text.set

data class Workflow(
    val ID: Int? = null,
    val ActionName: String = "",
    val ActionParameters: List<String> = emptyList(),
    val ReactionName: String = "",
    val ReactionParameters: List<String> = emptyList()
)

@Composable
fun WorkflowListScreen(
    token: String?,
    onOpenCreate: () -> Unit,
    onEdit: (Workflow) -> Unit = {},
    onDeleted: (Int) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var workflows by remember { mutableStateOf<List<Workflow>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
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
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    items(workflows) { wf ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable { wf.ID?.let { onEdit(wf) } }
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = wf.ActionName, style = MaterialTheme.typography.titleMedium)
                                    Text(text = "ID: ${wf.ID}", style = MaterialTheme.typography.bodySmall)
                                    Text(text = "→ ${wf.ReactionName}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = {
                                    val id: Int? = wf.ID
                                    scope.launch {
                                        val res = deleteWorkflowApi(token, id)
                                        res.fold(onSuccess = {
                                            workflows = workflows.filter { it.ID != id }
                                        }, onFailure = { e -> error = e.message ?: "Delete failed" })
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
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
    val scope = rememberCoroutineScope()
    var actionName by remember { mutableStateOf("") }
    var reactionName by remember { mutableStateOf("") }

    val actionParams = remember { mutableStateListOf<String>() }
    val reactionParams = remember { mutableStateListOf<String>() }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Workflow") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (loading) return@TextButton
                        loading = true
                        error = null
                        scope.launch {
                            val res = createWorkflowApi(token, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() })
                            loading = false
                            res.fold(onSuccess = { wf -> onSaved(wf) }, onFailure = { e -> error = e.message ?: "Save failed" })
                        }
                    }) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp)) else Text("Save")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)) {

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Trigger Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = actionName,
                            onValueChange = { actionName = it },
                            label = { Text("Action name") },
                            placeholder = { Text("Ex: timer_cron_job") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Action parameters", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        for ((index, param) in actionParams.withIndex()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                OutlinedTextField(
                                    value = param,
                                    onValueChange = { v -> actionParams[index] = v },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    placeholder = { Text("Ex: * * * * *") },
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                                )
                                IconButton(onClick = { actionParams.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                        TextButton(onClick = { actionParams.add("") }) { Text("Add parameter") }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Reaction Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reactionName,
                            onValueChange = { reactionName = it },
                            label = { Text("Reaction name") },
                            placeholder = { Text("Ex: github_create_issue") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Reaction parameters", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        for ((index, param) in reactionParams.withIndex()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                OutlinedTextField(
                                    value = param,
                                    onValueChange = { v -> reactionParams[index] = v },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    placeholder = { Text("Ex: dawpitech/test-thingy") },
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                                )
                                IconButton(onClick = { reactionParams.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                        TextButton(onClick = { reactionParams.add("") }) { Text("Add parameter") }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    Button(onClick = {
                        if (loading) return@Button
                        loading = true
                        error = null
                        scope.launch {
                            val res = createWorkflowApi(token, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() })
                            loading = false
                            res.fold(onSuccess = { wf -> onSaved(wf) }, onFailure = { e -> error = e.message ?: "Save failed" })
                        }
                    }) {
                        Text("Create workflow")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(onClick = onClose) {
                        Text("Cancel")
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = error!!, color = Color(0xFFFF3333))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkflowScreen(
    token: String?,
    workflow: Workflow,
    onClose: () -> Unit,
    onSaved: (Workflow) -> Unit
) {
    val scope = rememberCoroutineScope()
    var actionName by remember { mutableStateOf(workflow.ActionName) }
    var reactionName by remember { mutableStateOf(workflow.ReactionName) }

    val actionParams = remember { mutableStateListOf<String>().apply { addAll(workflow.ActionParameters) } }
    val reactionParams = remember { mutableStateListOf<String>().apply { addAll(workflow.ReactionParameters) } }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Workflow") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (loading) return@TextButton
                        loading = true
                        error = null
                        scope.launch {
                            val res = updateWorkflowApi(token, workflow.ID!!, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() })
                            loading = false
                            res.fold(onSuccess = { wf -> onSaved(wf) }, onFailure = { e -> error = e.message ?: "Save failed" })
                        }
                    }) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp)) else Text("Save")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)) {

                // Trigger card
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Trigger Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = actionName,
                            onValueChange = { actionName = it },
                            label = { Text("Action name") },
                            placeholder = { Text("Ex: timer_cron_job") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Action parameters", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        for ((index, param) in actionParams.withIndex()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                OutlinedTextField(
                                    value = param,
                                    onValueChange = { v -> actionParams[index] = v },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    placeholder = { Text("Ex: * * * * *") },
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                                )
                                IconButton(onClick = { actionParams.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                        TextButton(onClick = { actionParams.add("") }) { Text("Add parameter") }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Reaction Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reactionName,
                            onValueChange = { reactionName = it },
                            label = { Text("Reaction name") },
                            placeholder = { Text("Ex: github_create_issue") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Reaction parameters", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        for ((index, param) in reactionParams.withIndex()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                OutlinedTextField(
                                    value = param,
                                    onValueChange = { v -> reactionParams[index] = v },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    placeholder = { Text("Ex: dawpitech/test-thingy") },
                                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                                )
                                IconButton(onClick = { reactionParams.removeAt(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                        TextButton(onClick = { reactionParams.add("") }) { Text("Add parameter") }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    Button(onClick = {
                        if (loading) return@Button
                        loading = true
                        error = null
                        scope.launch {
                            val res = updateWorkflowApi(token, workflow.ID!!, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() })
                            loading = false
                            res.fold(onSuccess = { wf -> onSaved(wf) }, onFailure = { e -> error = e.message ?: "Save failed" })
                        }
                    }) {
                        Text("Update workflow")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(onClick = onClose) {
                        Text("Cancel")
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = error!!, color = Color(0xFFFF3333))
                }
            }
        }
    )
}
