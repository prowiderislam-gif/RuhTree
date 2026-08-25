package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Gender
import com.example.ui.theme.DeceasedVignetteRed
import com.example.ui.theme.DeceasedVignetteRedDark
import com.example.ui.theme.DivorcedVignetteGrey
import com.example.ui.theme.DivorcedVignetteGreyDark
import com.example.ui.theme.FemalePink
import com.example.ui.theme.GenderOtherPurple
import com.example.ui.theme.MaleBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.TextWhitePrimary

@Composable
fun MemberPhotoView(
    photoUri: String?,
    name: String,
    gender: String,
    isDeceased: Boolean = false,
    isDivorced: Boolean = false,
    size: Dp = 64.dp,
    showStatusBadge: Boolean = true,
    modifier: Modifier = Modifier
) {
    val genderColor = when (gender) {
        Gender.FEMALE.name -> FemalePink
        Gender.MALE.name -> MaleBlue
        else -> GenderOtherPurple
    }

    // Determine outer ring styling
    val (primaryRingColor, secondaryRingColor) = when {
        isDeceased -> Pair(DeceasedVignetteRed, DeceasedVignetteRedDark)
        isDivorced -> Pair(DivorcedVignetteGrey, DivorcedVignetteGreyDark)
        else -> Pair(genderColor, NeonCyan)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow & border frame
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            primaryRingColor,
                            secondaryRingColor,
                            primaryRingColor.copy(alpha = 0.5f),
                            primaryRingColor
                        )
                    )
                )
                .padding(3.dp)
                .clip(CircleShape)
                .background(ObsidianBg)
        ) {
            // Photo Content
            if (!photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback stylish Initials / Avatar placeholder
                val initials = name.trim().split(" ")
                    .filter { it.isNotEmpty() }
                    .take(2)
                    .map { it.first().uppercase() }
                    .joinToString("")
                    .ifEmpty { "?" }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    genderColor.copy(alpha = 0.35f),
                                    ObsidianBg
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = TextWhitePrimary,
                        fontSize = (size.value * 0.35f).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // RED VIGNETTE OVERLAY FOR DECEASED MEMBERS
            if (isDeceased) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.toPx() / 2f
                    val center = Offset(radius, radius)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.55f to Color.Transparent,
                                0.85f to DeceasedVignetteRed.copy(alpha = 0.65f),
                                1.0f to DeceasedVignetteRed.copy(alpha = 0.95f)
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }
            }

            // GREY VIGNETTE OVERLAY FOR DIVORCED MEMBERS
            if (isDivorced && !isDeceased) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.toPx() / 2f
                    val center = Offset(radius, radius)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.50f to Color.Transparent,
                                0.80f to DivorcedVignetteGrey.copy(alpha = 0.70f),
                                1.0f to DivorcedVignetteGreyDark.copy(alpha = 0.95f)
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }
            }
        }

        // Status badge icon at the bottom corner if enabled
        if (showStatusBadge) {
            if (isDeceased) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size((size.value * 0.32f).dp.coerceAtLeast(18.dp))
                        .clip(CircleShape)
                        .background(DeceasedVignetteRed)
                        .border(1.dp, TextWhitePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "†",
                        color = TextWhitePrimary,
                        fontSize = (size.value * 0.22f).coerceAtLeast(10f).sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else if (isDivorced) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .size((size.value * 0.32f).dp.coerceAtLeast(18.dp))
                        .clip(CircleShape)
                        .background(DivorcedVignetteGrey)
                        .border(1.dp, TextWhitePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HeartBroken,
                        contentDescription = "Divorced",
                        tint = TextWhitePrimary,
                        modifier = Modifier.size((size.value * 0.20f).dp.coerceAtLeast(11.dp))
                    )
                }
            }
        }
    }
}
