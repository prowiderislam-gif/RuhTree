package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FamilyMember
import com.example.data.model.SpouseRelation
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.GlassCardBorder
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary
import com.example.util.DateUtils
import com.example.util.ImageExporter
import com.example.util.KinshipCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KinshipCompareDialog(
    allMembers: List<FamilyMember>,
    spouses: List<SpouseRelation>,
    initialMemberAId: Long? = null,
    initialMemberBId: Long? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var memberAId by remember {
        mutableStateOf(initialMemberAId ?: allMembers.firstOrNull()?.id)
    }
    var memberBId by remember {
        mutableStateOf(
            initialMemberBId ?: allMembers.getOrNull(1)?.id ?: allMembers.firstOrNull()?.id
        )
    }

    var dropdownAExpanded by remember { mutableStateOf(false) }
    var dropdownBExpanded by remember { mutableStateOf(false) }
    var searchAQuery by remember { mutableStateOf("") }
    var searchBQuery by remember { mutableStateOf("") }

    val personA = allMembers.find { it.id == memberAId }
    val personB = allMembers.find { it.id == memberBId }

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
                borderColor = NeonPurple
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CompareArrows,
                                    contentDescription = "Compare",
                                    tint = NeonPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Kinship & Age Comparator",
                                    color = TextWhitePrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Text(
                                    text = "Analyze dual kinship & exact age gap",
                                    color = TextMutedSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMutedSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Searchable Selectors Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Selector A
                        ExposedDropdownMenuBox(
                            expanded = dropdownAExpanded,
                            onExpandedChange = { dropdownAExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = if (dropdownAExpanded) searchAQuery else (personA?.name ?: "Select Person 1"),
                                onValueChange = { searchAQuery = it },
                                placeholder = { Text("Name/Age/DoB...", fontSize = 11.sp) },
                                label = { Text("Person 1", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(15.dp))
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownAExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary,
                                    focusedContainerColor = Color(0x331E293B),
                                    unfocusedContainerColor = Color(0x221E293B),
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = GlassCardBorder
                                ),
                                singleLine = true
                            )

                            val filteredA = allMembers.filter { member ->
                                if (searchAQuery.isBlank()) true
                                else {
                                    val q = searchAQuery.trim().lowercase()
                                    val nameMatch = member.name.lowercase().contains(q)
                                    val dobMatch = member.dateOfBirth?.lowercase()?.contains(q) == true
                                    val ageMatch = DateUtils.formatAgeString(member.dateOfBirth, member.isDeceased, member.dateOfDeath).lowercase().contains(q)
                                    nameMatch || dobMatch || ageMatch
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = dropdownAExpanded,
                                onDismissRequest = { dropdownAExpanded = false },
                                modifier = Modifier.background(Color(0xFF0F172A))
                            ) {
                                filteredA.forEach { member ->
                                    val ageStr = DateUtils.formatAgeString(member.dateOfBirth, member.isDeceased, member.dateOfDeath)
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                MemberPhotoView(
                                                    photoUri = member.photoUri,
                                                    name = member.name,
                                                    gender = member.gender,
                                                    isDeceased = member.isDeceased,
                                                    isDivorced = member.isDivorced,
                                                    size = 28.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(member.name, color = TextWhitePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("DoB: ${member.dateOfBirth ?: "—"} • Age: $ageStr", color = TextMutedSecondary, fontSize = 10.sp)
                                                }
                                            }
                                        },
                                        onClick = {
                                            memberAId = member.id
                                            dropdownAExpanded = false
                                            searchAQuery = ""
                                        }
                                    )
                                }
                            }
                        }

                        // Swap Button
                        GlassIconButton(
                            onClick = {
                                val temp = memberAId
                                memberAId = memberBId
                                memberBId = temp
                            },
                            iconColor = NeonCyan,
                            modifier = Modifier.size(38.dp),
                            testTag = "swap_members_btn"
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", modifier = Modifier.size(20.dp))
                        }

                        // Selector B
                        ExposedDropdownMenuBox(
                            expanded = dropdownBExpanded,
                            onExpandedChange = { dropdownBExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = if (dropdownBExpanded) searchBQuery else (personB?.name ?: "Select Person 2"),
                                onValueChange = { searchBQuery = it },
                                placeholder = { Text("Name/Age/DoB...", fontSize = 11.sp) },
                                label = { Text("Person 2", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(15.dp))
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownBExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhitePrimary,
                                    unfocusedTextColor = TextWhitePrimary,
                                    focusedContainerColor = Color(0x331E293B),
                                    unfocusedContainerColor = Color(0x221E293B),
                                    focusedBorderColor = NeonPurple,
                                    unfocusedBorderColor = GlassCardBorder
                                ),
                                singleLine = true
                            )

                            val filteredB = allMembers.filter { member ->
                                if (searchBQuery.isBlank()) true
                                else {
                                    val q = searchBQuery.trim().lowercase()
                                    val nameMatch = member.name.lowercase().contains(q)
                                    val dobMatch = member.dateOfBirth?.lowercase()?.contains(q) == true
                                    val ageMatch = DateUtils.formatAgeString(member.dateOfBirth, member.isDeceased, member.dateOfDeath).lowercase().contains(q)
                                    nameMatch || dobMatch || ageMatch
                                }
                            }

                            ExposedDropdownMenu(
                                expanded = dropdownBExpanded,
                                onDismissRequest = { dropdownBExpanded = false },
                                modifier = Modifier.background(Color(0xFF0F172A))
                            ) {
                                filteredB.forEach { member ->
                                    val ageStr = DateUtils.formatAgeString(member.dateOfBirth, member.isDeceased, member.dateOfDeath)
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                MemberPhotoView(
                                                    photoUri = member.photoUri,
                                                    name = member.name,
                                                    gender = member.gender,
                                                    isDeceased = member.isDeceased,
                                                    isDivorced = member.isDivorced,
                                                    size = 28.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(member.name, color = TextWhitePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("DoB: ${member.dateOfBirth ?: "—"} • Age: $ageStr", color = TextMutedSecondary, fontSize = 10.sp)
                                                }
                                            }
                                        },
                                        onClick = {
                                            memberBId = member.id
                                            dropdownBExpanded = false
                                            searchBQuery = ""
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (personA != null && personB != null) {
                        val relationship = KinshipCalculator.determineRelationship(
                            personA = personA,
                            personB = personB,
                            allMembers = allMembers,
                            spouses = spouses
                        )

                        val ageDiffResult = DateUtils.calculateAgeDifference(
                            dobStr1 = personA.dateOfBirth,
                            dobStr2 = personB.dateOfBirth
                        )

                        val isAOlder = if (ageDiffResult.hasExactDates) {
                            ageDiffResult.firstIsOlder
                        } else {
                            personA.generationLevel <= personB.generationLevel
                        }

                        val leftPerson = if (isAOlder) personA else personB
                        val rightPerson = if (isAOlder) personB else personA

                        val leftRelationshipTitle = if (isAOlder) relationship.titleForA else relationship.titleForB
                        val rightRelationshipTitle = if (isAOlder) relationship.titleForB else relationship.titleForA

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Dual Kinship Notice if Cousin Marriage / Consanguinity
                            if (relationship.isDualRelationship) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(NeonAmber.copy(alpha = 0.15f))
                                        .border(1.dp, NeonAmber.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "💍 Dual Relationship Detected: Spouses & Blood Relatives (Cousin Marriage)",
                                            color = NeonAmber,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Side by Side Comparison Presentation Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0x331E293B), Color(0x660F172A))
                                        )
                                    )
                                    .border(1.dp, GlassCardBorder, RoundedCornerShape(18.dp))
                                    .padding(16.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Older Person (Left, 92dp Photo)
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            MemberPhotoView(
                                                photoUri = leftPerson.photoUri,
                                                name = leftPerson.name,
                                                gender = leftPerson.gender,
                                                isDeceased = leftPerson.isDeceased,
                                                isDivorced = leftPerson.isDivorced,
                                                size = 92.dp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = leftPerson.name,
                                                color = TextWhitePrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.5.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = DateUtils.formatAgeString(leftPerson.dateOfBirth, leftPerson.isDeceased, leftPerson.dateOfDeath),
                                                color = NeonCyan,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (relationship.isDualRelationship) NeonAmber.copy(alpha = 0.25f) else NeonPurple.copy(alpha = 0.25f))
                                                    .border(1.dp, if (relationship.isDualRelationship) NeonAmber else NeonPurple.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = leftRelationshipTitle,
                                                    color = TextWhitePrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }

                                        // Central Age Difference & Kinship Indicator
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(NeonCyan.copy(alpha = 0.2f))
                                                    .border(1.dp, NeonCyan, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CompareArrows,
                                                    contentDescription = "Comparison",
                                                    tint = NeonCyan,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "${leftPerson.name} is Older",
                                                color = TextMutedSecondary,
                                                fontSize = 9.5.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        // Younger Person (Right, 74dp Photo)
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            MemberPhotoView(
                                                photoUri = rightPerson.photoUri,
                                                name = rightPerson.name,
                                                gender = rightPerson.gender,
                                                isDeceased = rightPerson.isDeceased,
                                                isDivorced = rightPerson.isDivorced,
                                                size = 74.dp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = rightPerson.name,
                                                color = TextWhitePrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = DateUtils.formatAgeString(rightPerson.dateOfBirth, rightPerson.isDeceased, rightPerson.dateOfDeath),
                                                color = NeonCyan,
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (relationship.isDualRelationship) NeonAmber.copy(alpha = 0.25f) else NeonCyan.copy(alpha = 0.25f))
                                                    .border(1.dp, if (relationship.isDualRelationship) NeonAmber else NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = rightRelationshipTitle,
                                                    color = TextWhitePrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.5.sp,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Age Difference Banner
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0x330284C7))
                                            .border(1.dp, Color(0x660284C7), RoundedCornerShape(10.dp))
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = "⏱ ${ageDiffResult.differenceText}",
                                            color = EmeraldGlow,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            // Relationship Breakdown & Lineage Path
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x221E293B))
                                    .border(1.dp, GlassCardBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .padding(14.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "Kinship Analysis",
                                        color = TextWhitePrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = relationship.detailedExplanation,
                                        color = TextWhitePrimary.copy(alpha = 0.9f),
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Save Comparison Dossier
                        FuturisticButton(
                            onClick = {
                                val bitmap = Bitmap.createBitmap(850, 600, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                canvas.drawColor(android.graphics.Color.parseColor("#090D16"))
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.WHITE
                                    textSize = 34f
                                    isAntiAlias = true
                                }
                                canvas.drawText("Ruh Tree Kinship Analysis", 40f, 60f, paint)
                                paint.textSize = 28f
                                paint.color = android.graphics.Color.parseColor("#00F0FF")
                                canvas.drawText("${personA.name} & ${personB.name}", 40f, 110f, paint)
                                paint.textSize = 22f
                                paint.color = android.graphics.Color.LTGRAY
                                canvas.drawText("To ${personA.name}: ${relationship.titleForA}", 40f, 160f, paint)
                                canvas.drawText("To ${personB.name}: ${relationship.titleForB}", 40f, 200f, paint)
                                canvas.drawText("Age Gap: ${ageDiffResult.differenceText}", 40f, 250f, paint)
                                canvas.drawText(relationship.detailedExplanation, 40f, 300f, paint)

                                ImageExporter.saveBitmapToDevice(context, bitmap, "Kinship_${personA.name}_${personB.name}")
                            },
                            accentColor = NeonCyan,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "export_comparison_btn"
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Download, contentDescription = "Export", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export Comparison Dossier", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
