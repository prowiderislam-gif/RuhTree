package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.cloud.SyncStatus
import com.example.data.cloud.UserAccount
import com.example.ui.theme.DeceasedVignetteRed
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CloudAccountDialog(
    currentAccount: UserAccount?,
    syncStatus: SyncStatus,
    syncMessage: String,
    lastSyncTimestamp: Long,
    membersCount: Int,
    spousesCount: Int,
    onCreateAccount: (userId: String, password: String, vaultName: String) -> Unit,
    onSignIn: (userId: String, password: String) -> Unit,
    onSyncNow: () -> Unit,
    onRestoreFromCloud: () -> Unit,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(if (currentAccount != null) 0 else 0) }
    var inputUserId by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var inputVaultName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
        ) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = ObsidianBg.copy(alpha = 0.96f),
                borderColor = EmeraldGlow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Logo Emblem
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, EmeraldGlow, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ruh_tree_logo),
                                    contentDescription = "Ruh Tree Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ruh Tree Cloud Vault",
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Secure ID & Password Cloud Backup",
                                    color = EmeraldGlow,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentAccount != null) {
                        // LOGGED IN VIEW: Active Vault Management
                        ActiveVaultView(
                            account = currentAccount,
                            syncStatus = syncStatus,
                            syncMessage = syncMessage,
                            lastSyncTimestamp = lastSyncTimestamp,
                            membersCount = membersCount,
                            spousesCount = spousesCount,
                            onSyncNow = onSyncNow,
                            onRestoreFromCloud = onRestoreFromCloud,
                            onLogout = onLogout
                        )
                    } else {
                        // NOT LOGGED IN: Sign In or Create Vault Tabs
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color(0x221E293B),
                            contentColor = EmeraldGlow,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = EmeraldGlow
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0; errorMessage = null },
                                text = {
                                    Text(
                                        "Sign In & Restore",
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (selectedTab == 0) EmeraldGlow else TextMutedSecondary
                                    )
                                }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1; errorMessage = null },
                                text = {
                                    Text(
                                        "Create New Vault",
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (selectedTab == 1) EmeraldGlow else TextMutedSecondary
                                    )
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Info security notice
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x1A10B981))
                                .border(1.dp, EmeraldGlow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = EmeraldGlow,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Your family tree is strictly isolated to your Unique ID & Password. No other user can access your data, and your backups persist across devices.",
                                color = TextWhitePrimary.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (selectedTab == 1) {
                            // Vault Name (optional for creation)
                            GlassTextField(
                                value = inputVaultName,
                                onValueChange = { inputVaultName = it },
                                label = "Family Vault Name (e.g. Ruh Family)",
                                leadingIcon = {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = EmeraldGlow)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "input_vault_name"
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // User ID input
                        GlassTextField(
                            value = inputUserId,
                            onValueChange = { inputUserId = it.trim().lowercase() },
                            label = "Unique User ID (e.g. syedrhl62)",
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldGlow)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "input_user_id"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Password input
                        GlassTextField(
                            value = inputPassword,
                            onValueChange = { inputPassword = it },
                            label = "Secret Password",
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldGlow)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password",
                                        tint = TextMutedSecondary
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "input_user_password"
                        )

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                color = DeceasedVignetteRed,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (selectedTab == 0) {
                            // Sign In / Restore Button
                            FuturisticButton(
                                onClick = {
                                    if (inputUserId.isBlank() || inputPassword.isBlank()) {
                                        errorMessage = "Please enter both User ID and Password"
                                    } else {
                                        errorMessage = null
                                        onSignIn(inputUserId, inputPassword)
                                    }
                                },
                                accentColor = EmeraldGlow,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "signin_vault_btn"
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign In & Restore My Tree", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Create Vault Account Button
                            FuturisticButton(
                                onClick = {
                                    if (inputUserId.length < 3) {
                                        errorMessage = "User ID must be at least 3 characters"
                                    } else if (inputPassword.length < 4) {
                                        errorMessage = "Password must be at least 4 characters"
                                    } else {
                                        errorMessage = null
                                        onCreateAccount(
                                            inputUserId,
                                            inputPassword,
                                            if (inputVaultName.isNotBlank()) inputVaultName else "Ruh Tree ($inputUserId)"
                                        )
                                    }
                                },
                                accentColor = EmeraldGlow,
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "create_vault_btn"
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Create Vault & Start Real-time Backup", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveVaultView(
    account: UserAccount,
    syncStatus: SyncStatus,
    syncMessage: String,
    lastSyncTimestamp: Long,
    membersCount: Int,
    spousesCount: Int,
    onSyncNow: () -> Unit,
    onRestoreFromCloud: () -> Unit,
    onLogout: () -> Unit
) {
    val formattedTime = remember(lastSyncTimestamp) {
        if (lastSyncTimestamp > 0) {
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            sdf.format(Date(lastSyncTimestamp))
        } else {
            "Not synced yet"
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Vault Account Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x2210B981), Color(0x1106B6D4))
                    )
                )
                .border(1.dp, EmeraldGlow.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(EmeraldGlow.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = EmeraldGlow,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName.ifBlank { "Ruh Tree Vault" },
                    color = TextWhitePrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "ID: @${account.userId}",
                    color = EmeraldGlow,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Real-time Cloud Sync Active",
                    color = TextMutedSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Status & Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x221E293B))
                .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Last Cloud Backup", color = TextMutedSecondary, fontSize = 11.sp)
                Text(formattedTime, color = TextWhitePrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Stored in Tree", color = TextMutedSecondary, fontSize = 11.sp)
                Text("$membersCount Members • $spousesCount Marriages", color = EmeraldGlow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (syncMessage.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x1510B981))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (syncStatus == SyncStatus.SYNCING) {
                    CircularProgressIndicator(
                        color = EmeraldGlow,
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = syncMessage,
                    color = TextWhitePrimary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Backup to Cloud Now Button
        FuturisticButton(
            onClick = onSyncNow,
            accentColor = EmeraldGlow,
            modifier = Modifier.fillMaxWidth(),
            testTag = "force_sync_btn"
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sync & Backup to Cloud Now", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Download / Restore from Cloud Button
        FuturisticButton(
            onClick = onRestoreFromCloud,
            accentColor = NeonCyan,
            modifier = Modifier.fillMaxWidth(),
            testTag = "restore_cloud_btn"
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download & Restore from Cloud", fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Switch Account / Logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Switch or Disconnect",
                color = TextMutedSecondary,
                fontSize = 12.sp
            )

            FuturisticButton(
                onClick = onLogout,
                accentColor = DeceasedVignetteRed,
                testTag = "logout_vault_btn"
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp), tint = DeceasedVignetteRed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Out", fontSize = 12.sp, color = DeceasedVignetteRed)
                }
            }
        }
    }
}
