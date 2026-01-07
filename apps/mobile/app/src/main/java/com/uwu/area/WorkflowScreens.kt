package com.uwu.area

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.util.Log
import kotlin.text.set

data class Workflow(
    val ID: Int? = null,
    val Name: String = "",
    val ActionName: String = "",
    val ActionParameters: List<String> = emptyList(),
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
    refreshTrigger: Int = 0
) {
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
                                    Text(text = wf.Name, style = MaterialTheme.typography.titleMedium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = if (wf.Active) "Active" else "Inactive", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = wf.Active,
                                        onCheckedChange = { newActive ->
                                            val workflowId = wf.ID
                                            if (workflowId != null) {
                                                scope.launch {
                                                    // Get full workflow details first
                                                    getWorkflowDetails(token, workflowId).fold(
                                                        onSuccess = { fullWorkflow ->
                                                            // Update with new active status
                                                            updateWorkflowApi(
                                                                token, workflowId,
                                                                fullWorkflow.ActionName,
                                                                fullWorkflow.ActionParameters.map { it }, // Convert back to strings
                                                                fullWorkflow.ReactionName,
                                                                fullWorkflow.ReactionParameters.map { it }, // Convert back to strings
                                                                newActive,
                                                                fullWorkflow.Name
                                                            ).fold(
                                                                onSuccess = { updatedWorkflow ->
                                                                    // Debug logging
                                                                    println("Updating workflow ${workflowId}: old name='${wf.Name}', new name='${updatedWorkflow.Name}'")
                                                                    // Update local list - find by index to be more reliable
                                                                    val index = workflows.indexOfFirst { it.ID == workflowId }
                                                                    if (index != -1) {
                                                                        val newList = workflows.toMutableList()
                                                                        newList[index] = updatedWorkflow
                                                                        workflows = newList
                                                                        println("Updated workflow at index $index")
                                                                    } else {
                                                                        println("Could not find workflow with ID $workflowId in list")
                                                                    }
                                                                },
                                                                onFailure = { e -> error = e.message ?: "Update failed" }
                                                            )
                                                        },
                                                        onFailure = { e -> error = e.message ?: "Failed to get workflow details" }
                                                    )
                                                }
                                            }
                                        }
                                    )
                                IconButton(onClick = {
                                    val workflowId: Int? = wf.ID
                                    if (workflowId != null) {
                                        scope.launch {
                                            try {
                                                val res = deleteWorkflowApi(token, workflowId)
                                                res.fold(onSuccess = {
                                                    // Use index-based removal for reliability
                                                    val index = workflows.indexOfFirst { it.ID == workflowId }
                                                    if (index != -1) {
                                                        val newList = workflows.toMutableList()
                                                        newList.removeAt(index)
                                                        workflows = newList
                                                        println("Deleted workflow at index $index")
                                                        onDeleted(workflowId) // Call the callback after successful deletion
                                                    }
                                                }, onFailure = { e ->
                                                    error = e.message ?: "Delete failed"
                                                    Log.e("WorkflowScreens", "Failed to delete workflow $workflowId", e)
                                                })
                                            } catch (e: Exception) {
                                                error = "Unexpected error during delete"
                                                Log.e("WorkflowScreens", "Unexpected error deleting workflow $workflowId", e)
                                            }
                                        }
                                    }
                                }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete")
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var workflowName by remember { mutableStateOf("") }
    var actionName by remember { mutableStateOf("") }
    var reactionName by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    val actionParams = remember { mutableStateListOf<String>() }
    val reactionParams = remember { mutableStateListOf<String>() }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var availableActionInfos by remember { mutableStateOf<List<ActionInfo>>(emptyList()) }
    var availableReactionInfos by remember { mutableStateOf<List<ReactionInfo>>(emptyList()) }
    var selectedActionInfo by remember { mutableStateOf<ActionInfo?>(null) }
    var selectedReactionInfo by remember { mutableStateOf<ReactionInfo?>(null) }
    var actionsLoading by remember { mutableStateOf(false) }
    var reactionsLoading by remember { mutableStateOf(false) }
    var actionExpanded by remember { mutableStateOf(false) }
    var reactionExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        actionsLoading = true
        reactionsLoading = true

        // Load all action details upfront
        scope.launch {
            val actionInfos = mutableListOf<ActionInfo>()
            getAllActions().fold(
                onSuccess = { actionNames ->
                    actionNames.forEach { actionName ->
                        getActionInfo(actionName).fold(
                            onSuccess = { info -> actionInfos.add(info) },
                            onFailure = {
                                // Fallback: create minimal info if API fails
                                actionInfos.add(ActionInfo(actionName, actionName, "", emptyList()))
                            }
                        )
                    }
                },
                onFailure = {  }
            )
            availableActionInfos = actionInfos
            actionsLoading = false

            // Set selected action info if editing
            if (actionName.isNotBlank()) {
                selectedActionInfo = actionInfos.find { it.Name == actionName }
            }
        }

        // Load all reaction details upfront
        scope.launch {
            val reactionInfos = mutableListOf<ReactionInfo>()
            getAllReactions().fold(
                onSuccess = { reactionNames ->
                    reactionNames.forEach { reactionName ->
                        getReactionInfo(reactionName).fold(
                            onSuccess = { info -> reactionInfos.add(info) },
                            onFailure = {
                                // Fallback: create minimal info if API fails
                                reactionInfos.add(ReactionInfo(reactionName, reactionName, "", emptyList()))
                            }
                        )
                    }
                },
                onFailure = {  }
            )
            availableReactionInfos = reactionInfos
            reactionsLoading = false

            // Set selected reaction info if editing
            if (reactionName.isNotBlank()) {
                selectedReactionInfo = reactionInfos.find { it.Name == reactionName }
            }
        }
    }

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
                            val res = createWorkflowApi(token, workflowName.ifBlank { "New Workflow" }, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() }, isActive)
                            loading = false
                            res.fold(
                                onSuccess = { wf -> onSaved(wf) },
                                onFailure = { e ->
                                    val errorMsg = e.message ?: "Save failed"
                                    if (errorMsg.contains("syntax", ignoreCase = true) ||
                                        errorMsg.contains("validation", ignoreCase = true) ||
                                        errorMsg.contains("Workflow check failed", ignoreCase = true)) {
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    } else {
                                        error = errorMsg
                                    }
                                }
                            )
                        }
                    }) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp)) else Text("Save")
                    }
                }
            )
        },
        content = { innerPadding ->
            val scrollState = rememberScrollState()
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)) {

                // Workflow Name and Active Status
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Workflow Settings", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = workflowName,
                            onValueChange = { workflowName = it },
                            label = { Text("Workflow Name") },
                            placeholder = { Text("Enter workflow name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Active", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Trigger Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = actionExpanded,
                            onExpandedChange = { actionExpanded = !actionExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedActionInfo?.PrettyName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Action") },
                                trailingIcon = {
                                    if (actionsLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    } else {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionExpanded)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = actionExpanded,
                                onDismissRequest = { actionExpanded = false }
                            ) {
                                availableActionInfos.forEach { actionInfo ->
                                    DropdownMenuItem(
                                        text = { Text(actionInfo.PrettyName) },
                                        onClick = {
                                            actionName = actionInfo.Name
                                            selectedActionInfo = actionInfo
                                            actionExpanded = false
                                            actionParams.clear()
                                            actionInfo.Parameters.forEach { param ->
                                                actionParams.add("$param=")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Action parameters", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        for ((index, param) in actionParams.withIndex()) {
                            val parts = param.split("=", limit = 2)
                            val key = parts.getOrNull(0) ?: ""
                            val value = parts.getOrNull(1) ?: ""
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("$key:", modifier = Modifier.width(100.dp))
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { v -> actionParams[index] = "$key=$v" },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    placeholder = { Text("Enter value") },
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
                        ExposedDropdownMenuBox(
                            expanded = reactionExpanded,
                            onExpandedChange = { reactionExpanded = !reactionExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedReactionInfo?.PrettyName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Reaction") },
                                trailingIcon = {
                                    if (reactionsLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    } else {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = reactionExpanded)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = reactionExpanded,
                                onDismissRequest = { reactionExpanded = false }
                            ) {
                                availableReactionInfos.forEach { reactionInfo ->
                                    DropdownMenuItem(
                                        text = { Text(reactionInfo.PrettyName) },
                                        onClick = {
                                            reactionName = reactionInfo.Name
                                            selectedReactionInfo = reactionInfo
                                            reactionExpanded = false
                                            reactionParams.clear()
                                            reactionInfo.Parameters.forEach { param ->
                                                reactionParams.add("$param=")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Reaction parameters", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        for ((index, param) in reactionParams.withIndex()) {
                            val parts = param.split("=", limit = 2)
                            val key = parts.getOrNull(0) ?: ""
                            val value = parts.getOrNull(1) ?: ""
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("$key:", modifier = Modifier.width(100.dp))
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { v -> reactionParams[index] = "$key=$v" },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    placeholder = { Text("Enter value") },
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
                            val res = createWorkflowApi(token, workflowName.ifBlank { "New Workflow" }, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() }, isActive)
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var workflowName by remember { mutableStateOf(workflow.Name) }
    var actionName by remember { mutableStateOf(workflow.ActionName) }
    var reactionName by remember { mutableStateOf(workflow.ReactionName) }
    var active by remember { mutableStateOf(workflow.Active) }

    val actionParams = remember { mutableStateListOf<String>().apply { addAll(workflow.ActionParameters) } }
    val reactionParams = remember { mutableStateListOf<String>().apply { addAll(workflow.ReactionParameters) } }

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var availableActionInfos by remember { mutableStateOf<List<ActionInfo>>(emptyList()) }
    var availableReactionInfos by remember { mutableStateOf<List<ReactionInfo>>(emptyList()) }
    var selectedActionInfo by remember { mutableStateOf<ActionInfo?>(null) }
    var selectedReactionInfo by remember { mutableStateOf<ReactionInfo?>(null) }
    var actionsLoading by remember { mutableStateOf(false) }
    var reactionsLoading by remember { mutableStateOf(false) }
    var actionExpanded by remember { mutableStateOf(false) }
    var reactionExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        actionsLoading = true
        reactionsLoading = true

        // First, fetch full workflow details if we don't have them
        scope.launch {
            if (workflow.ActionName.isBlank() || workflow.ReactionName.isBlank()) {
                // Need to fetch full details
                workflow.ID?.let { id ->
                    Log.d("WorkflowScreens", "Editing workflow with ID $id, initial name '${workflow.Name}'")
                    getWorkflowDetails(token, id).fold(
                        onSuccess = { fullWorkflow ->
                            Log.d("WorkflowScreens", "getWorkflowDetails returned: ID=${fullWorkflow.ID}, Name='${fullWorkflow.Name}'")
                            // Update our local state with full details
                            workflowName = fullWorkflow.Name
                            actionName = fullWorkflow.ActionName
                            reactionName = fullWorkflow.ReactionName
                            active = fullWorkflow.Active
                            actionParams.clear()
                            actionParams.addAll(fullWorkflow.ActionParameters)
                            reactionParams.clear()
                            reactionParams.addAll(fullWorkflow.ReactionParameters)
                        },
                        onFailure = { e ->
                            Log.e("WorkflowScreens", "getWorkflowDetails failed for ID $id", e)
                        }
                    )
                }
            }
        }

        scope.launch {
            val actionInfos = mutableListOf<ActionInfo>()
            getAllActions().fold(
                onSuccess = { actionNames ->
                    actionNames.forEach { actionName ->
                        getActionInfo(actionName).fold(
                            onSuccess = { info -> actionInfos.add(info) },
                            onFailure = {  }
                        )
                    }
                },
                onFailure = {  }
            )
            availableActionInfos = actionInfos
            actionsLoading = false

            val selectedAction = actionInfos.find { it.Name == actionName }
            if (selectedAction != null) {
                selectedActionInfo = selectedAction
            }
        }

        scope.launch {
            val reactionInfos = mutableListOf<ReactionInfo>()
            getAllReactions().fold(
                onSuccess = { reactionNames ->
                    reactionNames.forEach { reactionName ->
                        getReactionInfo(reactionName).fold(
                            onSuccess = { info -> reactionInfos.add(info) },
                            onFailure = {  }
                        )
                    }
                },
                onFailure = {  }
            )
            availableReactionInfos = reactionInfos
            reactionsLoading = false

            val selectedReaction = reactionInfos.find { it.Name == reactionName }
            if (selectedReaction != null) {
                selectedReactionInfo = selectedReaction
            }
        }
    }

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
                            val res = updateWorkflowApi(token, workflow.ID!!, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() }, active, workflowName)
                            loading = false
                            res.fold(
                                onSuccess = { wf -> onSaved(wf) },
                                onFailure = { e ->
                                    val errorMsg = e.message ?: "Save failed"
                                    if (errorMsg.contains("syntax", ignoreCase = true) ||
                                        errorMsg.contains("validation", ignoreCase = true) ||
                                        errorMsg.contains("Workflow check failed", ignoreCase = true)) {
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                    } else {
                                        error = errorMsg
                                    }
                                }
                            )
                        }
                    }) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(18.dp)) else Text("Save")
                    }
                }
            )
        },
        content = { innerPadding ->
            val scrollState = rememberScrollState()
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState)) {

                // Workflow Name
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Workflow Settings", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = workflowName,
                            onValueChange = { workflowName = it },
                            label = { Text("Workflow Name") },
                            placeholder = { Text("Enter workflow name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Trigger Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = actionExpanded,
                            onExpandedChange = { actionExpanded = !actionExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedActionInfo?.PrettyName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Action") },
                                trailingIcon = {
                                    if (actionsLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    } else {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionExpanded)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = actionExpanded,
                                onDismissRequest = { actionExpanded = false }
                            ) {
                                availableActionInfos.forEach { actionInfo ->
                                    DropdownMenuItem(
                                        text = { Text(actionInfo.PrettyName) },
                                        onClick = {
                                            actionName = actionInfo.Name
                                            selectedActionInfo = actionInfo
                                            actionExpanded = false
                                            actionParams.clear()
                                            actionInfo.Parameters.forEach { param ->
                                                actionParams.add("$param=")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Action parameters", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        for ((index, param) in actionParams.withIndex()) {
                            val parts = param.split("=", limit = 2)
                            val key = parts.getOrNull(0) ?: ""
                            val value = parts.getOrNull(1) ?: ""
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("$key:", modifier = Modifier.width(100.dp))
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { v -> actionParams[index] = "$key=$v" },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    placeholder = { Text("Enter value") },
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
                        ExposedDropdownMenuBox(
                            expanded = reactionExpanded,
                            onExpandedChange = { reactionExpanded = !reactionExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedReactionInfo?.PrettyName ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Reaction") },
                                trailingIcon = {
                                    if (reactionsLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                    } else {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = reactionExpanded)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = reactionExpanded,
                                onDismissRequest = { reactionExpanded = false }
                            ) {
                                availableReactionInfos.forEach { reactionInfo ->
                                    DropdownMenuItem(
                                        text = { Text(reactionInfo.PrettyName) },
                                        onClick = {
                                            reactionName = reactionInfo.Name
                                            selectedReactionInfo = reactionInfo
                                            reactionExpanded = false
                                            reactionParams.clear()
                                            reactionInfo.Parameters.forEach { param ->
                                                reactionParams.add("$param=")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Reaction parameters", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        for ((index, param) in reactionParams.withIndex()) {
                            val parts = param.split("=", limit = 2)
                            val key = parts.getOrNull(0) ?: ""
                            val value = parts.getOrNull(1) ?: ""
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text("$key:", modifier = Modifier.width(100.dp))
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { v -> reactionParams[index] = "$key=$v" },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    placeholder = { Text("Enter value") },
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

                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Switch(
                            checked = active,
                            onCheckedChange = { active = it }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    Button(onClick = {
                        if (loading) return@Button
                        loading = true
                        error = null
                        scope.launch {
                            val res = updateWorkflowApi(token, workflow.ID!!, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() }, active, workflowName)
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
