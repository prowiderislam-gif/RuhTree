package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.security.SecurityManager
import com.example.ui.theme.DeceasedVignetteRed
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary

@Composable
fun SettingsDialog(
    securityManager: SecurityManager,
    onOpenCloudVault: () -> Unit,
    onResetToSampleTree: () -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var isPinEnabled by remember { mutableStateOf(securityManager.isPinEnabled()) }
    var currentPinInput by remember { mutableStateOf(securityManager.getPin()) }
    var pinSavedMessage by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
        ) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = ObsidianBg.copy(alpha = 0.95f),
                borderColor = EmeraldGlow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    // Header with Ruh Tree Logo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, EmeraldGlow, CircleShape)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ruh_tree_logo),
                                    contentDescription = "Ruh Tree Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ruh Tree Settings",
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Security & Cloud Management",
                                    color = TextMutedSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cloud Vault Button
                    FuturisticButton(
                        onClick = {
                            onDismiss()
                            onOpenCloudVault()
                        },
                        accentColor = EmeraldGlow,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "settings_open_cloud_vault_btn"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cloud, contentDescription = null, tint = EmeraldGlow, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Cloud Vault & Real-Time Sync", color = TextWhitePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("User ID & Password Backup", color = TextMutedSecondary, fontSize = 10.sp)
                                }
                            }
                            Text("Manage →", color = EmeraldGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN Protection Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x221E293B))
                            .border(1.dp, GlassCardBorder, RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Edit & Delete PIN Protection", color = TextWhitePrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text("Requires password before modifying saved members", color = TextMutedSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = {
                                isPinEnabled = it
                                securityManager.setPinEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhitePrimary,
                                checkedTrackColor = EmeraldGlow
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Change PIN field
                    if (isPinEnabled) {
                        GlassTextField(
                            value = currentPinInput,
                            onValueChange = {
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    currentPinInput = it
                                    if (it.length == 4) {
                                        securityManager.setPin(it)
                                        pinSavedMessage = true
                                    }
                                }
                            },
                            label = "Security PIN (4 digits)",
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldGlow)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "settings_pin_input"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Tree Management", color = TextWhitePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Reset to Sample Tree
                    FuturisticButton(
                        onClick = onResetToSampleTree,
                        accentColor = NeonCyan,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "reset_sample_tree_btn"
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Comprehensive Sample Tree", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Clear Full Tree
                    FuturisticButton(
                        onClick = onClearAll,
                        accentColor = DeceasedVignetteRed,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "clear_all_tree_btn"
                    ) {
                        Text("Clear All Members", fontSize = 12.sp, color = DeceasedVignetteRed)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        FuturisticButton(
                            onClick = onDismiss,
                            accentColor = EmeraldGlow,
                            testTag = "close_settings_btn"
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
