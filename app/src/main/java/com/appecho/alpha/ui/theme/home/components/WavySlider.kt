package com.appecho.alpha.ui.theme.home.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun WavySlider(
    progress: Float, // 0f ~ 1f
    modifier: Modifier = Modifier,
    waveColor: Color = Color(0xFF673AB7), // 截图中的淡黄色 🟡
    trackColor: Color = Color(0xFF03A9F4), // 截图中的深蓝色轨道 🔵
    thumbColor: Color = Color(0xFF673AB7)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseShift"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(40.dp)) {
        val width = size.width
        val centerY = size.height / 2
        val currentX = width * progress

        // 1. 绘制背景轨道 (未播放部分)
        drawLine(
            color = trackColor,
            start = Offset(currentX, centerY),
            end = Offset(width, centerY),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 2. 绘制波浪线 (已播放部分)
        val wavePath = Path()

        val waveAmplitude = 6.dp.toPx() // 波浪高度 🌊
        val waveFrequency = 0.05f // 波浪密度

        for (x in 0..currentX.toInt()) {
            // 正弦函数计算 y 偏移，加入 phaseShift 实现动态波浪
            val relativeY = waveAmplitude * kotlin.math.sin(x * waveFrequency - phaseShift)
            if (x == 0) {
                wavePath.moveTo(0f, centerY + relativeY)
            } else {
                wavePath.lineTo(x.toFloat(), centerY + relativeY)
            }
        }

        drawPath(
            path = wavePath,
            color = waveColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 3. 绘制垂直胶囊滑块
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(currentX - 2.dp.toPx(), centerY - 15.dp.toPx()),
            size = Size(4.dp.toPx(), 30.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
}