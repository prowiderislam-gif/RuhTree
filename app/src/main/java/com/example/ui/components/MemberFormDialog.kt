package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Transgender
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.data.model.SpouseRelation
import com.example.ui.theme.DeceasedVignetteRed
import com.example.ui.theme.DivorcedVignetteGrey
import com.example.ui.theme.FemalePink
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.GlassCardFill
import com.example.ui.theme.MaleBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberFormDialog(
    initialMember: FamilyMember? = null,
    allMembers: List<FamilyMember> = emptyList(),
    suggestedFatherId: Long? = null,
    suggestedMotherId: Long? = null,
    onSave: (FamilyMember, List<Long>, List<Boolean>) -> Unit, // member, spouseIds, spouseIsDivorcedList
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialMember?.name ?: "") }
    var photoUri by remember { mutableStateOf(initialMember?.photoUri) }
    var dateOfBirth by remember { mutableStateOf(initialMember?.dateOfBirth ?: "") }
    var timeOfBirth by remember { mutableStateOf(initialMember?.timeOfBirth ?: "") }
    var gender by remember { mutableStateOf(initialMember?.gender ?: Gender.MALE.name) }
    var description by remember { mutableStateOf(initialMember?.description ?: "") }
    var isDeceased by remember { mutableStateOf(initialMember?.isDeceased ?: false) }
    var dateOfDeath by remember { mutableStateOf(initialMember?.dateOfDeath ?: "") }
    var isDivorced by remember { mutableStateOf(initialMember?.isDivorced ?: false) }
    var fatherId by remember { mutableStateOf(initialMember?.fatherId ?: suggestedFatherId) }
    var motherId by remember { mutableStateOf(initialMember?.motherId ?: suggestedMotherId) }

    // Multi-spouse selection states
    var selectedSpouseIds by remember { mutableStateOf(listOf<Long>()) }

    var fatherDropdownExpanded by remember { mutableStateOf(false) }
    var motherDropdownExpanded by remember { mutableStateOf(false) }
    var spouseDropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64Data = com.example.util.PhotoUtils.uriToBase64(context, uri)
            photoUri = base64Data ?: uri.toString()
        }
    }

    // Live age calculation
    val liveAgeString = DateUtils.formatAgeString(
        dateOfBirthStr = dateOfBirth.ifBlank { null },
        isDeceased = isDeceased,
        dateOfDeathStr = dateOfDeath.ifBlank { null }
    )

    // Filter available parents
    val availableFathers = allMembers.filter { it.id != initialMember?.id && it.gender == Gender.MALE.name }
    val availableMothers = allMembers.filter { it.id != initialMember?.id && it.gender == Gender.FEMALE.name }
    val availableSpouses = allMembers.filter { it.id != initialMember?.id }

    val selectedFather = allMembers.find { it.id == fatherId }
    val selectedMother = allMembers.find { it.id == motherId }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
        ) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = ObsidianBg.copy(alpha = 0.95f),
                borderColor = NeonCyan
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Title Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (initialMember == null) "Add Family Member" else "Edit Member Profile",
                            color = TextWhitePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMutedSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable Form Body
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Photo & Vignette Preview Box
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MemberPhotoView(
                                photoUri = photoUri,
                                name = name.ifBlank { "New Member" },
                                gender = gender,
                                isDeceased = isDeceased,
                                isDivorced = isDivorced,
                                size = 80.dp
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Member Avatar",
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = when {
                                        isDeceased -> "Red Vignette: Deceased status active"
                                        isDivorced -> "Grey Vignette: Divorced status active"
                                        else -> "Upload photo or leave blank for initials"
                                    },
                                    color = when {
                                        isDeceased -> DeceasedVignetteRed
                                        isDivorced -> DivorcedVignetteGrey
                                        else -> TextMutedSecondary
                                    },
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FuturisticButton(
                                        onClick = { imagePickerLauncher.launch("image/*") },
                                        accentColor = NeonCyan,
                                        modifier = Modifier.height(36.dp),
                                        testTag = "upload_photo_btn"
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AddPhotoAlternate,
                                                contentDescription = "Upload",
                                                tint = TextWhitePrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Upload", fontSize = 12.sp)
                                        }
                                    }

                                    if (photoUri != null) {
                                        GlassIconButton(
                                            onClick = { photoUri = null },
                                            iconColor = DeceasedVignetteRed,
                                            modifier = Modifier.size(36.dp),
                                            testTag = "remove_photo_btn"
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove photo",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Full Name
                        GlassTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = it.isBlank()
                            },
                            label = "Full Name *",
                            placeholder = "Enter full name",
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = NeonCyan)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "input_member_name"
                        )

                        if (nameError) {
                            Text(
                                text = "Name is required",
                                color = DeceasedVignetteRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        // Gender Selector Tabs
                        Column {
                            Text("Gender", color = TextMutedSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val genders = listOf(
                                    Gender.MALE to ("Male" to MaleBlue),
                                    Gender.FEMALE to ("Female" to FemalePink),
                                    Gender.OTHER to ("Other" to NeonCyan)
                                )

                                for ((g, data) in genders) {
                                    val (label, col) = data
                                    val isSelected = gender == g.name
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) col.copy(alpha = 0.25f) else Color(0x221E293B))
                                            .border(
                                                1.dp,
                                                if (isSelected) col else GlassCardBorder,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { gender = g.name }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) TextWhitePrimary else TextMutedSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Date of Birth & Time of Birth
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            GlassTextField(
                                value = dateOfBirth,
                                onValueChange = { dateOfBirth = it },
                                label = "Date of Birth (Optional)",
                                placeholder = "YYYY-MM-DD",
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = NeonCyan)
                                },
                                modifier = Modifier.weight(1.2f),
                                testTag = "input_dob"
                            )

                            GlassTextField(
                                value = timeOfBirth,
                                onValueChange = { timeOfBirth = it },
                                label = "Time (Optional)",
                                placeholder = "HH:MM",
                                leadingIcon = {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = NeonCyan)
                                },
                                modifier = Modifier.weight(0.8f),
                                testTag = "input_tob"
                            )
                        }

                        // Auto-Calculated Age Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x330284C7))
                                .border(1.dp, Color(0x660284C7), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Auto-Calculated Age:", color = TextMutedSecondary, fontSize = 12.sp)
                                Text(
                                    text = liveAgeString,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Parentage Lineage (Father & Mother)
                        Text(
                            text = "Biological Parents Lineage",
                            color = TextWhitePrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        // Father Dropdown
                        ExposedDropdownMenuBox(
                            expanded = fatherDropdownExpanded,
                            onExpandedChange = { fatherDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedFather?.name ?: "None (Root/Unknown)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Father (Paternal Line)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fatherDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary,
                                    focusedContainerColor = Color(0x331E293B),
                                    unfocusedContainerColor = Color(0x221E293B),
                                    focusedBorderColor = MaleBlue,
                                    unfocusedBorderColor = GlassCardBorder
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = fatherDropdownExpanded,
                                onDismissRequest = { fatherDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None (Unknown / Ancestor)") },
                                    onClick = {
                                        fatherId = null
                                        fatherDropdownExpanded = false
                                    }
                                )
                                availableFathers.forEach { male ->
                                    DropdownMenuItem(
                                        text = { Text("${male.name} (${male.dateOfBirth ?: "Living"})") },
                                        onClick = {
                                            fatherId = male.id
                                            fatherDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Mother Dropdown
                        ExposedDropdownMenuBox(
                            expanded = motherDropdownExpanded,
                            onExpandedChange = { motherDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedMother?.name ?: "None (Root/Unknown)",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Mother (Maternal Line)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = motherDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary,
                                    focusedContainerColor = Color(0x331E293B),
                                    unfocusedContainerColor = Color(0x221E293B),
                                    focusedBorderColor = FemalePink,
                                    unfocusedBorderColor = GlassCardBorder
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = motherDropdownExpanded,
                                onDismissRequest = { motherDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("None (Unknown / Ancestor)") },
                                    onClick = {
                                        motherId = null
                                        motherDropdownExpanded = false
                                    }
                                )
                                availableMothers.forEach { female ->
                                    DropdownMenuItem(
                                        text = { Text("${female.name} (${female.dateOfBirth ?: "Living"})") },
                                        onClick = {
                                            motherId = female.id
                                            motherDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Status Toggles: Deceased & Divorced
                        Text(
                            text = "Status & Visual Vignettes",
                            color = TextWhitePrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )

                        // Deceased Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x221E293B))
                                .border(1.dp, if (isDeceased) DeceasedVignetteRed else GlassCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Deceased Status", color = TextWhitePrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("Applies Red Vignette on photo edges", color = DeceasedVignetteRed, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isDeceased,
                                onCheckedChange = { isDeceased = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextWhitePrimary,
                                    checkedTrackColor = DeceasedVignetteRed
                                )
                            )
                        }

                        // Date of Death Input if deceased
                        AnimatedVisibility(visible = isDeceased) {
                            GlassTextField(
                                value = dateOfDeath,
                                onValueChange = { dateOfDeath = it },
                                label = "Date of Death (Optional)",
                                placeholder = "YYYY-MM-DD",
                                leadingIcon = {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = DeceasedVignetteRed)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "input_dod"
                            )
                        }

                        // Divorced Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x221E293B))
                                .border(1.dp, if (isDivorced) DivorcedVignetteGrey else GlassCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Divorced / Separated Status", color = TextWhitePrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("Applies Grey Vignette on photo edges", color = DivorcedVignetteGrey, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isDivorced,
                                onCheckedChange = { isDivorced = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextWhitePrimary,
                                    checkedTrackColor = DivorcedVignetteGrey
                                )
                            )
                        }

                        // Description / Notes
                        GlassTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Description / Biography (Optional)",
                            placeholder = "Add profession, hometown, memories, or bio notes...",
                            singleLine = false,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "input_description"
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel", color = TextMutedSecondary)
                        }

                        FuturisticButton(
                            onClick = {
                                if (name.isBlank()) {
                                    nameError = true
                                    return@FuturisticButton
                                }
                                val updated = (initialMember ?: FamilyMember(name = name)).copy(
                                    name = name.trim(),
                                    photoUri = photoUri,
                                    dateOfBirth = dateOfBirth.ifBlank { null },
                                    timeOfBirth = timeOfBirth.ifBlank { null },
                                    gender = gender,
                                    description = description.ifBlank { null },
                                    isDeceased = isDeceased,
                                    dateOfDeath = if (isDeceased) dateOfDeath.ifBlank { null } else null,
                                    isDivorced = isDivorced,
                                    fatherId = fatherId,
                                    motherId = motherId,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(updated, selectedSpouseIds, emptyList())
                            },
                            accentColor = NeonCyan,
                            testTag = "save_member_btn"
                        ) {
                            Text("Save Member", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
