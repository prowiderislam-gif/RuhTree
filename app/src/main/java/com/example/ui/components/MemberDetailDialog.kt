package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.data.model.SpouseRelation
import com.example.ui.theme.DeceasedVignetteRed
import com.example.ui.theme.DivorcedVignetteGrey
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.FemalePink
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.MaleBlue
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary
import com.example.util.DateUtils
import com.example.util.ImageExporter
import com.example.util.KinshipCalculator
import com.example.util.KinshipCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailDialog(
    member: FamilyMember,
    allMembers: List<FamilyMember>,
    spouses: List<SpouseRelation>,
    onEditRequest: () -> Unit,
    onDeleteRequest: () -> Unit,
    onAddSuccessor: (parentFatherId: Long?, parentMotherId: Long?) -> Unit,
    onAddPredecessor: (forMember: FamilyMember) -> Unit,
    onAddSpouse: (forMember: FamilyMember) -> Unit,
    onCompareWith: (memberId: Long) -> Unit,
    onSelectMember: (memberId: Long) -> Unit,
    onFocusAsMain: (memberId: Long) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val memberMap = remember(allMembers) { allMembers.associateBy { it.id } }

    val father = member.fatherId?.let { memberMap[it] }
    val mother = member.motherId?.let { memberMap[it] }

    val memberSpouseRelations = spouses.filter { it.memberId1 == member.id || it.memberId2 == member.id }
    val spouseMembers = memberSpouseRelations.mapNotNull { relation ->
        val spouseId = if (relation.memberId1 == member.id) relation.memberId2 else relation.memberId1
        memberMap[spouseId]?.let { it to relation }
    }

    val children = allMembers.filter { it.fatherId == member.id || it.motherId == member.id }
    val siblings = allMembers.filter {
        it.id != member.id && (
            (member.fatherId != null && it.fatherId == member.fatherId) ||
            (member.motherId != null && it.motherId == member.motherId)
        )
    }

    val calculatedAge = DateUtils.formatAgeString(member.dateOfBirth, member.isDeceased, member.dateOfDeath)

    // Interactive Kinship Comparison State inside the Dossier
    var compareTargetId by remember {
        mutableStateOf(allMembers.firstOrNull { it.id != member.id }?.id)
    }
    var isTargetPickerExpanded by remember { mutableStateOf(false) }
    var targetSearchQuery by remember { mutableStateOf("") }

    val compareTarget = allMembers.find { it.id == compareTargetId }
    val computedKinship = remember(member, compareTarget, allMembers, spouses) {
        if (compareTarget != null) {
            KinshipCalculator.determineRelationship(member, compareTarget, allMembers, spouses)
        } else null
    }

    val ageComparison = remember(member, compareTarget) {
        if (compareTarget != null) {
            val age1 = DateUtils.calculateAge(member.dateOfBirth, member.isDeceased, member.dateOfDeath)
            val age2 = DateUtils.calculateAge(compareTarget.dateOfBirth, compareTarget.isDeceased, compareTarget.dateOfDeath)
            if (age1 != null && age2 != null) {
                val diffYears = kotlin.math.abs(age1.years - age2.years)
                when {
                    age1.years > age2.years -> "${member.name} is $diffYears ${if (diffYears == 1) "year" else "years"} older"
                    age2.years > age1.years -> "${compareTarget.name} is $diffYears ${if (diffYears == 1) "year" else "years"} older"
                    else -> "Same age (${age1.years} years old)"
                }
            } else "Age difference not determinable"
        } else ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 730.dp)
        ) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = ObsidianBg.copy(alpha = 0.96f),
                borderColor = if (member.isDeceased) DeceasedVignetteRed else if (member.isDivorced) DivorcedVignetteGrey else NeonCyan
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Member Dossier & Kinship",
                            color = TextWhitePrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Focus on Sub-tree Button
                            GlassIconButton(
                                onClick = {
                                    onFocusAsMain(member.id)
                                    onDismiss()
                                },
                                iconColor = EmeraldGlow,
                                modifier = Modifier.size(34.dp),
                                testTag = "focus_subtree_dossier_btn"
                            ) {
                                Icon(Icons.Default.AccountTree, contentDescription = "Focus Sub-Tree", modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Capture Dossier as Image
                            GlassIconButton(
                                onClick = {
                                    val bitmap = Bitmap.createBitmap(800, 700, Bitmap.Config.ARGB_8888)
                                    val canvas = Canvas(bitmap)
                                    canvas.drawColor(android.graphics.Color.parseColor("#090D16"))
                                    val paint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 34f
                                        isAntiAlias = true
                                    }
                                    canvas.drawText("Ruh Tree Member Dossier", 40f, 70f, paint)
                                    paint.textSize = 38f
                                    paint.color = android.graphics.Color.parseColor("#00F0FF")
                                    canvas.drawText(member.name, 40f, 130f, paint)
                                    paint.textSize = 24f
                                    paint.color = android.graphics.Color.LTGRAY
                                    canvas.drawText("Gender: ${member.gender} | Status: ${if (member.isDeceased) "Deceased" else "Living"}", 40f, 180f, paint)
                                    canvas.drawText("Date of Birth: ${member.dateOfBirth ?: "Unknown"} (Age: $calculatedAge)", 40f, 220f, paint)
                                    if (member.isDeceased && member.dateOfDeath != null) {
                                        canvas.drawText("Date of Death: ${member.dateOfDeath}", 40f, 260f, paint)
                                    }
                                    if (father != null || mother != null) {
                                        canvas.drawText("Parents: ${father?.name ?: "None"} & ${mother?.name ?: "None"}", 40f, 310f, paint)
                                    }
                                    if (!member.description.isNullOrBlank()) {
                                        canvas.drawText("Bio: ${member.description}", 40f, 360f, paint)
                                    }

                                    ImageExporter.saveBitmapToDevice(context, bitmap, "Dossier_${member.name}")
                                },
                                iconColor = NeonCyan,
                                modifier = Modifier.size(34.dp),
                                testTag = "capture_dossier_btn"
                            ) {
                                Icon(Icons.Default.Download, contentDescription = "Capture Card", modifier = Modifier.size(18.dp))
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedSecondary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scrollable Dossier Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Zoomed-in Hero Profile Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0x331E293B))
                                .border(1.dp, GlassCardBorder, RoundedCornerShape(18.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Zoomed In Portrait
                            Box(
                                modifier = Modifier
                                    .size(92.dp)
                                    .shadow(8.dp, CircleShape)
                                    .clip(CircleShape)
                                    .border(
                                        2.5.dp,
                                        if (member.isDeceased) DeceasedVignetteRed else if (member.isDivorced) DivorcedVignetteGrey else if (member.gender == Gender.FEMALE.name) FemalePink else MaleBlue,
                                        CircleShape
                                    )
                            ) {
                                MemberPhotoView(
                                    photoUri = member.photoUri,
                                    name = member.name,
                                    gender = member.gender,
                                    isDeceased = member.isDeceased,
                                    isDivorced = member.isDivorced,
                                    size = 92.dp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                )

                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Gender Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (member.gender) {
                                                    Gender.FEMALE.name -> FemalePink.copy(alpha = 0.25f)
                                                    Gender.MALE.name -> MaleBlue.copy(alpha = 0.25f)
                                                    else -> NeonPurple.copy(alpha = 0.25f)
                                                }
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = member.gender,
                                            color = when (member.gender) {
                                                Gender.FEMALE.name -> FemalePink
                                                Gender.MALE.name -> MaleBlue
                                                else -> NeonPurple
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Deceased Badge
                                    if (member.isDeceased) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DeceasedVignetteRed.copy(alpha = 0.25f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("† Deceased", color = DeceasedVignetteRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // Divorced Badge
                                    if (member.isDivorced) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DivorcedVignetteGrey.copy(alpha = 0.25f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Divorced", color = DivorcedVignetteGrey, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = calculatedAge,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Birth & Death Details
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x221E293B))
                                .border(1.dp, GlassCardBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Date of Birth:", color = TextMutedSecondary, fontSize = 12.sp)
                                    Text(
                                        text = "${DateUtils.formatDisplayDate(member.dateOfBirth)}${if (!member.timeOfBirth.isNullOrBlank()) " at ${DateUtils.formatDisplayTime(member.timeOfBirth)}" else ""}".ifEmpty { "Not specified" },
                                        color = TextWhitePrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                }

                                if (member.isDeceased) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Passed Away:", color = DeceasedVignetteRed, fontSize = 12.sp)
                                        Text(
                                            text = DateUtils.formatDisplayDate(member.dateOfDeath).ifEmpty { "Date unrecorded" },
                                            color = DeceasedVignetteRed,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Biography / Notes
                        if (!member.description.isNullOrBlank()) {
                            Column {
                                Text("Biography & Notes", color = TextWhitePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = member.description,
                                    color = TextMutedSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        // ==========================================
                        // EMBEDDED KINSHIP COMPARISON CARD SECTION
                        // ==========================================
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0x33A855F7),
                                            Color(0x1F1E293B)
                                        )
                                    )
                                )
                                .border(1.2.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CompareArrows,
                                            contentDescription = "Compare",
                                            tint = NeonPurple,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Compare Relationship & Age",
                                            color = TextWhitePrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }

                                    TextButton(
                                        onClick = { compareTargetId?.let { onCompareWith(it) } },
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("Full View ↗", color = NeonCyan, fontSize = 11.sp)
                                    }
                                }

                                // Searchable Member Dropdown Selector for comparison
                                ExposedDropdownMenuBox(
                                    expanded = isTargetPickerExpanded,
                                    onExpandedChange = { isTargetPickerExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value = if (isTargetPickerExpanded) targetSearchQuery else (compareTarget?.name ?: "Search person by name/age/dob..."),
                                        onValueChange = { targetSearchQuery = it },
                                        placeholder = { Text("Search by name, DoB, or age...", color = TextMutedSecondary, fontSize = 11.sp) },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTargetPickerExpanded)
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonPurple, modifier = Modifier.size(16.dp))
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextWhitePrimary,
                                            unfocusedTextColor = TextWhitePrimary,
                                            focusedBorderColor = NeonPurple,
                                            unfocusedBorderColor = GlassCardBorder
                                        ),
                                        singleLine = true
                                    )

                                    val filteredTargets = allMembers.filter { it.id != member.id }
                                        .filter { target ->
                                            if (targetSearchQuery.isBlank()) true
                                            else {
                                                val q = targetSearchQuery.trim().lowercase()
                                                val nameMatch = target.name.lowercase().contains(q)
                                                val dobMatch = target.dateOfBirth?.lowercase()?.contains(q) == true
                                                val ageMatch = DateUtils.formatAgeString(target.dateOfBirth, target.isDeceased, target.dateOfDeath).lowercase().contains(q)
                                                nameMatch || dobMatch || ageMatch
                                            }
                                        }

                                    ExposedDropdownMenu(
                                        expanded = isTargetPickerExpanded,
                                        onDismissRequest = { isTargetPickerExpanded = false },
                                        modifier = Modifier.background(Color(0xFF0F172A))
                                    ) {
                                        filteredTargets.forEach { target ->
                                            val tAge = DateUtils.formatAgeString(target.dateOfBirth, target.isDeceased, target.dateOfDeath)
                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        MemberPhotoView(
                                                            photoUri = target.photoUri,
                                                            name = target.name,
                                                            gender = target.gender,
                                                            isDeceased = target.isDeceased,
                                                            isDivorced = target.isDivorced,
                                                            size = 26.dp
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(target.name, color = TextWhitePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                            Text("DoB: ${target.dateOfBirth ?: "—"} • Age: $tAge", color = TextMutedSecondary, fontSize = 10.sp)
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    compareTargetId = target.id
                                                    isTargetPickerExpanded = false
                                                    targetSearchQuery = ""
                                                }
                                            )
                                        }
                                    }
                                }

                                // Computed Relationship Result Display
                                if (compareTarget != null && computedKinship != null) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0x330F172A))
                                            .border(1.dp, GlassCardBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Dual Relationship Highlight
                                        if (computedKinship.isDualRelationship) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(NeonAmber.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "💍 Consanguineous Marriage / Dual Kinship",
                                                    color = NeonAmber,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("To ${member.name}:", color = TextMutedSecondary, fontSize = 10.sp)
                                                Text(
                                                    text = computedKinship.titleForA,
                                                    color = if (computedKinship.isDualRelationship) NeonAmber else NeonCyan,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }

                                            Icon(
                                                Icons.Default.SwapHoriz,
                                                contentDescription = "Relationship",
                                                tint = TextMutedSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )

                                            Column(
                                                modifier = Modifier.weight(1f),
                                                horizontalAlignment = Alignment.End
                                            ) {
                                                Text("To ${compareTarget.name}:", color = TextMutedSecondary, fontSize = 10.sp)
                                                Text(
                                                    text = computedKinship.titleForB,
                                                    color = if (computedKinship.isDualRelationship) NeonAmber else FemalePink,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        Text(
                                            text = computedKinship.detailedExplanation,
                                            color = TextWhitePrimary.copy(alpha = 0.9f),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp
                                        )

                                        Text(
                                            text = "⏱ $ageComparison",
                                            color = EmeraldGlow,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        // Biological Parents
                        Column {
                            Text("Parents Lineage", color = TextWhitePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Father Card
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x221E293B))
                                        .border(1.dp, if (father != null) MaleBlue.copy(alpha = 0.5f) else GlassCardBorder, RoundedCornerShape(12.dp))
                                        .clickable(enabled = father != null) { father?.id?.let { onSelectMember(it) } }
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text("Father", color = MaleBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = father?.name ?: "Unknown / None",
                                            color = if (father != null) TextWhitePrimary else TextMutedSecondary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                // Mother Card
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x221E293B))
                                        .border(1.dp, if (mother != null) FemalePink.copy(alpha = 0.5f) else GlassCardBorder, RoundedCornerShape(12.dp))
                                        .clickable(enabled = mother != null) { mother?.id?.let { onSelectMember(it) } }
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Text("Mother", color = FemalePink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = mother?.name ?: "Unknown / None",
                                            color = if (mother != null) TextWhitePrimary else TextMutedSecondary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Spouses & Marriages
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Spouses & Marriages (${spouseMembers.size})", color = TextWhitePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                TextButton(
                                    onClick = { onAddSpouse(member) },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("+ Add Spouse", color = NeonCyan, fontSize = 11.sp)
                                }
                            }
                            if (spouseMembers.isEmpty()) {
                                Text("No spouse records linked.", color = TextMutedSecondary, fontSize = 11.sp)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    spouseMembers.forEach { (spMember, rel) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0x221E293B))
                                                .border(1.dp, if (rel.isDivorced) DivorcedVignetteGrey else GlassCardBorder, RoundedCornerShape(10.dp))
                                                .clickable { onSelectMember(spMember.id) }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                MemberPhotoView(
                                                    photoUri = spMember.photoUri,
                                                    name = spMember.name,
                                                    gender = spMember.gender,
                                                    isDeceased = spMember.isDeceased,
                                                    isDivorced = rel.isDivorced || spMember.isDivorced,
                                                    size = 32.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(spMember.name, color = TextWhitePrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                                                    Text(
                                                        if (rel.isDivorced) "Divorced / Previous Marriage" else "Current Spouse",
                                                        color = if (rel.isDivorced) DivorcedVignetteGrey else NeonEmerald,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Children / Successors
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Children / Successors (${children.size})", color = TextWhitePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                TextButton(
                                    onClick = {
                                        val fatherId = if (member.gender == Gender.MALE.name) member.id else null
                                        val motherId = if (member.gender == Gender.FEMALE.name) member.id else null
                                        onAddSuccessor(fatherId, motherId)
                                    },
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("+ Add Child", color = NeonCyan, fontSize = 11.sp)
                                }
                            }
                            if (children.isEmpty()) {
                                Text("No children added yet.", color = TextMutedSecondary, fontSize = 11.sp)
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    children.take(4).forEach { child ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0x221E293B))
                                                .clickable { onSelectMember(child.id) }
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                MemberPhotoView(
                                                    photoUri = child.photoUri,
                                                    name = child.name,
                                                    gender = child.gender,
                                                    isDeceased = child.isDeceased,
                                                    isDivorced = child.isDivorced,
                                                    size = 38.dp
                                                )
                                                Text(child.name, color = TextWhitePrimary, fontSize = 10.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Siblings
                        if (siblings.isNotEmpty()) {
                            Column {
                                Text("Siblings (${siblings.size})", color = TextWhitePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    siblings.take(4).forEach { sibling ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0x221E293B))
                                                .clickable { onSelectMember(sibling.id) }
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                MemberPhotoView(
                                                    photoUri = sibling.photoUri,
                                                    name = sibling.name,
                                                    gender = sibling.gender,
                                                    isDeceased = sibling.isDeceased,
                                                    isDivorced = sibling.isDivorced,
                                                    size = 38.dp
                                                )
                                                Text(sibling.name, color = TextWhitePrimary, fontSize = 10.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Bottom Action Bar: Edit, Delete (Password Protected), Compare Kinship
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Delete Button (PIN Protected)
                            GlassIconButton(
                                onClick = onDeleteRequest,
                                iconColor = DeceasedVignetteRed,
                                modifier = Modifier.size(42.dp),
                                testTag = "delete_member_btn"
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
                            }

                            // Edit Button (PIN Protected)
                            FuturisticButton(
                                onClick = onEditRequest,
                                accentColor = NeonCyan,
                                modifier = Modifier.height(42.dp),
                                testTag = "edit_member_btn"
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Lock, contentDescription = "PIN required", modifier = Modifier.size(12.dp), tint = NeonCyan)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Open dedicated comparator dialog
                        FuturisticButton(
                            onClick = { onCompareWith(member.id) },
                            accentColor = NeonPurple,
                            modifier = Modifier.height(42.dp),
                            testTag = "compare_from_dossier_btn"
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CompareArrows, contentDescription = "Compare", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Compare Hub", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
