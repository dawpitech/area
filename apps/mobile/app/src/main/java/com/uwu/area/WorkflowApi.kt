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
                val arr = JSONArray(respText)
                val list = mutableListOf<Workflow>()
                for (i in 0 until arr.length()) {
                    try {
                        val jo = arr.getJSONObject(i)
                        val wp = Workflow(
                            jo.optInt("ID"),
                            jo.optString("ActionName", "")
                        )
                        list.add(wp)
                    } catch (_: Exception) { }
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
    actionName: String,
    actionParams: List<String>,
    reactionName: String,
    reactionParams: List<String>
): Result<Workflow> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.WORKFLOWS)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            val body = JSONObject().apply {
                put("ActionName", actionName)
                put("ActionParameters", JSONArray(actionParams))
                put("ReactionName", reactionName)
                put("ReactionParameters", JSONArray(reactionParams))
            }.toString()

            conn.outputStream.use { os: OutputStream ->
                os.write(body.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            Log.d("WorkflowApi", "POST ${ApiRoutes.WORKFLOWS} -> code=$code resp=$respText")

            if (code in 200..299) {
                val jo = JSONObject(respText)
                val wf = Workflow(
                    jo.optInt("ID"),
                    jo.optString("ActionName", actionName)
                )
                Result.success(wf)
            } else {
                Result.failure(Exception(respText))
            }
        } catch (e: Exception) {
            Log.e("WorkflowApi", "createWorkflowApi error", e)
            Result.failure(e)
        }
    }
}

suspend fun deleteWorkflowApi(token: String? = null, id: Int?): Result<Unit> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.WORKFLOWS + "/$id")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
                connectTimeout = 5000
                readTimeout = 5000
            }
            val code = conn.responseCode
            if (code in 200..299) Result.success(Unit)
            else {
                val resp = conn.errorStream?.let { BufferedReader(InputStreamReader(it)).use { r -> r.readText() } } ?: "Delete failed"
                Result.failure(Exception(resp))
            }
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
    reactionParams: List<String>
): Result<Workflow> {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(ApiRoutes.BASE + ApiRoutes.WORKFLOWS + "/$id")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                if (!token.isNullOrBlank()) setRequestProperty("Authorization", "Bearer $token")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }

            val body = JSONObject().apply {
                put("ActionName", actionName)
                put("ActionParameters", JSONArray(actionParams))
                put("ReactionName", reactionName)
                put("ReactionParameters", JSONArray(reactionParams))
            }.toString()

            conn.outputStream.use { os: OutputStream ->
                os.write(body.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val code = conn.responseCode
            val reader = if (code in 200..299) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream ?: conn.inputStream))

            val respText = reader.use { it.readText() }
            if (code in 200..299) {
                val jo = JSONObject(respText)
                val wf = Workflow(
                    jo.optInt("ID"),
                    jo.optString("ActionName", actionName)
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
