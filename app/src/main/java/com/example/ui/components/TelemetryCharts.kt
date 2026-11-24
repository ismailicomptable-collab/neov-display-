package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CpuCoreInfo
import com.example.ui.theme.LocalTelemetryPalette
import com.example.ui.theme.MonoMetricLarge
import com.example.ui.theme.MonoMetricSmall
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TelemetryCard(
  modifier: Modifier = Modifier,
  borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
  content: @Composable () -> Unit,
) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.large,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    border = BorderStroke(1.dp, borderColor),
  ) {
    content()
  }
}

@Composable
fun TelemetryArcGauge(
  percent: Float,
  primaryLabel: String,
  secondaryLabel: String,
  accentColor: Color,
  modifier: Modifier = Modifier,
  size: Dp = 152.dp,
) {
  val clamped = percent.coerceIn(0f, 100f)
  val animatedPercent by
    animateFloatAsState(
      targetValue = clamped,
      animationSpec = spring(stiffness = 220f, dampingRatio = 0.85f),
      label = "gauge_percent",
    )
  val trackColor = MaterialTheme.colorScheme.surfaceVariant
  val tickColor = MaterialTheme.colorScheme.outline

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
      val strokePx = 14.dp.toPx()
      val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
      val topLeft = Offset(strokePx / 2f, strokePx / 2f)
      val startAngle = 140f
      val maxSweep = 260f

      // Subtle outer tick marks
      val cx = this.size.width / 2f
      val cy = this.size.height / 2f
      val outerR = this.size.minDimension / 2f
      for (i in 0..20) {
        val deg = startAngle + (maxSweep * i / 20f)
        val rad = Math.toRadians(deg.toDouble())
        val innerR = outerR - strokePx - 6.dp.toPx()
        val tickR = outerR - strokePx - 1.dp.toPx()
        drawLine(
          color = tickColor.copy(alpha = if (i % 5 == 0) 0.7f else 0.3f),
          start = Offset(cx + (innerR * cos(rad)).toFloat(), cy + (innerR * sin(rad)).toFloat()),
          end = Offset(cx + (tickR * cos(rad)).toFloat(), cy + (tickR * sin(rad)).toFloat()),
          strokeWidth = if (i % 5 == 0) 2.dp.toPx() else 1.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }

      // Background arc
      drawArc(
        color = trackColor,
        startAngle = startAngle,
        sweepAngle = maxSweep,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokePx, cap = StrokeCap.Round),
      )

      // Foreground value arc
      val sweep = (animatedPercent / 100f) * maxSweep
      if (sweep > 0.5f) {
        drawArc(
          brush =
            Brush.sweepGradient(
              0.0f to accentColor.copy(alpha = 0.75f),
              0.5f to accentColor,
              1.0f to accentColor,
            ),
          startAngle = startAngle,
          sweepAngle = sweep,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
      }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = String.format(Locale.US, "%.1f%%", animatedPercent),
        style = MonoMetricLarge.copy(fontSize = 26.sp),
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = primaryLabel,
        style = MaterialTheme.typography.labelMedium,
        color = accentColor,
        fontWeight = FontWeight.Bold,
      )
      if (secondaryLabel.isNotBlank()) {
        Text(
          text = secondaryLabel,
          style = MonoMetricSmall.copy(fontSize = 11.sp),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun LiveSparklineChart(
  values: List<Float>,
  accentColor: Color,
  modifier: Modifier = Modifier,
  maxValue: Float = 100f,
) {
  val gridColor = LocalTelemetryPalette.current.gridLine.copy(alpha = 0.45f)
  val surfaceBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(14.dp))
        .background(surfaceBg)
        .border(1.dp, gridColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
  ) {
    Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 6.dp)) {
      val w = size.width
      val h = size.height
      if (w <= 0f || h <= 0f) return@Canvas

      // Draw Windows Task Manager style 4x6 grid
      for (row in 1..3) {
        val y = h * row / 4f
        drawLine(
          color = gridColor,
          start = Offset(0f, y),
          end = Offset(w, y),
          strokeWidth = 0.8.dp.toPx(),
        )
      }
      for (col in 1..5) {
        val x = w * col / 6f
        drawLine(
          color = gridColor,
          start = Offset(x, 0f),
          end = Offset(x, h),
          strokeWidth = 0.8.dp.toPx(),
        )
      }

      if (values.size < 2) return@Canvas
      val safeMax = maxValue.coerceAtLeast(1f)
      val windowSize = 60
      val stepX = w / (windowSize - 1).toFloat()
      val startOffset = (windowSize - values.size).coerceAtLeast(0)

      val linePath = Path()
      val fillPath = Path()

      values.forEachIndexed { idx, v ->
        val x = (startOffset + idx) * stepX
        val norm = (v / safeMax).coerceIn(0f, 1f)
        val y = h - (norm * (h - 4.dp.toPx())) - 2.dp.toPx()
        if (idx == 0) {
          linePath.moveTo(x, y)
          fillPath.moveTo(x, h)
          fillPath.lineTo(x, y)
        } else {
          linePath.lineTo(x, y)
          fillPath.lineTo(x, y)
        }
        if (idx == values.lastIndex) {
          fillPath.lineTo(x, h)
          fillPath.close()

          // Live tip dot
          drawCircle(
            color = accentColor.copy(alpha = 0.25f),
            radius = 7.dp.toPx(),
            center = Offset(x, y),
          )
          drawCircle(
            color = accentColor,
            radius = 3.2.dp.toPx(),
            center = Offset(x, y),
          )
        }
      }

      drawPath(
        path = fillPath,
        brush =
          Brush.verticalGradient(
            colors = listOf(accentColor.copy(alpha = 0.36f), accentColor.copy(alpha = 0.02f))
          ),
      )
      drawPath(
        path = linePath,
        color = accentColor,
        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
      )
    }
  }
}

