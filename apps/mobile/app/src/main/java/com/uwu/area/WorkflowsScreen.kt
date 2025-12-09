package com.uwu.area

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Workflow(val id: Int, val name: String)

@Composable
fun WorkflowsScreen() {
    val mockWorkflows = remember {
        listOf(
            Workflow(1, "My first workflow"),
            Workflow(2, "GitHub to Discord alert")
        )
    }
    var selectedId by remember { mutableStateOf(mockWorkflows.firstOrNull()?.id) }

    val selectedWorkflow = mockWorkflows.find { it.id == selectedId }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .width(200.dp)
                .fillMaxHeight()
                .background(Color(0xFFF9FAFB))
                .padding(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3F4F6))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Workflows",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF374151)
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                mockWorkflows.forEach { workflow ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedId = workflow.id }
                            .background(
                                if (workflow.id == selectedId) Color(0xFF2563EB) else Color.Transparent
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = workflow.name,
                            fontSize = 14.sp,
                            color = if (workflow.id == selectedId) Color.White else Color(0xFF374151)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = selectedWorkflow?.name ?: "No workflow selected",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .shadow(2.dp, RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Main workflow content goes here",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        }
    }
}

