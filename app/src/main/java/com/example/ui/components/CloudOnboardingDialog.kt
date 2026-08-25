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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.cloud.SyncStatus
import com.example.ui.theme.DeceasedVignetteRed
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary

@Composable
fun CloudOnboardingDialog(
    syncStatus: SyncStatus,
    syncMessage: String,
    onCreateAccount: (userId: String, password: String, vaultName: String) -> Unit,
    onSignIn: (userId: String, password: String) -> Unit,
    onSkipForNow: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = New Cloud Vault, 1 = Restore Existing
    var inputUserId by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var inputVaultName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { /* Force explicit decision */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 12.dp)
        ) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                backgroundColor = ObsidianBg.copy(alpha = 0.98f),
                borderColor = EmeraldGlow,
                elevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Futuristic Glowing Logo Emblem
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        EmeraldGlow.copy(alpha = 0.4f),
                                        NeonCyan.copy(alpha = 0.1f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .border(2.dp, EmeraldGlow, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ruh_tree_logo),
                            contentDescription = "Ruh Tree Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Welcome to Ruh Tree",
                        color = TextWhitePrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Google Cloud Real-Time Backup & Multi-Device Sync",
                        color = EmeraldGlow,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x1A10B981))
                            .border(1.dp, Color(0x3310B981), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🛡️ Connect your Google Cloud ID and password so all family members, high-resolution photos, and non-overlapping branches are saved in real time. If you install this app on another phone or tablet in the future, simply enter your credentials to open your exact tree instantly.",
                            color = TextMutedSecondary,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tab selector (New vs Restore)
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = GlassCardFill,
                        contentColor = EmeraldGlow,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = EmeraldGlow,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                validationError = null
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("New Cloud Vault", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                validationError = null
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restore from Cloud", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Form Inputs
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Google Cloud ID Input
                        GlassTextField(
                            value = inputUserId,
                            onValueChange = {
                                inputUserId = it
                                validationError = null
                            },
                            label = "Google Cloud ID / Account Name",
                            placeholder = "e.g. syedrhl62 or email",
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = EmeraldGlow)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "onboarding_cloud_id_input"
                        )

                        // Password Input
                        GlassTextField(
                            value = inputPassword,
                            onValueChange = {
                                inputPassword = it
                                validationError = null
                            },
                            label = "Vault Password (Encryption Key)",
                            placeholder = "Minimum 4 characters",
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = NeonCyan)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password Visibility",
                                        tint = TextMutedSecondary
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "onboarding_password_input"
                        )

                        // Vault Name (Only on Create tab)
                        AnimatedVisibility(visible = selectedTab == 0) {
                            GlassTextField(
                                value = inputVaultName,
                                onValueChange = { inputVaultName = it },
                                label = "Family Tree Title (Optional)",
                                placeholder = "e.g. Sterling Family Vault",
                                leadingIcon = {
                                    Icon(Icons.Default.Cloud, contentDescription = null, tint = NeonPurple)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "onboarding_vault_name_input"
                            )
                        }

                        // Validation or Sync Error Message
                        val currentError = validationError ?: (if (syncStatus == SyncStatus.ERROR) syncMessage else null)
                        if (currentError != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DeceasedVignetteRed.copy(alpha = 0.2f))
                                    .border(1.dp, DeceasedVignetteRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "⚠️ $currentError",
                                    color = DeceasedVignetteRed,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Syncing spinner status
                        if (syncStatus == SyncStatus.SYNCING) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = EmeraldGlow,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = syncMessage.ifBlank { "Connecting to Google Cloud Vault..." },
                                    color = EmeraldGlow,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Action Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = if (selectedTab == 0) listOf(EmeraldGlow, Color(0xFF059669))
                                    else listOf(NeonCyan, Color(0xFF0284C7))
                                )
                            )
                            .clickable(enabled = syncStatus != SyncStatus.SYNCING) {
                                if (inputUserId.trim().length < 3) {
                                    validationError = "Please enter a valid Google Cloud ID (at least 3 characters)"
                                    return@clickable
                                }
                                if (inputPassword.trim().length < 4) {
                                    validationError = "Password must be at least 4 characters long"
                                    return@clickable
                                }

                                if (selectedTab == 0) {
                                    onCreateAccount(inputUserId.trim(), inputPassword.trim(), inputVaultName.trim())
                                } else {
                                    onSignIn(inputUserId.trim(), inputPassword.trim())
                                }
                            }
                            .testTag("onboarding_primary_action_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.CloudUpload else Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = ObsidianBg,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (selectedTab == 0) "Create Cloud Vault & Auto-Save" else "Download & Open Tree from Cloud",
                                color = ObsidianBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary action: Explore demo tree
                    TextButton(
                        onClick = onSkipForNow,
                        modifier = Modifier.testTag("onboarding_skip_btn")
                    ) {
                        Text(
                            text = "Explore Demo Tree (Set Up Cloud Later)",
                            color = TextMutedSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
