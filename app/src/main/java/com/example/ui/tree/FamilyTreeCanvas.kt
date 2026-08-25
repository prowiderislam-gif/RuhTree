package com.example.ui.tree

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FamilyMember
import com.example.data.model.Gender
import com.example.data.model.SpouseRelation
import com.example.ui.components.GlassIconButton
import com.example.ui.components.MemberPhotoView
import com.example.ui.theme.DeceasedVignetteRed
import com.example.ui.theme.DivorcedVignetteGrey
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.FemalePink
import com.example.ui.theme.MaleBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextMutedSecondary
import com.example.ui.theme.TextWhitePrimary
import com.example.util.DateUtils
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class NodePosition(
    val memberId: Long,
    val x: Float,
    val y: Float
)

// Clean Spacing & Card Dimensions (Strict Non-Overlapping Margins)
private const val CARD_WIDTH = 138f
private const val CARD_HEIGHT = 144f
private const val SPOUSE_GAP = 42f
private const val SIBLING_GAP = 54f
private const val FAMILY_ROOT_GAP = 84f
private const val VERTICAL_SPACING = 236f
private const val START_Y = 64f

// Clean Slate Connector Branch Color
private val BranchLineColor = Color(0xFF8C9CAB)

@Composable
fun FamilyTreeCanvas(
    members: List<FamilyMember>,
    spouses: List<SpouseRelation>,
    focusMemberId: Long? = null,
    onFocusMemberChange: (Long?) -> Unit = {},
    onOpenMemberDetail: (FamilyMember) -> Unit = {},
    onAddChild: (fatherId: Long?, motherId: Long?) -> Unit,
    onAddSpouse: (FamilyMember) -> Unit,
    modifier: Modifier = Modifier
) {
    // 1. Filter Sub-Tree if a Focus Member is selected (shows main member + lower descendants up to 3 generations)
    val (activeMembers, activeSpouses) = remember(members, spouses, focusMemberId) {
        if (focusMemberId == null) {
            Pair(members, spouses)
        } else {
            extractDescendantsSubtree(
                rootId = focusMemberId,
                allMembers = members,
                allSpouses = spouses,
                maxGenerations = 3
            )
        }
    }

    val focusedMember = remember(members, focusMemberId) {
        members.find { it.id == focusMemberId }
    }

    // 2. Generational Hierarchy Computation (Zero overlap)
    val generationLevels = remember(activeMembers, activeSpouses, focusMemberId) {
        calculateGenerationalLevels(activeMembers, activeSpouses, focusMemberId)
    }

    val nodePositions = remember(activeMembers, activeSpouses, generationLevels) {
        calculateNonOverlappingPositions(activeMembers, activeSpouses, generationLevels)
    }

    // Transform State: Scale & Pan Offsets
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF111726),
                        Color(0xFF0A0E17),
                        ObsidianBg
                    )
                )
            )
            .pointerInput(Unit) {
                // Allows 1-finger dragging/sliding and 2-finger pinch zoom seamlessly
                detectTransformGestures(panZoomLock = false) { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.35f, 2.5f)
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
    ) {
        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        // Function to fit and center content smoothly on screen
        val fitToScreen: () -> Unit = {
            if (nodePositions.isNotEmpty()) {
                val minX = nodePositions.minOf { it.x }
                val maxX = nodePositions.maxOf { it.x } + CARD_WIDTH
                val minY = nodePositions.minOf { it.y }
                val maxY = nodePositions.maxOf { it.y } + CARD_HEIGHT

                val contentWidth = max(maxX - minX, 180f)
                val contentHeight = max(maxY - minY, 180f)

                val targetScaleX = (screenWidthPx * 0.88f) / contentWidth
                val targetScaleY = (screenHeightPx * 0.72f) / contentHeight
                val optimalScale = min(targetScaleX, targetScaleY).coerceIn(0.45f, 1.1f)

                scale = optimalScale
                val contentCenterX = (minX + maxX) / 2f
                val contentCenterY = (minY + maxY) / 2f

                offsetX = (screenWidthPx / 2f) - (contentCenterX * scale)
                offsetY = (screenHeightPx / 2f) - (contentCenterY * scale) + 10f
            } else {
                scale = 1.0f
                offsetX = 0f
                offsetY = 0f
            }
        }

        // Auto-fit when focus member changes or tree loads
        LaunchedEffect(focusMemberId, nodePositions.size) {
            fitToScreen()
        }

        // Subtle background dot grid
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dotSpacing = 32f * scale
            val startX = (offsetX % dotSpacing)
            val startY = (offsetY % dotSpacing)

            var x = startX
            while (x < size.width) {
                var y = startY
                while (y < size.height) {
                    drawCircle(
                        color = Color(0x1238BDF8),
                        radius = 1.2f * scale,
                        center = Offset(x, y)
                    )
                    y += dotSpacing
                }
                x += dotSpacing
            }
        }

        // Tree Content Container (Transformed with scale & pan)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        ) {
            // Connector Lines Layer:
            // Layout Pattern:
            //          Adam----spouse----Eve
            //             |                |
            //   ------------------------------------
            //   |                  |               |
            //  Ash -spouse- Maya  Rock            Lili
            //   |             |
            // -------------------
            // |        |        |
            // Lee     Nini     Ron
            Canvas(modifier = Modifier.fillMaxSize()) {
                val posMap = nodePositions.associateBy { it.memberId }
                val halfWidth = CARD_WIDTH / 2f
                val cardMidY = CARD_HEIGHT / 2f
                val strokeWidth = 2.4f

                // 1. Draw Spouse Connector Lines (Horizontal line connecting husband and wife)
                activeSpouses.forEach { relation ->
                    val pos1 = posMap[relation.memberId1]
                    val pos2 = posMap[relation.memberId2]
                    if (pos1 != null && pos2 != null) {
                        val leftPos = if (pos1.x < pos2.x) pos1 else pos2
                        val rightPos = if (pos1.x < pos2.x) pos2 else pos1

                        val startP = Offset(leftPos.x + CARD_WIDTH, leftPos.y + cardMidY)
                        val endP = Offset(rightPos.x, rightPos.y + cardMidY)

                        val isDivorced = relation.isDivorced
                        val lineColor = if (isDivorced) DivorcedVignetteGrey.copy(alpha = 0.6f) else BranchLineColor
                        val pathEffect = if (isDivorced) PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) else null

                        // Straight horizontal spouse bridge
                        drawLine(
                            color = lineColor,
                            start = startP,
                            end = endP,
                            strokeWidth = strokeWidth,
                            pathEffect = pathEffect,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 2. Draw Clean Parent-to-Children Tree Branches
                // Both Father and Mother drop clean vertical lines down to the horizontal sibling bus bar,
                // and vertical drop lines go down from the bus bar into each child.
                val nuclearFamilies = activeMembers.filter { it.fatherId != null || it.motherId != null }
                    .groupBy { Pair(it.fatherId, it.motherId) }

                nuclearFamilies.forEach { (parents, childrenList) ->
                    val fatherPos = parents.first?.let { posMap[it] }
                    val motherPos = parents.second?.let { posMap[it] }

                    val childrenPositions = childrenList.mapNotNull { posMap[it.id] }

                    if (childrenPositions.isNotEmpty()) {
                        val childTopY = childrenPositions.first().y

                        // Calculate parent bottom Y
                        val parentBottomY = max(
                            fatherPos?.let { it.y + CARD_HEIGHT } ?: 0f,
                            motherPos?.let { it.y + CARD_HEIGHT } ?: 0f
                        )

                        // Sibling bus bar sits neatly midway between parents bottom and children top
                        val busY = (parentBottomY + childTopY) / 2f

                        // Gather all X points (Parents + Children) to determine span of horizontal bus bar
                        val allXPoints = mutableListOf<Float>()

                        fatherPos?.let {
                            val fCenterX = it.x + halfWidth
                            allXPoints.add(fCenterX)
                            // Vertical line dropping from Father bottom to sibling bus bar
                            drawLine(
                                color = BranchLineColor,
                                start = Offset(fCenterX, it.y + CARD_HEIGHT),
                                end = Offset(fCenterX, busY),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }

                        motherPos?.let {
                            val mCenterX = it.x + halfWidth
                            allXPoints.add(mCenterX)
                            // Vertical line dropping from Mother bottom to sibling bus bar
                            drawLine(
                                color = BranchLineColor,
                                start = Offset(mCenterX, it.y + CARD_HEIGHT),
                                end = Offset(mCenterX, busY),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }

                        // Connect each child with a vertical drop from bus bar into top of child card
                        childrenPositions.forEach { cPos ->
                            val cCenterX = cPos.x + halfWidth
                            allXPoints.add(cCenterX)

                            drawLine(
                                color = BranchLineColor,
                                start = Offset(cCenterX, busY),
                                end = Offset(cCenterX, childTopY),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }

                        // Draw clean horizontal sibling bus bar across all connected branches
                        if (allXPoints.isNotEmpty()) {
                            val minX = allXPoints.minOrNull() ?: 0f
                            val maxX = allXPoints.maxOrNull() ?: 0f

                            drawLine(
                                color = BranchLineColor,
                                start = Offset(minX, busY),
                                end = Offset(maxX, busY),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            // Member Nodes Layer (Sleek Matte Translucent Glass Cards)
            activeMembers.forEach { member ->
                val pos = nodePositions.find { it.memberId == member.id }
                if (pos != null) {
                    val isMainMember = member.id == focusMemberId

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(pos.x.roundToInt(), pos.y.roundToInt()) }
                    ) {
                        SleekGlassFamilyMemberCard(
                            member = member,
                            isMainFocus = isMainMember,
                            onClick = {
                                if (isMainMember) {
                                    // Clicking on the already focused member opens their full dossier
                                    onOpenMemberDetail(member)
                                } else {
                                    // Clicking on any member makes him the main member and shows lower descendants
                                    onFocusMemberChange(member.id)
                                }
                            },
                            onOpenDetail = { onOpenMemberDetail(member) },
                            onAddChild = {
                                val fatherId = if (member.gender == Gender.MALE.name) member.id else null
                                val motherId = if (member.gender == Gender.FEMALE.name) member.id else null
                                onAddChild(fatherId, motherId)
                            },
                            onAddSpouse = { onAddSpouse(member) }
                        )
                    }
                }
            }
        }

        // Top Floating Focus Sub-Tree Breadcrumb Banner
        AnimatedVisibility(
            visible = focusedMember != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 76.dp, start = 16.dp, end = 16.dp)
        ) {
            if (focusedMember != null) {
                Box(
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xF20F172A))
                        .border(1.2.dp, EmeraldGlow.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "Sub-tree",
                            tint = EmeraldGlow,
                            modifier = Modifier.size(20.dp)
                        )

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Main Focus: ",
                                    color = TextMutedSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = focusedMember.name,
                                    color = EmeraldGlow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Showing descendants (up to 3 generations)",
                                color = TextWhitePrimary.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // View Full Dossier Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x3338BDF8))
                                .border(0.8.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .clickable { onOpenMemberDetail(focusedMember) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Dossier",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Reset / View Full Tree Button
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0x33EF4444))
                                .border(0.8.dp, DeceasedVignetteRed.copy(alpha = 0.6f), CircleShape)
                                .clickable { onFocusMemberChange(null) }
                                .testTag("reset_focus_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Show Full Tree",
                                tint = DeceasedVignetteRed,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating Zoom & Recenter Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassIconButton(
                onClick = { scale = (scale * 1.25f).coerceAtMost(2.5f) },
                iconColor = NeonCyan,
                modifier = Modifier.size(44.dp),
                testTag = "zoom_in_btn"
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
            }

            GlassIconButton(
                onClick = { scale = (scale * 0.8f).coerceAtLeast(0.35f) },
                iconColor = NeonCyan,
                modifier = Modifier.size(44.dp),
                testTag = "zoom_out_btn"
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
            }

            GlassIconButton(
                onClick = { fitToScreen() },
                iconColor = EmeraldGlow,
                modifier = Modifier.size(44.dp),
                testTag = "fit_screen_btn"
            ) {
                Icon(Icons.Default.FitScreen, contentDescription = "Fit to Screen")
            }
        }
    }
}

/**
 * Sleek Matte Translucent Glass Finish Card:
 * - Frosted dark glass container with subtle glow border (gender/deceased/focus themed)
 * - Profile avatar / initial disc with luminous border
 * - Crisp Member Name in bold typography
 * - Clean formatted age pill underneath
 * - Quick action icons (ℹ️ Dossier, + Child, + Spouse)
 */
@Composable
fun SleekGlassFamilyMemberCard(
    member: FamilyMember,
    isMainFocus: Boolean = false,
    onClick: () -> Unit,
    onOpenDetail: () -> Unit,
    onAddChild: () -> Unit,
    onAddSpouse: () -> Unit
) {
    // Theme colors
    val accentColor = when {
        isMainFocus -> EmeraldGlow
        member.isDeceased -> DeceasedVignetteRed
        member.gender == Gender.FEMALE.name -> Color(0xFFFF4866) // Vibrant Coral/Pink
        member.gender == Gender.MALE.name -> Color(0xFF38BDF8)   // Sky Blue
        else -> NeonPurple
    }

    val initialLetter = member.name.trim().firstOrNull()?.uppercase() ?: "?"
    val formattedAge = DateUtils.formatShortAgeString(member.dateOfBirth, member.isDeceased, member.dateOfDeath)

    Box(
        modifier = Modifier
            .width(CARD_WIDTH.dp)
            .height(CARD_HEIGHT.dp)
            .shadow(
                elevation = if (isMainFocus) 12.dp else 6.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = accentColor.copy(alpha = 0.25f),
                spotColor = accentColor.copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xE6141E30), // Sleek matte frosted dark glass
                        Color(0xD90A0F1D)
                    )
                )
            )
            .border(
                width = if (isMainFocus) 2.dp else 1.2.dp,
                color = if (isMainFocus) EmeraldGlow else accentColor.copy(alpha = 0.45f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 7.dp)
            .testTag("tree_node_${member.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Avatar + Info Icon
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Circular Avatar Disc
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .border(1.8.dp, accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!member.photoUri.isNullOrBlank()) {
                        MemberPhotoView(
                            photoUri = member.photoUri,
                            name = member.name,
                            gender = member.gender,
                            isDeceased = member.isDeceased,
                            isDivorced = member.isDivorced,
                            size = 46.dp
                        )
                    } else {
                        Text(
                            text = initialLetter,
                            color = accentColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Top-Right Dossier Info Icon (ℹ️)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0x3338BDF8))
                        .clickable { onOpenDetail() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View Dossier",
                        tint = NeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
                }

                // Top-Left Root Badge if active focus
                if (isMainFocus) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .clip(RoundedCornerShape(4.dp))
                            .background(EmeraldGlow)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "ROOT",
                            color = Color(0xFF062817),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            // Middle Section: Member Name
            Text(
                text = member.name,
                color = TextWhitePrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Age Pill
            if (formattedAge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x2B334155))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = formattedAge,
                        color = Color(0xFF94A3B8),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Bottom Section: Quick Add Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add Child Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x2610B981))
                        .clickable { onAddChild() }
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldGlow, modifier = Modifier.size(10.dp))
                        Text("Child", color = EmeraldGlow, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Add Spouse Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0x26EC4899))
                        .clickable { onAddSpouse() }
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = FemalePink, modifier = Modifier.size(10.dp))
                        Text("Spouse", color = FemalePink, fontSize = 8.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/**
 * Extracts a sub-tree rooted at [rootId] and includes all descendants up to [maxGenerations] (2-3 generations)
 * along with spouses of the root and of every descendant.
 */
fun extractDescendantsSubtree(
    rootId: Long,
    allMembers: List<FamilyMember>,
    allSpouses: List<SpouseRelation>,
    maxGenerations: Int = 3
): Pair<List<FamilyMember>, List<SpouseRelation>> {
    val memberMap = allMembers.associateBy { it.id }
    if (!memberMap.containsKey(rootId)) return Pair(allMembers, allSpouses)

    val includedMemberIds = mutableSetOf<Long>()
    includedMemberIds.add(rootId)

    fun addSpousesOf(idSet: Set<Long>) {
        val spouseIds = allSpouses
            .filter { it.memberId1 in idSet || it.memberId2 in idSet }
            .flatMap { listOf(it.memberId1, it.memberId2) }
            .filter { memberMap.containsKey(it) }
        includedMemberIds.addAll(spouseIds)
    }

    addSpousesOf(setOf(rootId))

    var currentParentIds = includedMemberIds.toSet()
    for (gen in 1..maxGenerations) {
        val nextGenChildren = allMembers.filter { child ->
            (child.fatherId != null && child.fatherId in currentParentIds) ||
            (child.motherId != null && child.motherId in currentParentIds)
        }
        if (nextGenChildren.isEmpty()) break

        val childIds = nextGenChildren.map { it.id }.toSet()
        includedMemberIds.addAll(childIds)
        addSpousesOf(childIds)
        currentParentIds = childIds
    }

    val filteredMembers = allMembers.filter { it.id in includedMemberIds }
    val filteredSpouses = allSpouses.filter { it.memberId1 in includedMemberIds && it.memberId2 in includedMemberIds }
    return Pair(filteredMembers, filteredSpouses)
}

/**
 * Computes generational hierarchy levels for tree layout.
 * When [focusRootId] is provided, the root and spouse sit at level 0,
 * their children at level 1, grandchildren at level 2, etc.
 */
private fun calculateGenerationalLevels(
    members: List<FamilyMember>,
    spouses: List<SpouseRelation>,
    focusRootId: Long? = null
): Map<Long, Int> {
    val levels = mutableMapOf<Long, Int>()
    val memberMap = members.associateBy { it.id }

    if (focusRootId != null && memberMap.containsKey(focusRootId)) {
        // Focus sub-tree leveling starting at root = 0
        levels[focusRootId] = 0

        // Spouses of root
        spouses.filter { it.memberId1 == focusRootId || it.memberId2 == focusRootId }
            .forEach {
                val spId = if (it.memberId1 == focusRootId) it.memberId2 else it.memberId1
                levels[spId] = 0
            }

        // Descendants level BFS
        var currentLevel = 0
        while (levels.size < members.size && currentLevel < 5) {
            val currentParents = levels.filter { it.value == currentLevel }.keys
            val nextGen = members.filter {
                (it.fatherId in currentParents || it.motherId in currentParents) && !levels.containsKey(it.id)
            }
            if (nextGen.isEmpty()) break

            nextGen.forEach { child ->
                levels[child.id] = currentLevel + 1
                // Add child's spouses to same level
                spouses.filter { it.memberId1 == child.id || it.memberId2 == child.id }
                    .forEach { sp ->
                        val spId = if (sp.memberId1 == child.id) sp.memberId2 else sp.memberId1
                        levels[spId] = currentLevel + 1
                    }
            }
            currentLevel++
        }

        // Catch any disconnected member in the sub-tree
        members.forEach { if (!levels.containsKey(it.id)) levels[it.id] = 0 }
        return levels
    }

    // Default Full-Tree Leveling
    fun getLevel(id: Long, visited: Set<Long> = emptySet()): Int {
        if (id in visited) return 0
        if (levels.containsKey(id)) return levels[id]!!

        val member = memberMap[id] ?: return 0
        val fatherLevel = member.fatherId?.let { getLevel(it, visited + id) + 1 }
        val motherLevel = member.motherId?.let { getLevel(it, visited + id) + 1 }

        val computed = max(fatherLevel ?: 0, motherLevel ?: 0)
        levels[id] = computed
        return computed
    }

    members.forEach { getLevel(it.id) }

    repeat(4) {
        spouses.forEach { sp ->
            val lvl1 = levels[sp.memberId1] ?: 0
            val lvl2 = levels[sp.memberId2] ?: 0
            val maxLvl = max(lvl1, lvl2)
            levels[sp.memberId1] = maxLvl
            levels[sp.memberId2] = maxLvl
        }
    }

    return levels
}

/**
 * Positions tree nodes in a clean, hierarchical layout with linear rows and columns.
 * Guarantees zero overlapping across all branches and sub-branches.
 */
private fun calculateNonOverlappingPositions(
    members: List<FamilyMember>,
    spouses: List<SpouseRelation>,
    generationLevels: Map<Long, Int>
): List<NodePosition> {
    val positions = mutableListOf<NodePosition>()
    val memberMap = members.associateBy { it.id }
    if (members.isEmpty()) return positions

    val cardWidth = CARD_WIDTH
    val spouseGap = SPOUSE_GAP
    val siblingGap = SIBLING_GAP
    val familyRootGap = FAMILY_ROOT_GAP
    val verticalSpacing = VERTICAL_SPACING
    val startY = START_Y

    fun getSpousesOf(memberId: Long): List<Long> {
        return spouses
            .filter { it.memberId1 == memberId || it.memberId2 == memberId }
            .map { if (it.memberId1 == memberId) it.memberId2 else it.memberId1 }
            .filter { memberMap.containsKey(it) }
    }

    fun getChildrenOf(memberId: Long, spouseIds: List<Long>): List<FamilyMember> {
        val parentIds = (listOf(memberId) + spouseIds).toSet()
        return members.filter { child ->
            (child.fatherId != null && child.fatherId in parentIds) ||
            (child.motherId != null && child.motherId in parentIds)
        }.distinctBy { it.id }
    }

    class SubtreeUnit(
        val primaryMember: FamilyMember,
        val spouseMembers: List<FamilyMember>,
        val childrenUnits: MutableList<SubtreeUnit> = mutableListOf()
    ) {
        val allMemberIds: List<Long> get() = listOf(primaryMember.id) + spouseMembers.map { it.id }
        val unitWidth: Float get() = (allMemberIds.size * cardWidth) + ((allMemberIds.size - 1).coerceAtLeast(0) * spouseGap)
        var subtreeWidth: Float = 0f
    }

    val visitedMembers = mutableSetOf<Long>()

    fun buildSubtree(member: FamilyMember): SubtreeUnit {
        visitedMembers.add(member.id)
        val spouseIds = getSpousesOf(member.id).filter { it !in visitedMembers }
        val spouseList = spouseIds.mapNotNull { memberMap[it] }
        spouseIds.forEach { visitedMembers.add(it) }

        val unit = SubtreeUnit(primaryMember = member, spouseMembers = spouseList)
        val children = getChildrenOf(member.id, spouseIds).filter { it.id !in visitedMembers }

        children.forEach { child ->
            unit.childrenUnits.add(buildSubtree(child))
        }
        return unit
    }

    fun measureSubtree(unit: SubtreeUnit): Float {
        if (unit.childrenUnits.isEmpty()) {
            unit.subtreeWidth = unit.unitWidth
            return unit.subtreeWidth
        }

        val childrenTotalWidth = unit.childrenUnits.map { measureSubtree(it) }.sum() +
                ((unit.childrenUnits.size - 1).coerceAtLeast(0) * siblingGap)

        unit.subtreeWidth = max(unit.unitWidth, childrenTotalWidth)
        return unit.subtreeWidth
    }

    fun placeSubtree(unit: SubtreeUnit, leftX: Float) {
        val currentLevel = generationLevels[unit.primaryMember.id] ?: 0
        val y = startY + (currentLevel * verticalSpacing)

        val availableWidth = unit.subtreeWidth
        val unitW = unit.unitWidth

        // Place parents centered in their allocated subtree width
        val parentStartX = leftX + (availableWidth - unitW) / 2f
        var currentParentX = parentStartX

        val unitMembers = listOf(unit.primaryMember) + unit.spouseMembers
        unitMembers.forEach { m ->
            positions.add(NodePosition(m.id, currentParentX, y))
            currentParentX += cardWidth + spouseGap
        }

        // Place children side-by-side without overlap
        if (unit.childrenUnits.isNotEmpty()) {
            val childrenTotalWidth = unit.childrenUnits.sumOf { it.subtreeWidth.toDouble() }.toFloat() +
                    ((unit.childrenUnits.size - 1) * siblingGap)

            var childLeftX = leftX + (availableWidth - childrenTotalWidth) / 2f
            unit.childrenUnits.forEach { childUnit ->
                placeSubtree(childUnit, childLeftX)
                childLeftX += childUnit.subtreeWidth + siblingGap
            }
        }
    }

    // Identify Root Subtrees (Members with no parents in current active members list)
    val rootMembers = members.filter { m ->
        (m.fatherId == null || !memberMap.containsKey(m.fatherId)) &&
        (m.motherId == null || !memberMap.containsKey(m.motherId))
    }

    val rootSubtrees = mutableListOf<SubtreeUnit>()
    rootMembers.forEach { root ->
        if (root.id !in visitedMembers) {
            rootSubtrees.add(buildSubtree(root))
        }
    }

    members.forEach { m ->
        if (m.id !in visitedMembers) {
            rootSubtrees.add(buildSubtree(m))
        }
    }

    rootSubtrees.forEach { measureSubtree(it) }

    var rootLeftX = 60f
    rootSubtrees.forEach { rootUnit ->
        placeSubtree(rootUnit, rootLeftX)
        rootLeftX += rootUnit.subtreeWidth + familyRootGap
    }

    return positions
}
