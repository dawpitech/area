package com.uwu.area

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

private fun JSONObject.getWorkflowId(): Int {
    return optInt("ID", optInt("WorkflowID", 0))
}

suspend fun fetchWorkflows(token: String? = null): Result<List<Workflow>> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.WORKFLOWS)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "GET ${ApiRoutes.WORKFLOWS} -> code=$code resp=$respText")

            if (code in 200..299) {
                val list = mutableListOf<Workflow>()
                if (respText.trim() != "null" && respText.trim() != "") {
                    try {
                        val arr = JSONArray(respText)
                        Log.d("WorkflowApi", "Parsing ${arr.length()} workflows from: $respText")
                        for (i in 0 until arr.length()) {
                            try {
                                val jo = arr.getJSONObject(i)
                                val workflowId = jo.getWorkflowId()
                                val workflowName = jo.optString("Name", "")
                                val workflowActive = jo.optBoolean("Active", false)
                                Log.d("WorkflowApi", "Parsed workflow $i: ID=$workflowId, Name='$workflowName', Active=$workflowActive")
                                val wp = Workflow(
                                    ID = workflowId,
                                    Name = workflowName,
                                    ActionName = "", // Will be loaded on demand
                                    ActionParameters = emptyList(),
                                    ReactionName = "", // Will be loaded on demand
                                    ReactionParameters = emptyList(),
                                    Active = workflowActive
                                )
                                list.add(wp)
                            } catch (e: Exception) {
                                Log.e("WorkflowApi", "Failed to parse workflow $i", e)
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("WorkflowApi", "Failed to parse workflow array: $respText", e)
                    }
                }
                Result.success(list)
            } else {
                Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("WorkflowApi", "fetchWorkflows error", e)
            Result.failure(e)
        }
    }
}

