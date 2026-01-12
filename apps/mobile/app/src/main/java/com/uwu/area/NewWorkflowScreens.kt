package com.uwu.area

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.uwu.area.ui.theme.Blue2734bd
import com.uwu.area.ui.theme.ActiveGreen
import com.uwu.area.ui.theme.ErrorRed

data class WorkflowState(
    val id: Int? = null,
    var name: String = "",
    var active: Boolean = true,
    var selectedActionInfo: ActionInfo? = null,
    val actionParams: MutableState<List<ParameterValue>> = mutableStateOf(emptyList()),
    var selectedModifierInfo: ModifierInfo? = null,
    val modifierParams: MutableState<List<ParameterValue>> = mutableStateOf(emptyList()),
    var selectedReactionInfo: ReactionInfo? = null,
    val reactionParams: MutableState<List<ParameterValue>> = mutableStateOf(emptyList()),
    val allAvailableOutputs: MutableState<List<OutputInfo>> = mutableStateOf(emptyList()),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCreateWorkflowScreen(
    token: String?,
    onClose: () -> Unit,
    onSaved: (Workflow) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val workflowState = remember { mutableStateOf(WorkflowState()) }

    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 4 // Name, Action, Modifier, Reaction

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var availableActionInfos by remember { mutableStateOf<List<ActionInfo>>(emptyList()) }
    var availableModifierInfos by remember { mutableStateOf<List<ModifierInfo>>(emptyList()) }
    var availableReactionInfos by remember { mutableStateOf<List<ReactionInfo>>(emptyList()) }

    LaunchedEffect(Unit) {
        scope.launch {
            availableActionInfos = getAllActions().fold(onSuccess = { actionNames ->
                actionNames.mapNotNull { actionName -> getActionInfo(actionName).getOrNull() }
            }, onFailure = { emptyList() })
        }
        scope.launch {
            availableModifierInfos = getAllModifiers().fold(onSuccess = { modifierNames ->
                modifierNames.mapNotNull { modifierName -> getModifierInfo(modifierName).getOrNull() }
            }, onFailure = { emptyList() })
        }
        scope.launch {
            availableReactionInfos = getAllReactions().fold(onSuccess = { reactionNames ->
                reactionNames.mapNotNull { reactionName -> getReactionInfo(reactionName).getOrNull() }
            }, onFailure = { emptyList() })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (workflowState.value.id == null) "Create Workflow" else "Edit Workflow") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentStep == totalSteps - 1) { // Only show Save on the last step
                        TextButton(
                            onClick = {
                                if (loading) return@TextButton
                                loading = true
                                error = null
                                scope.launch {
                                    val actionName = workflowState.value.selectedActionInfo?.Name ?: return@launch
                                    val modifierName = workflowState.value.selectedModifierInfo?.Name ?: ""
                                    val reactionName = workflowState.value.selectedReactionInfo?.Name ?: return@launch

                                    val actionParamsList = workflowState.value.actionParams.value.map { "${it.technicalName}=${it.value}" }
                                    val modifierParamsList = workflowState.value.modifierParams.value.map { "${it.technicalName}=${it.value}" }
                                    val reactionParamsList = workflowState.value.reactionParams.value.map { "${it.technicalName}=${it.value}" }

                                    val res = if (workflowState.value.id == null) {
                                        createWorkflowApi(
                                            token,
                                            workflowState.value.name.ifBlank { "New Workflow" },
                                            actionName,
                                            actionParamsList.filter { it.isNotBlank() },
                                            modifierName,
                                            modifierParamsList.filter { it.isNotBlank() },
                                            reactionName,
                                            reactionParamsList.filter { it.isNotBlank() },
                                            workflowState.value.active
                                        )
                                    } else {
                                        updateWorkflowApi(
                                            token,
                                            workflowState.value.id!!,
                                            actionName,
                                            actionParamsList.filter { it.isNotBlank() },
                                            modifierName,
                                            modifierParamsList.filter { it.isNotBlank() },
                                            reactionName,
                                            reactionParamsList.filter { it.isNotBlank() },
                                            workflowState.value.active,
                                            workflowState.value.name
                                        )
                                    }
                                    loading = false
                                    res.fold(
                                        onSuccess = { wf -> onSaved(wf) },
                                        onFailure = { e ->
                                            val errorMsg = e.message ?: "Save failed"
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                            error = errorMsg
                                        }
                                    )
                                }
                            },
                            enabled = !loading
                        ) {
                            if (loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Text("Save")
                            }
                        }
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = (currentStep + 1).toFloat() / totalSteps,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (currentStep) {
                    0 -> WorkflowNameAndActiveStep(workflowState.value) { updatedWorkflowState -> workflowState.value = updatedWorkflowState }
                    1 -> ActionSelectionStep(
                        workflowState.value,
                        availableActionInfos
                    ) { updatedWorkflowState -> workflowState.value = updatedWorkflowState }
                    2 -> ModifierSelectionStep(
                        workflowState.value,
                        availableModifierInfos
                    ) { updatedWorkflowState -> workflowState.value = updatedWorkflowState }
                    3 -> ReactionSelectionStep(
                        workflowState.value,
                        availableReactionInfos
                    ) { updatedWorkflowState -> workflowState.value = updatedWorkflowState }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(onClick = { currentStep-- }) {
                            Text("Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f)) // Push Next to the right
                    }

                    if (currentStep < totalSteps - 1) {
                        Button(onClick = { currentStep++ }) {
                            Text("Next")
                        }
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = error!!, color = ErrorRed)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEditWorkflowScreen(
    token: String?,
    workflow: Workflow,
    onClose: () -> Unit,
    onSaved: (Workflow) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val workflowState = remember { mutableStateOf(WorkflowState(
        id = workflow.ID,
        name = workflow.Name,
        active = workflow.Active
    )) }

    var currentStep by remember { mutableStateOf(0) }
    val totalSteps = 4 // Name, Action, Modifier, Reaction

    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var availableActionInfos by remember { mutableStateOf<List<ActionInfo>>(emptyList()) }
    var availableModifierInfos by remember { mutableStateOf<List<ModifierInfo>>(emptyList()) }
    var availableReactionInfos by remember { mutableStateOf<List<ReactionInfo>>(emptyList()) }

    LaunchedEffect(workflow) {
        val workflowDetails = getWorkflowDetails(token, workflow.ID!!).getOrNull()

        val actionInfos = getAllActions().fold(onSuccess = { actionNames ->
            actionNames.mapNotNull { actionName -> getActionInfo(actionName).getOrNull() }
        }, onFailure = { emptyList() })
        availableActionInfos = actionInfos

        val modifierInfos = getAllModifiers().fold(onSuccess = { modifierNames ->
            modifierNames.mapNotNull { modifierName -> getModifierInfo(modifierName).getOrNull() }
        }, onFailure = { emptyList() })
        availableModifierInfos = modifierInfos

        val reactionInfos = getAllReactions().fold(onSuccess = { reactionNames ->
            reactionNames.mapNotNull { reactionName -> getReactionInfo(reactionName).getOrNull() }
        }, onFailure = { emptyList() })
        availableReactionInfos = reactionInfos

        val completeWorkflow = workflowDetails ?: workflow

        val workflowActionParams = completeWorkflow.ActionParameters.associate {
            val parts = it.split("=", limit = 2)
            (parts.getOrNull(0) ?: "") to (parts.getOrNull(1) ?: "")
        }
        val workflowModifierParams = completeWorkflow.ModifierParameters.associate {
            val parts = it.split("=", limit = 2)
            (parts.getOrNull(0) ?: "") to (parts.getOrNull(1) ?: "")
        }
        val workflowReactionParams = completeWorkflow.ReactionParameters.associate {
            val parts = it.split("=", limit = 2)
            (parts.getOrNull(0) ?: "") to (parts.getOrNull(1) ?: "")
        }

        val selectedActionInfo = actionInfos.find { it.Name == completeWorkflow.ActionName }
        val selectedModifierInfo = modifierInfos.find { it.Name == completeWorkflow.ModifierName }
        val selectedReactionInfo = reactionInfos.find { it.Name == completeWorkflow.ReactionName }

        workflowState.value = WorkflowState(
            id = completeWorkflow.ID,
            name = completeWorkflow.Name,
            active = completeWorkflow.Active,
            selectedActionInfo = selectedActionInfo,
            actionParams = mutableStateOf(
                selectedActionInfo?.ParameterInfos?.map { paramInfo ->
                    ParameterValue(
                        paramInfo.Name,
                        paramInfo.PrettyName,
                        workflowActionParams[paramInfo.Name] ?: ""
                    )
                }?.toMutableList() ?: mutableListOf()
            ),
            selectedModifierInfo = selectedModifierInfo,
            modifierParams = mutableStateOf(
                selectedModifierInfo?.ParameterInfos?.map { paramInfo ->
                    ParameterValue(
                        paramInfo.Name,
                        paramInfo.PrettyName,
                        workflowModifierParams[paramInfo.Name] ?: ""
                    )
                }?.toMutableList() ?: mutableListOf()
            ),
            selectedReactionInfo = selectedReactionInfo,
            reactionParams = mutableStateOf(
                selectedReactionInfo?.ParameterInfos?.map { paramInfo ->
                    ParameterValue(
                        paramInfo.Name,
                        paramInfo.PrettyName,
                        workflowReactionParams[paramInfo.Name] ?: ""
                    )
                }?.toMutableList() ?: mutableListOf()
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (workflowState.value.id == null) "Create Workflow" else "Edit Workflow") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentStep == totalSteps - 1) { // Only show Save on the last step
                        TextButton(
                            onClick = {
                                if (loading) return@TextButton
                                loading = true
                                error = null
                                scope.launch {
                                    val actionName = workflowState.value.selectedActionInfo?.Name ?: return@launch
                                    val modifierName = workflowState.value.selectedModifierInfo?.Name ?: ""
                                    val reactionName = workflowState.value.selectedReactionInfo?.Name ?: return@launch

                                    val actionParamsList = workflowState.value.actionParams.value.map { "${it.technicalName}=${it.value}" }
                                    val modifierParamsList = workflowState.value.modifierParams.value.map { "${it.technicalName}=${it.value}" }
                                    val reactionParamsList = workflowState.value.reactionParams.value.map { "${it.technicalName}=${it.value}" }

                                    val res = if (workflowState.value.id == null) {
                                        createWorkflowApi(
                                            token,
                                            workflowState.value.name.ifBlank { "New Workflow" },
                                            actionName,
                                            actionParamsList.filter { it.isNotBlank() },
                                            modifierName,
                                            modifierParamsList.filter { it.isNotBlank() },
                                            reactionName,
                                            reactionParamsList.filter { it.isNotBlank() },
                                            workflowState.value.active
                                        )
                                    } else {
                                        updateWorkflowApi(
                                            token,
                                            workflowState.value.id!!,
                                            actionName,
                                            actionParamsList.filter { it.isNotBlank() },
                                            modifierName,
                                            modifierParamsList.filter { it.isNotBlank() },
                                            reactionName,
                                            reactionParamsList.filter { it.isNotBlank() },
                                            workflowState.value.active,
                                            workflowState.value.name
                                        )
                                    }
                                    loading = false
                                    res.fold(
                                        onSuccess = { wf -> onSaved(wf) },
                                        onFailure = { e ->
                                            val errorMsg = e.message ?: "Save failed"
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                            error = errorMsg
                                        }
                                    )
                                }
                            },
                            enabled = !loading
                        ) {
                            if (loading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Text("Save")
                            }
                        }
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = (currentStep + 1).toFloat() / totalSteps,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (currentStep) {
                    0 -> WorkflowNameAndActiveStep(workflowState.value) { updatedWorkflowState -> workflowState.value = updatedWorkflowState }
                    1 -> ActionSelectionStep(
                        workflowState.value,
                        availableActionInfos
                    ) { updatedWorkflowState -> workflowState.value = updatedWorkflowState }
                    2 -> ModifierSelectionStep(
                        workflowState.value,
                        availableModifierInfos
                    ) { updatedWorkflowState -> workflowState.value = updatedWorkflowState }
                    3 -> ReactionSelectionStep(
                        workflowState.value,
                        availableReactionInfos
                    ) { updatedWorkflowState -> workflowState.value = updatedWorkflowState }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentStep > 0) {
                        OutlinedButton(onClick = { currentStep-- }) {
                            Text("Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f)) // Push Next to the right
                    }

                    if (currentStep < totalSteps - 1) {
                        Button(onClick = { currentStep++ }) {
                            Text("Next")
                        }
                    }
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = error!!, color = ErrorRed)
                }
            }
        }
    )
}

@Composable
fun WorkflowNameAndActiveStep(
    workflowState: WorkflowState,
    onWorkflowStateChange: (WorkflowState) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Workflow Details",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        OutlinedTextField(
            value = workflowState.name,
            onValueChange = { onWorkflowStateChange(workflowState.copy(name = it)) },
            label = { Text("Workflow Name") },
            placeholder = { Text("Enter a descriptive name for your workflow") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Active",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = workflowState.active,
                onCheckedChange = { onWorkflowStateChange(workflowState.copy(active = it)) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionSelectionStep(
    workflowState: WorkflowState,
    availableActionInfos: List<ActionInfo>,
    onWorkflowStateChange: (WorkflowState) -> Unit
) {
    var actionExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Choose an Action (Trigger)",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        ExposedDropdownMenuBox(
            expanded = actionExpanded,
            onExpandedChange = { actionExpanded = !actionExpanded }
        ) {
            OutlinedTextField(
                value = workflowState.selectedActionInfo?.PrettyName ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Select Action") },
                placeholder = { Text("Choose a trigger action") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = actionExpanded,
                onDismissRequest = { actionExpanded = false }
            ) {
                availableActionInfos.forEach { actionInfo ->
                    DropdownMenuItem(
                        text = { Text(actionInfo.PrettyName) },
                        onClick = {
                            onWorkflowStateChange(workflowState.copy(selectedActionInfo = actionInfo, actionParams = mutableStateOf(actionInfo.ParameterInfos.map { ParameterValue(it.Name, it.PrettyName, "") })))
                            actionExpanded = false
                        }
                    )
                }
            }
        }

        workflowState.selectedActionInfo?.let { actionInfo ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = actionInfo.Description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            ParameterInputSection(
                parameters = workflowState.actionParams.value,
                onParametersChange = { updatedParams ->
                    workflowState.actionParams.value = updatedParams
                    onWorkflowStateChange(workflowState)
                },
                availableOutputs = workflowState.allAvailableOutputs.value // TODO: Populate this correctly
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifierSelectionStep(
    workflowState: WorkflowState,
    availableModifierInfos: List<ModifierInfo>,
    onWorkflowStateChange: (WorkflowState) -> Unit
) {
    var modifierExpanded by remember { mutableStateOf(false) }

    val combinedOutputs = remember(workflowState.selectedActionInfo, workflowState.selectedModifierInfo) {
        val outputs = mutableListOf<OutputInfo>()
        workflowState.selectedActionInfo?.OutputInfos?.let { outputs.addAll(it) }
        workflowState.selectedModifierInfo?.OutputInfos?.let { outputs.addAll(it) }
        workflowState.allAvailableOutputs.value = outputs
        outputs
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Add a Modifier (Optional)",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        ExposedDropdownMenuBox(
            expanded = modifierExpanded,
            onExpandedChange = { modifierExpanded = !modifierExpanded }
        ) {
            OutlinedTextField(
                value = workflowState.selectedModifierInfo?.PrettyName ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Select Modifier") },
                placeholder = { Text("Choose an optional modifier") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modifierExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = modifierExpanded,
                onDismissRequest = { modifierExpanded = false }
            ) {
                availableModifierInfos.forEach { modifierInfo ->
                    DropdownMenuItem(
                        text = { Text(modifierInfo.PrettyName) },
                        onClick = {
                            onWorkflowStateChange(workflowState.copy(selectedModifierInfo = modifierInfo, modifierParams = mutableStateOf(modifierInfo.ParameterInfos.map { ParameterValue(it.Name, it.PrettyName, "") })))
                            modifierExpanded = false
                        }
                    )
                }
            }
        }

        workflowState.selectedModifierInfo?.let { modifierInfo ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = modifierInfo.Description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            ParameterInputSection(
                parameters = workflowState.modifierParams.value,
                onParametersChange = { updatedParams ->
                    workflowState.modifierParams.value = updatedParams
                    onWorkflowStateChange(workflowState)
                },
                availableOutputs = workflowState.selectedActionInfo?.OutputInfos ?: emptyList()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionSelectionStep(
    workflowState: WorkflowState,
    availableReactionInfos: List<ReactionInfo>,
    onWorkflowStateChange: (WorkflowState) -> Unit
) {
    var reactionExpanded by remember { mutableStateOf(false) }

    val combinedOutputs = remember(workflowState.selectedActionInfo, workflowState.selectedModifierInfo) {
        val outputs = mutableListOf<OutputInfo>()
        workflowState.selectedActionInfo?.OutputInfos?.let { outputs.addAll(it) }
        workflowState.selectedModifierInfo?.OutputInfos?.let { outputs.addAll(it) }
        workflowState.allAvailableOutputs.value = outputs
        outputs
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Choose a Reaction",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        ExposedDropdownMenuBox(
            expanded = reactionExpanded,
            onExpandedChange = { reactionExpanded = !reactionExpanded }
        ) {
            OutlinedTextField(
                value = workflowState.selectedReactionInfo?.PrettyName ?: "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Select Reaction") },
                placeholder = { Text("Choose a reaction") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reactionExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = reactionExpanded,
                onDismissRequest = { reactionExpanded = false }
            ) {
                availableReactionInfos.forEach { reactionInfo ->
                    DropdownMenuItem(
                        text = { Text(reactionInfo.PrettyName) },
                        onClick = {
                            onWorkflowStateChange(workflowState.copy(selectedReactionInfo = reactionInfo, reactionParams = mutableStateOf(reactionInfo.ParameterInfos.map { ParameterValue(it.Name, it.PrettyName, "") })))
                            reactionExpanded = false
                        }
                    )
                }
            }
        }

        workflowState.selectedReactionInfo?.let { reactionInfo ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = reactionInfo.Description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            ParameterInputSection(
                parameters = workflowState.reactionParams.value,
                onParametersChange = { updatedParams ->
                    workflowState.reactionParams.value = updatedParams
                    onWorkflowStateChange(workflowState)
                },
                availableOutputs = combinedOutputs
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParameterInputSection(
    parameters: List<ParameterValue>,
    onParametersChange: (List<ParameterValue>) -> Unit,
    availableOutputs: List<OutputInfo>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Parameters",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (parameters.isEmpty()) {
            Text(
                text = "No parameters for this selection.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            parameters.forEachIndexed { index, param ->
                var outputExpanded by remember { mutableStateOf(false) }
                val displayValue = if (param.value.startsWith("#")) {
                    availableOutputs.find { it.Name == param.value.substring(1) }?.PrettyName ?: param.value
                } else {
                    param.value
                }
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    ExposedDropdownMenuBox(
                        expanded = outputExpanded,
                        onExpandedChange = { outputExpanded = !outputExpanded }
                    ) {
                        OutlinedTextField(
                            value = displayValue,
                            onValueChange = { newValue ->
                                val matchingOutput = availableOutputs.find { it.PrettyName == newValue }
                                val updatedParam = if (matchingOutput != null) {
                                    param.copy(value = "#${matchingOutput.Name}")
                                } else {
                                    param.copy(value = newValue)
                                }
                                onParametersChange(parameters.toMutableList().apply { this[index] = updatedParam })
                            },
                            label = { Text(param.prettyName.ifBlank { param.technicalName }) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            trailingIcon = {
                                Row {
                                    if (param.value.startsWith("#")) {
                                        IconButton(onClick = {
                                            val updatedParam = param.copy(value = "")
                                            onParametersChange(parameters.toMutableList().apply { this[index] = updatedParam })
                                        }) {
                                            Icon(
                                                Icons.Filled.Close,
                                                contentDescription = "Clear output selection"
                                            )
                                        }
                                    }
                                    if (availableOutputs.isNotEmpty()) {
                                        IconButton(onClick = { outputExpanded = !outputExpanded }) {
                                            Icon(
                                                if (outputExpanded) Icons.Filled.Close else Icons.Filled.ArrowDropDown,
                                                contentDescription = "Select output"
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        if (availableOutputs.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = outputExpanded,
                                onDismissRequest = { outputExpanded = false }
                            ) {
                                availableOutputs.forEach { output ->
                                    DropdownMenuItem(
                                        text = { Text(output.PrettyName) },
                                        onClick = {
                                            val updatedParam = param.copy(value = "#${output.Name}")
                                            onParametersChange(parameters.toMutableList().apply { this[index] = updatedParam })
                                            outputExpanded = false
                                        }
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
