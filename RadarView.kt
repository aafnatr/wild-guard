package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Detection
import com.example.ui.theme.ForestCardBorder
import com.example.ui.theme.ForestSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Utilitarian 2D Spatial Perimeter Map
 * Crisp concentric range rings, cardinal axes, and high-contrast wildlife position markers.
 * Designed without glowing sweeps, animations, or decorative gradients.
 */
@Composable
fun RadarView(
    detections: List<Detection>,
    onDetectionClick: (Detection) -> Unit,
    modifier: Modifier = Modifier,
    maxDistanceMeters: Float = 1000f
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(ForestSurface)
            .border(1.dp, ForestCardBorder, RoundedCornerShape(6.dp))
            .testTag("radar_view"),
        contentAlignment = Alignment.Center
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val center = Offset(width / 2f, height / 2f)
        val radius = min(width, height) / 2f - 24.dp.value

        Canvas(modifier = Modifier.fillMaxSize()) {
            val ringCount = 4
            val ringStep = radius / ringCount

            // Range rings
            for (i in 1..ringCount) {
                val currentRadius = ringStep * i
                drawCircle(
                    color = ForestCardBorder,
                    radius = currentRadius,
                    center = center,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = if (i % 2 == 1) PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f) else null
                    )
                )
            }

            // Crosshair axes
            drawLine(
                color = ForestCardBorder,
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y + radius),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = ForestCardBorder,
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // Center base point (Settlement)
            drawCircle(
                color = TextSecondary,
                radius = 4.dp.toPx(),
                center = center
            )
        }

        // Cardinal directions
        Text(
            text = "N",
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
        )
        Text(
            text = "S",
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp)
        )
        Text(
            text = "E",
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
        )
        Text(
            text = "W",
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp)
        )

        // Range scale notation
        Text(
            text = "1000m range",
            color = TextMuted,
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        )

        // Wildlife position blips
        detections.take(12).forEach { detection ->
            val distRatio = (detection.distanceMeters.toFloat() / maxDistanceMeters).coerceIn(0.1f, 0.95f)
            val angleRad = Math.toRadians((detection.directionAngleDeg - 90.0))
            val blipRadius = radius * distRatio

            val offsetX = (center.x + blipRadius * cos(angleRad).toFloat() - center.x)
            val offsetY = (center.y + blipRadius * sin(angleRad).toFloat() - center.y)
            val blipColor = Color(detection.threatLevel.hexColor)

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
                    .size(32.dp)
                    .clickable { onDetectionClick(detection) }
                    .testTag("radar_blip_${detection.id}"),
                contentAlignment = Alignment.Center
            ) {
                // Crisp solid indicator with high contrast border
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(blipColor)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }
    }
}