suspend fun createWorkflowApi(
    token: String? = null,
    workflowName: String,
    actionName: String,
    actionParams: List<String>,
    reactionName: String,
    reactionParams: List<String>,
    active: Boolean = true
): Result<Workflow> {
    return withContext(Dispatchers.IO) {
        try {
            val createUrl = URL(ApiRoutes.BASE + ApiRoutes.WORKFLOWS)
            val createConn = (createUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            val createBody = JSONObject().apply {
                put("Active", active)
            }.toString()

            createConn.outputStream.use { os: OutputStream ->
                os.write(createBody.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val createCode = createConn.responseCode
            val createReader = if (createCode in 200..299) BufferedReader(InputStreamReader(createConn.inputStream))
            else BufferedReader(InputStreamReader(createConn.errorStream ?: createConn.inputStream))

            val createRespText = createReader.use { it.readText() }
            Log.d("WorkflowApi", "POST ${ApiRoutes.WORKFLOWS} -> code=$createCode resp=$createRespText")

            if (createCode !in 200..299) {
                return@withContext Result.failure(Exception(createRespText))
            }

            val createJo = JSONObject(createRespText)
            val workflowId = createJo.getWorkflowId()
            Log.d("WorkflowApi", "Created workflow with ID: $workflowId")
            if (workflowId == -1) {
                return@withContext Result.failure(Exception("Failed to get workflow ID from create response"))
            }

            val name = createJo.optString("Name", "")
            val actionNameFromResponse = createJo.optString("ActionName", "")
            val reactionNameFromResponse = createJo.optString("ReactionName", "")

            val createdWorkflow = Workflow(
                ID = workflowId,
                Name = name,
                ActionName = actionNameFromResponse,
                ActionParameters = emptyList(), // Will be updated
                ReactionName = reactionNameFromResponse,
                ReactionParameters = emptyList(), // Will be updated
                Active = active
            )

            val checkResult = checkWorkflowApi(actionName, actionParams, reactionName, reactionParams)
            if (checkResult.isFailure) {
                return@withContext Result.failure(checkResult.exceptionOrNull() ?: Exception("Workflow check failed"))
            }

            Log.d("WorkflowApi", "Updating workflow $workflowId with name='$workflowName', action='$actionName', reaction='$reactionName'")
            val updateResult = updateWorkflowApi(token, workflowId, actionName, actionParams, reactionName, reactionParams, active, workflowName)
            updateResult.fold(
                onSuccess = { updatedWorkflow ->
                    Result.success(updatedWorkflow)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e("WorkflowApi", "createWorkflowApi error", e)
            Result.failure(e)
        }
    }
}

suspend fun deleteWorkflowApi(token: String? = null, id: Int?): Result<Unit> {
    return withContext(Dispatchers.IO) {
        try {
            if (id == null) {
                return@withContext Result.failure(Exception("Workflow ID is null"))
            }
            Log.d("WorkflowApi", "DELETE attempt: token=${token?.take(10)}..., id=$id")
            val url = URL(ApiRoutes.BASE + ApiRoutes.WORKFLOWS + "$id")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                if (!token.isNullOrBlank()) {
                    val authHeader = "Bearer $token"
                    setRequestProperty("Authorization", authHeader)
                    Log.d("WorkflowApi", "DELETE setting Authorization: ${authHeader.take(20)}...")
                } else {
                    Log.d("WorkflowApi", "DELETE no token provided")
                }
                connectTimeout = 5000
                readTimeout = 5000
            }
            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "DELETE ${ApiRoutes.WORKFLOWS}$id -> code=$code resp=$respText")

            if (code in 200..299) Result.success(Unit)
            else Result.failure(Exception(respText.ifBlank { "Delete failed" }))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

suspend fun updateWorkflowApi(
    token: String? = null,
    id: Int,
    actionName: String,
    actionParams: List<String>,
    reactionName: String,
    reactionParams: List<String>,
    active: Boolean = true,
    name: String = ""
): Result<Workflow> {
    Log.d("WorkflowApi", "updateWorkflowApi called with id=$id, name='$name', action='$actionName', reaction='$reactionName'")
    return withContext(Dispatchers.IO) {
        try {
            val checkResult = checkWorkflowApi(actionName, actionParams, reactionName, reactionParams)
            if (checkResult.isFailure) {
                return@withContext Result.failure(checkResult.exceptionOrNull() ?: Exception("Workflow check failed"))
            }

            val url = URL(ApiRoutes.BASE + ApiRoutes.WORKFLOWS + "$id")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            val body = JSONObject().apply {
                put("ActionName", actionName)
                put("ActionParameters", listToJSONObject(actionParams))
                put("ReactionName", reactionName)
                put("ReactionParameters", listToJSONObject(reactionParams))
                put("Active", active)
                if (name.isNotBlank()) {
                    put("Name", name)
                }
            }.toString()

            conn.outputStream.use { os: OutputStream ->
                os.write(body.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "PATCH ${ApiRoutes.WORKFLOWS}/$id -> code=$code body=$body resp=$respText")

            if (code in 200..299) {
                val jo = JSONObject(respText)
                val actionParams = jsonObjectToList(jo.optJSONObject("ActionParameters"))
                val reactionParams = jsonObjectToList(jo.optJSONObject("ReactionParameters"))
                val wf = Workflow(
                    ID = jo.getWorkflowId(),
                    Name = jo.optString("Name", ""),
                    ActionName = jo.optString("ActionName", actionName),
                    ActionParameters = actionParams,
                    ReactionName = jo.optString("ReactionName", ""),
                    ReactionParameters = reactionParams,
                    Active = jo.optBoolean("Active", true)
                )
                Result.success(wf)
            } else {
                Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun jsonArrayToList(arr: JSONArray?): List<String> {
    if (arr == null) return emptyList()
    val out = mutableListOf<String>()
    for (i in 0 until arr.length()) {
        try { out.add(arr.optString(i, "")) } catch (_: Exception) {}
    }
    return out
}

private fun jsonObjectToList(obj: JSONObject?): List<String> {
    if (obj == null) return emptyList()
    val out = mutableListOf<String>()
    val keys = obj.keys()
    while (keys.hasNext()) {
        val key = keys.next()
        try {
            val value = obj.optString(key, "")
            if (value.isNotBlank()) {
                out.add("$key=$value")
            }
        } catch (_: Exception) {}
    }
    return out
}

private fun listToJSONObject(params: List<String>): JSONObject {
    val obj = JSONObject()
    params.forEach { param ->
        val parts = param.split("=", limit = 2)
        if (parts.size == 2) {
            obj.put(parts[0], parts[1])
        }
    }
    return obj
}

suspend fun checkWorkflowApi(
    actionName: String,
    actionParams: List<String>,
    reactionName: String,
    reactionParams: List<String>
): Result<String> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.WORKFLOW_CHECK)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            val body = JSONObject().apply {
                put("ActionName", actionName)
                put("ActionParameters", listToJSONObject(actionParams))
                put("ReactionName", reactionName)
                put("ReactionParameters", listToJSONObject(reactionParams))
            }.toString()

            conn.outputStream.use { os: OutputStream ->
                os.write(body.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "POST ${ApiRoutes.WORKFLOW_CHECK} -> code=$code resp=$respText")

            if (code in 200..299) {
                try {
                    val jo = JSONObject(respText)
                    val syntaxValid = jo.optBoolean("SyntaxValid", false)
                    val error = jo.optString("Error", "")

                    if (syntaxValid) {
                        Result.success(respText)
                    } else {
                        Result.failure(Exception(error.ifBlank { "Workflow syntax validation failed" }))
                    }
                } catch (e: Exception) {
                    Result.failure(Exception("Invalid response format from workflow check"))
                }
            } else {
                Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("WorkflowApi", "checkWorkflowApi error", e)
            Result.failure(e)
        }
    }
}

suspend fun getWorkflowDetails(token: String? = null, workflowId: Int): Result<Workflow> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.WORKFLOWS + "$workflowId")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "GET ${ApiRoutes.WORKFLOWS}$workflowId -> code=$code resp=$respText")

            if (code in 200..299) {
                val jo = JSONObject(respText)
                val workflowId = jo.getWorkflowId()
                val workflowName = jo.optString("Name", "")
                val actionName = jo.optString("ActionName", "")
                val reactionName = jo.optString("ReactionName", "")
                val active = jo.optBoolean("Active", false)
                Log.d("WorkflowApi", "getWorkflowDetails for ID $workflowId returned: Name='$workflowName', Action='$actionName', Reaction='$reactionName', Active=$active")

                val actionParams = jsonObjectToList(jo.optJSONObject("ActionParameters"))
                val reactionParams = jsonObjectToList(jo.optJSONObject("ReactionParameters"))
                val workflow = Workflow(
                    ID = workflowId,
                    Name = workflowName,
                    ActionName = actionName,
                    ActionParameters = actionParams,
                    ReactionName = reactionName,
                    ReactionParameters = reactionParams,
                    Active = active
                )
                Result.success(workflow)
            } else {
                Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("WorkflowApi", "getWorkflowDetails error", e)
            Result.failure(e)
        }
    }
}

suspend fun getAllActions(): Result<List<String>> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.ACTIONS)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "GET ${ApiRoutes.ACTIONS} -> code=$code resp=$respText")

            if (code in 200..299) {
                val jo = JSONObject(respText)
                val arr = jo.optJSONArray("ActionsName")
                if (arr != null) {
                    val actions = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        actions.add(arr.optString(i, ""))
                    }
                    Result.success(actions)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("WorkflowApi", "getAllActions error", e)
            Result.failure(e)
        }
    }
}

suspend fun getAllReactions(): Result<List<String>> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.REACTIONS)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "GET ${ApiRoutes.REACTIONS} -> code=$code resp=$respText")

            if (code in 200..299) {
                val jo = JSONObject(respText)
                val arr = jo.optJSONArray("ReactionsName")
                if (arr != null) {
                    val reactions = mutableListOf<String>()
                    for (i in 0 until arr.length()) {
                        reactions.add(arr.optString(i, ""))
                    }
                    Result.success(reactions)
                } else {
                    Result.success(emptyList())
                }
            } else {
                Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("WorkflowApi", "getAllReactions error", e)
            Result.failure(e)
        }
    }
}

