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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlin.text.set

data class Workflow(
    val ID: Int? = null,
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var actionName by remember { mutableStateOf("") }
    var reactionName by remember { mutableStateOf("") }

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

        getAllActions().fold(
            onSuccess = { actionNames ->
                val actionInfos = actionNames.map { actionName ->
                    ActionInfo(actionName, actionName, "", emptyList())
                }
                availableActionInfos = actionInfos
                actionsLoading = false

                if (actionName.isNotBlank()) {
                    getActionInfo(actionName).fold(
                        onSuccess = { info ->
                            selectedActionInfo = info
                            availableActionInfos = availableActionInfos.map {
                                if (it.Name == actionName) info else it
                            }
                        },
                        onFailure = { /* keep minimal info */ }
                    )
                }
            },
            onFailure = { actionsLoading = false }
        )

        getAllReactions().fold(
            onSuccess = { reactionNames ->
                val reactionInfos = reactionNames.map { reactionName ->
                    ReactionInfo(reactionName, reactionName, "", emptyList())
                }
                availableReactionInfos = reactionInfos
                reactionsLoading = false

                if (reactionName.isNotBlank()) {
                    getReactionInfo(reactionName).fold(
                        onSuccess = { info ->
                            selectedReactionInfo = info
                            availableReactionInfos = availableReactionInfos.map {
                                if (it.Name == reactionName) info else it
                            }
                        },
                        onFailure = { /* keep minimal info */ }
                    )
                }
            },
            onFailure = { reactionsLoading = false }
        )
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
                            val res = createWorkflowApi(token, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() })
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
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)) {

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
                                            actionExpanded = false

                                            if (actionInfo.Description.isEmpty()) {
                                                scope.launch {
                                                    getActionInfo(actionInfo.Name).fold(
                                                        onSuccess = { fullInfo ->
                                                            selectedActionInfo = fullInfo
                                                            availableActionInfos = availableActionInfos.map {
                                                                if (it.Name == actionInfo.Name) fullInfo else it
                                                            }
                                                            actionParams.clear()
                                                            fullInfo.Parameters.forEach { param ->
                                                                actionParams.add("$param=")
                                                            }
                                                        },
                                                        onFailure = {
                                                            selectedActionInfo = actionInfo
                                                            actionParams.clear()
                                                        }
                                                    )
                                                }
                                            } else {
                                                selectedActionInfo = actionInfo
                                                actionParams.clear()
                                                actionInfo.Parameters.forEach { param ->
                                                    actionParams.add("$param=")
                                                }
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
                                            reactionExpanded = false

                                            if (reactionInfo.Description.isEmpty()) {
                                                scope.launch {
                                                    getReactionInfo(reactionInfo.Name).fold(
                                                        onSuccess = { fullInfo ->
                                                            selectedReactionInfo = fullInfo
                                                            availableReactionInfos = availableReactionInfos.map {
                                                                if (it.Name == reactionInfo.Name) fullInfo else it
                                                            }
                                                            reactionParams.clear()
                                                            fullInfo.Parameters.forEach { param ->
                                                                reactionParams.add("$param=")
                                                            }
                                                        },
                                                        onFailure = {
                                                            selectedReactionInfo = reactionInfo
                                                            reactionParams.clear()
                                                        }
                                                    )
                                                }
                                            } else {
                                                selectedReactionInfo = reactionInfo
                                                reactionParams.clear()
                                                reactionInfo.Parameters.forEach { param ->
                                                    reactionParams.add("$param=")
                                                }
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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

        scope.launch {
            val actionInfos = mutableListOf<ActionInfo>()
            getAllActions().fold(
                onSuccess = { actionNames ->
                    actionNames.forEach { actionName ->
                        getActionInfo(actionName).fold(
                            onSuccess = { info -> actionInfos.add(info) },
                            onFailure = { /* skip failed ones */ }
                        )
                    }
                },
                onFailure = { /* keep empty list */ }
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
                            onFailure = { /* skip failed ones */ }
                        )
                    }
                },
                onFailure = { /* keep empty list */ }
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
                            val res = updateWorkflowApi(token, workflow.ID!!, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() }, active)
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
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)) {

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
                                            actionExpanded = false

                                            if (actionInfo.Description.isEmpty()) {
                                                scope.launch {
                                                    getActionInfo(actionInfo.Name).fold(
                                                        onSuccess = { fullInfo ->
                                                            selectedActionInfo = fullInfo
                                                            availableActionInfos = availableActionInfos.map {
                                                                if (it.Name == actionInfo.Name) fullInfo else it
                                                            }
                                                            actionParams.clear()
                                                            fullInfo.Parameters.forEach { param ->
                                                                actionParams.add("$param=")
                                                            }
                                                        },
                                                        onFailure = {
                                                            selectedActionInfo = actionInfo
                                                            actionParams.clear()
                                                        }
                                                    )
                                                }
                                            } else {
                                                selectedActionInfo = actionInfo
                                                actionParams.clear()
                                                actionInfo.Parameters.forEach { param ->
                                                    actionParams.add("$param=")
                                                }
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
                                            reactionExpanded = false

                                            if (reactionInfo.Description.isEmpty()) {
                                                scope.launch {
                                                    getReactionInfo(reactionInfo.Name).fold(
                                                        onSuccess = { fullInfo ->
                                                            selectedReactionInfo = fullInfo
                                                            availableReactionInfos = availableReactionInfos.map {
                                                                if (it.Name == reactionInfo.Name) fullInfo else it
                                                            }
                                                            reactionParams.clear()
                                                            fullInfo.Parameters.forEach { param ->
                                                                reactionParams.add("$param=")
                                                            }
                                                        },
                                                        onFailure = {
                                                            selectedReactionInfo = reactionInfo
                                                            reactionParams.clear()
                                                        }
                                                    )
                                                }
                                            } else {
                                                selectedReactionInfo = reactionInfo
                                                reactionParams.clear()
                                                reactionInfo.Parameters.forEach { param ->
                                                    reactionParams.add("$param=")
                                                }
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
                            val res = updateWorkflowApi(token, workflow.ID!!, actionName, actionParams.filter { it.isNotBlank() }, reactionName, reactionParams.filter { it.isNotBlank() }, active)
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
