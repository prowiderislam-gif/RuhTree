package com.example.ui

import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.cloud.SyncStatus
import com.example.data.model.FamilyMember
import com.example.ui.components.CloudAccountDialog
import com.example.ui.components.CloudOnboardingDialog
import com.example.ui.components.GlassIconButton
import com.example.ui.components.GlassSurface
import com.example.ui.components.GlassTextField
import com.example.ui.components.KinshipCompareDialog
import com.example.ui.components.MemberDetailDialog
import com.example.ui.components.MemberFormDialog
import com.example.ui.components.PinAuthDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.tree.FamilyTreeCanvas
import com.example.ui.viewmodel.FamilyTreeViewModel
import com.example.util.DateUtils
import com.example.util.ImageExporter

@Composable
fun HomeScreen(
    viewModel: FamilyTreeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val members by viewModel.members.collectAsStateWithLifecycle()
    val spouses by viewModel.spouses.collectAsStateWithLifecycle()
    val filteredMembers by viewModel.filteredMembers.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val selectedMemberForDetail by viewModel.selectedMemberForDetail.collectAsStateWithLifecycle()
    val focusMemberId by viewModel.focusMemberId.collectAsStateWithLifecycle()
    val isFormOpen by viewModel.isFormDialogOpen.collectAsStateWithLifecycle()
    val editingMember by viewModel.editingMember.collectAsStateWithLifecycle()
    val suggestedFatherId by viewModel.suggestedFatherId.collectAsStateWithLifecycle()
    val suggestedMotherId by viewModel.suggestedMotherId.collectAsStateWithLifecycle()

    val isCompareOpen by viewModel.isCompareDialogOpen.collectAsStateWithLifecycle()
    val compareMemberAId by viewModel.compareMemberAId.collectAsStateWithLifecycle()
    val compareMemberBId by viewModel.compareMemberBId.collectAsStateWithLifecycle()

    val isPinAuthOpen by viewModel.isPinAuthDialogOpen.collectAsStateWithLifecycle()
    val pinAuthTitle by viewModel.pinAuthTitle.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsDialogOpen.collectAsStateWithLifecycle()

    val currentAccount by viewModel.currentAccount.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val syncMessage by viewModel.syncMessage.collectAsStateWithLifecycle()
    val lastSyncTimestamp by viewModel.lastSyncTimestamp.collectAsStateWithLifecycle()
    val isCloudDialogOpen by viewModel.isCloudDialogOpen.collectAsStateWithLifecycle()
    val isOnboardingOpen by viewModel.isOnboardingDialogOpen.collectAsStateWithLifecycle()

    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive 2D Generational Canvas with Auto-fit, Slide/Pan & Descendant Focus
            FamilyTreeCanvas(
                members = if (searchQuery.isBlank()) members else filteredMembers,
                spouses = spouses,
                focusMemberId = focusMemberId,
                onFocusMemberChange = { newFocusId -> viewModel.setFocusMember(newFocusId) },
                onOpenMemberDetail = { member -> viewModel.selectedMemberForDetail.value = member },
                onAddChild = { fId, mId -> viewModel.openAddMember(fatherId = fId, motherId = mId) },
                onAddSpouse = { _ ->
                    viewModel.openAddMember()
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top Glassmorphic App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .align(Alignment.TopCenter)
            ) {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = ObsidianBg.copy(alpha = 0.88f),
                    borderColor = EmeraldGlow.copy(alpha = 0.4f),
                    elevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // App Identity: Ruh Tree Logo & Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.isCloudDialogOpen.value = true }
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                            ) {
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Ruh Tree",
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        // Small cloud status pill
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (currentAccount != null) EmeraldGlow.copy(alpha = 0.2f)
                                                    else NeonCyan.copy(alpha = 0.15f)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (currentAccount != null) "☁️ @${currentAccount?.userId}" else "☁️ Vault",
                                                color = if (currentAccount != null) EmeraldGlow else NeonCyan,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${members.size} Members • ${spouses.size} Relations",
                                        color = TextMutedSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // Quick Action Tools
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Cloud Vault Button
                                GlassIconButton(
                                    onClick = { viewModel.isCloudDialogOpen.value = true },
                                    iconColor = if (currentAccount != null) EmeraldGlow else NeonCyan,
                                    modifier = Modifier.size(36.dp),
                                    testTag = "top_cloud_vault_btn"
                                ) {
                                    Icon(
                                        imageVector = if (syncStatus == SyncStatus.SYNCING) Icons.Default.Sync else Icons.Default.CloudDone,
                                        contentDescription = "Cloud Vault",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Search toggle
                                GlassIconButton(
                                    onClick = {
                                        isSearchExpanded = !isSearchExpanded
                                        if (!isSearchExpanded) viewModel.searchQuery.value = ""
                                    },
                                    iconColor = if (isSearchExpanded) NeonCyan else TextMutedSecondary,
                                    modifier = Modifier.size(36.dp),
                                    testTag = "toggle_search_btn"
                                ) {
                                    Icon(
                                        imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                        contentDescription = "Search",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Kinship Comparator Action
                                GlassIconButton(
                                    onClick = { viewModel.openCompare() },
                                    iconColor = NeonPurple,
                                    modifier = Modifier.size(36.dp),
                                    testTag = "top_compare_btn"
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CompareArrows,
                                        contentDescription = "Kinship Comparator",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                // Export Full Tree Overview Image Action
                                GlassIconButton(
                                    onClick = {
                                        val bitmap = Bitmap.createBitmap(900, 700, Bitmap.Config.ARGB_8888)
                                        val canvas = Canvas(bitmap)
                                        canvas.drawColor(android.graphics.Color.parseColor("#090D16"))
                                        val paint = android.graphics.Paint().apply {
                                            color = android.graphics.Color.WHITE
                                            textSize = 36f
                                            isAntiAlias = true
                                        }
                                        canvas.drawText("Ruh Tree - Family Architecture Overview", 40f, 70f, paint)
                                        paint.textSize = 24f
                                        paint.color = android.graphics.Color.parseColor("#10B981")
                                        canvas.drawText("Total Members: ${members.size} | Spouses: ${spouses.size} | Cloud Vault: @${currentAccount?.userId ?: "local"}", 40f, 120f, paint)

                                        var yOffset = 180f
                                        paint.color = android.graphics.Color.LTGRAY
                                        paint.textSize = 22f
                                        members.take(12).forEach { m ->
                                            val ageStr = DateUtils.formatAgeString(m.dateOfBirth, m.isDeceased, m.dateOfDeath)
                                            canvas.drawText("• ${m.name} (${m.gender}, $ageStr)${if (m.isDeceased) " [Deceased]" else ""}", 40f, yOffset, paint)
                                            yOffset += 40f
                                        }

                                        ImageExporter.saveBitmapToDevice(context, bitmap, "RuhTree_FullOverview")
                                    },
                                    iconColor = NeonCyan,
                                    modifier = Modifier.size(36.dp),
                                    testTag = "top_export_tree_btn"
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = "Save Tree Image",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Settings Action
                                GlassIconButton(
                                    onClick = { viewModel.isSettingsDialogOpen.value = true },
                                    iconColor = TextMutedSecondary,
                                    modifier = Modifier.size(36.dp),
                                    testTag = "top_settings_btn"
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Collapsible Search Field
                        AnimatedVisibility(visible = isSearchExpanded) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                GlassTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.searchQuery.value = it },
                                    label = "Search member name or bio...",
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = TextMutedSecondary,
                                                modifier = Modifier.clickable { viewModel.searchQuery.value = "" }
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "search_member_input"
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Right Floating Action Button (Add Root / Member)
            FloatingActionButton(
                onClick = { viewModel.openAddMember() },
                containerColor = EmeraldGlow,
                contentColor = ObsidianBg,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .size(60.dp)
                    .border(2.dp, EmeraldGlow.copy(alpha = 0.6f), CircleShape)
                    .testTag("fab_add_member")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Family Member",
                    modifier = Modifier.size(30.dp)
                )
            }

            // Bottom Left Floating Action Button (Quick Kinship Comparator)
            FloatingActionButton(
                onClick = { viewModel.openCompare() },
                containerColor = NeonPurple,
                contentColor = TextWhitePrimary,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .size(52.dp)
                    .border(1.5.dp, NeonPurple.copy(alpha = 0.6f), CircleShape)
                    .testTag("fab_compare_kinship")
            ) {
                Icon(
                    imageVector = Icons.Default.CompareArrows,
                    contentDescription = "Kinship Comparator",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // ==========================================
    // MODAL DIALOGS
    // ==========================================

    // 1. Member Dossier Detail Dialog
    selectedMemberForDetail?.let { member ->
        MemberDetailDialog(
            member = member,
            allMembers = members,
            spouses = spouses,
            onEditRequest = { viewModel.requestEditMember(member) },
            onDeleteRequest = { viewModel.requestDeleteMember(member) },
            onAddSuccessor = { fId, mId ->
                viewModel.selectedMemberForDetail.value = null
                viewModel.openAddMember(fatherId = fId, motherId = mId)
            },
            onAddPredecessor = { _ ->
                viewModel.selectedMemberForDetail.value = null
                viewModel.openAddMember()
            },
            onAddSpouse = { _ ->
                viewModel.selectedMemberForDetail.value = null
                viewModel.openAddMember()
            },
            onCompareWith = { otherMemberId ->
                viewModel.selectedMemberForDetail.value = null
                viewModel.openCompare(memberAId = member.id, memberBId = otherMemberId)
            },
            onSelectMember = { targetMemberId ->
                viewModel.selectedMemberForDetail.value = members.firstOrNull { it.id == targetMemberId }
            },
            onFocusAsMain = { targetMemberId ->
                viewModel.setFocusMember(targetMemberId)
            },
            onDismiss = { viewModel.selectedMemberForDetail.value = null }
        )
    }

    // 2. Add / Edit Member Form Dialog
    if (isFormOpen) {
        MemberFormDialog(
            initialMember = editingMember,
            allMembers = members,
            suggestedFatherId = suggestedFatherId,
            suggestedMotherId = suggestedMotherId,
            onSave = { updatedMember, spouseIds, _ ->
                viewModel.saveMember(updatedMember, spouseIds)
            },
            onDismiss = {
                viewModel.isFormDialogOpen.value = false
                viewModel.editingMember.value = null
            }
        )
    }

    // 3. Kinship & Age Comparison Dialog
    if (isCompareOpen) {
        KinshipCompareDialog(
            allMembers = members,
            spouses = spouses,
            initialMemberAId = compareMemberAId,
            initialMemberBId = compareMemberBId,
            onDismiss = { viewModel.isCompareDialogOpen.value = false }
        )
    }

    // 4. Security PIN Auth Dialog
    if (isPinAuthOpen) {
        PinAuthDialog(
            actionTitle = pinAuthTitle,
            expectedPin = viewModel.securityManager.getPin(),
            onPinSuccess = { viewModel.onPinSuccess() },
            onDismiss = { viewModel.isPinAuthDialogOpen.value = false }
        )
    }

    // 5. Settings & Tree Management Dialog
    if (isSettingsOpen) {
        SettingsDialog(
            securityManager = viewModel.securityManager,
            onOpenCloudVault = { viewModel.isCloudDialogOpen.value = true },
            onResetToSampleTree = {
                viewModel.seedSampleFamilyTree()
                viewModel.isSettingsDialogOpen.value = false
            },
            onClearAll = {
                viewModel.clearAllTree()
                viewModel.isSettingsDialogOpen.value = false
            },
            onDismiss = { viewModel.isSettingsDialogOpen.value = false }
        )
    }

    // 6. Cloud Vault & Account Backup Dialog
    if (isCloudDialogOpen) {
        CloudAccountDialog(
            currentAccount = currentAccount,
            syncStatus = syncStatus,
            syncMessage = syncMessage,
            lastSyncTimestamp = lastSyncTimestamp,
            membersCount = members.size,
            spousesCount = spouses.size,
            onCreateAccount = { userId, password, vaultName ->
                viewModel.createVaultAccount(userId, password, vaultName)
            },
            onSignIn = { userId, password ->
                viewModel.signInAndRestoreVault(userId, password)
            },
            onSyncNow = { viewModel.syncToCloudNow() },
            onRestoreFromCloud = { viewModel.downloadAndRestoreFromActiveCloud() },
            onLogout = { viewModel.logoutAccount() },
            onDismiss = { viewModel.isCloudDialogOpen.value = false }
        )
    }

    // 7. First-Time Google Cloud Install Setup / Onboarding Dialog
    if (isOnboardingOpen) {
        CloudOnboardingDialog(
            syncStatus = syncStatus,
            syncMessage = syncMessage,
            onCreateAccount = { userId, password, vaultName ->
                viewModel.createVaultAccount(userId, password, vaultName)
            },
            onSignIn = { userId, password ->
                viewModel.signInAndRestoreVault(userId, password)
            },
            onSkipForNow = {
                viewModel.dismissOnboarding()
            }
        )
    }
}
