package com.uwu.area

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uwu.area.ui.theme.*
import com.uwu.area.R

enum class Screen {
    HOME, WORKFLOWS, SETTINGS
}

@Composable
fun NavigationBar(
    currentScreen: Screen,
    email: String?,
    onMenuClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Blue2734bd,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(48.dp) // Ensure minimum touch target for accessibility
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(R.string.cd_menu),
                    tint = White
                )
            }

            if (email != null) {
                Text(
                    text = stringResource(R.string.home_logged_in_as, email),
                    fontSize = 12.sp,
                    color = White
                )
            }
        }
    }
}