data class ActionInfo(
    val Name: String,
    val PrettyName: String,
    val Description: String,
    val Parameters: List<String>
)

data class ReactionInfo(
    val Name: String,
    val PrettyName: String,
    val Description: String,
    val Parameters: List<String>
)

data class LogEntry(
    val Message: String,
    val Timestamp: String,
    val Type: String
)

suspend fun getActionInfo(actionName: String): Result<ActionInfo> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.ACTIONS + actionName)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "GET ${ApiRoutes.ACTIONS}$actionName -> code=$code resp=$respText")

            if (code in 200..299) {
                val jo = JSONObject(respText)
                val actionInfo = ActionInfo(
                    jo.optString("Name", ""),
                    jo.optString("PrettyName", ""),
                    jo.optString("Description", ""),
                    jsonArrayToList(jo.optJSONArray("Parameters"))
                )
                Result.success(actionInfo)
            } else {
                Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("WorkflowApi", "getActionInfo error", e)
            Result.failure(e)
        }
    }
}

suspend fun getReactionInfo(reactionName: String): Result<ReactionInfo> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.REACTIONS + reactionName)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "GET ${ApiRoutes.REACTIONS}$reactionName -> code=$code resp=$respText")

            if (code in 200..299) {
                val jo = JSONObject(respText)
                val reactionInfo = ReactionInfo(
                    jo.optString("Name", ""),
                    jo.optString("PrettyName", ""),
                    jo.optString("Description", ""),
                    jsonArrayToList(jo.optJSONArray("Parameters"))
                )
                Result.success(reactionInfo)
            } else {
                Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("WorkflowApi", "getReactionInfo error", e)
            Result.failure(e)
        }
    }
}

suspend fun fetchWorkflowLogs(token: String? = null, workflowId: Int): Result<List<LogEntry>> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.WORKFLOW_LOGS + workflowId)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 5000
                readTimeout = 5000
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "GET ${ApiRoutes.WORKFLOW_LOGS}$workflowId -> code=$code resp=$respText")

            if (code in 200..299) {
                val logs = mutableListOf<LogEntry>()
                if (respText.trim() != "null" && respText.trim() != "") {
                    try {
                        val jo = JSONObject(respText)
                        val arr = jo.optJSONArray("Logs")
                        if (arr != null) {
                            Log.d("WorkflowApi", "Parsing ${arr.length()} logs from: $respText")
                            for (i in 0 until arr.length()) {
                                try {
                                    val logJo = arr.getJSONObject(i)
                                    val log = LogEntry(
                                        Message = logJo.optString("Message", ""),
                                        Timestamp = logJo.optString("Timestamp", ""),
                                        Type = logJo.optString("Type", "")
                                    )
                                    logs.add(log)
                                } catch (e: Exception) {
                                    Log.e("WorkflowApi", "Failed to parse log $i", e)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("WorkflowApi", "Failed to parse logs response: $respText", e)
                    }
                }
                Result.success(logs)
            } else {
                Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("WorkflowApi", "fetchWorkflowLogs error", e)
            Result.failure(e)
        }
    }
}