data class MemorySegment(
  val label: String,
  val valueText: String,
  val bytes: Long,
  val color: Color,
)

@Composable
fun SegmentedMemoryBar(
  segments: List<MemorySegment>,
  totalBytes: Long,
  modifier: Modifier = Modifier,
) {
  val safeTotal = totalBytes.coerceAtLeast(1L).toFloat()
  val trackBg = MaterialTheme.colorScheme.surfaceVariant

  Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .height(14.dp)
          .clip(RoundedCornerShape(7.dp))
          .background(trackBg),
      horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      segments.forEach { seg ->
        val fraction = (seg.bytes.toFloat() / safeTotal).coerceIn(0f, 1f)
        if (fraction > 0.01f) {
          Box(
            modifier =
              Modifier.weight(fraction)
                .height(14.dp)
                .background(seg.color)
          )
        }
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      segments.forEach { seg ->
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Box(
            modifier = Modifier.size(8.dp).clip(CircleShape).background(seg.color)
          )
          Column {
            Text(
              text = seg.label,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = seg.valueText,
              style = MonoMetricSmall,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
            )
          }
        }
      }
    }
  }
}

@Composable
fun CoreFrequencyCard(
  core: CpuCoreInfo,
  coreLabel: String,
  offlineLabel: String,
  modifier: Modifier = Modifier,
) {
  val palette = LocalTelemetryPalette.current
  val barColor =
    when {
      !core.isOnline -> MaterialTheme.colorScheme.outline
      core.utilizationPercent >= 80f -> palette.coral
      core.utilizationPercent >= 50f -> palette.amber
      else -> palette.emerald
    }

  val animatedFraction by
    animateFloatAsState(
      targetValue = if (core.isOnline) (core.utilizationPercent / 100f).coerceIn(0.05f, 1f) else 0f,
      animationSpec = spring(stiffness = 300f),
      label = "core_bar_${core.index}",
    )

  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "$coreLabel ${core.index}",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = if (core.isOnline && core.currentMhz > 0) "${core.currentMhz} MHz" else offlineLabel,
          style = MonoMetricSmall,
          color = barColor,
          fontWeight = FontWeight.Bold,
        )
      }

      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
      ) {
        Box(
          modifier =
            Modifier.fillMaxWidth(animatedFraction)
              .height(7.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(barColor)
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = core.governor,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (core.maxMhz > 0) {
          Text(
            text = "${core.minMhz}-${core.maxMhz}",
            style = MonoMetricSmall.copy(fontSize = 10.5.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
fun MetricStatPill(
  label: String,
  value: String,
  accent: Color,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(modifier = Modifier.width(2.dp))
      Text(
        text = value,
        style = MonoMetricSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
        color = accent,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
