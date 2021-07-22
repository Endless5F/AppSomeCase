package com.android.jetpack.ext

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

fun Modifier.unread(show: Boolean) = this.drawWithContent {
    drawContent()
    if (show) {
        drawIntoCanvas {
            // ViewOverlay
            // 绘制红色小点角标
            val paint = Paint().apply {
                color = Color.Red
            }
            it.drawCircle(Offset(size.width - 1.dp.toPx(), 1.dp.toPx()), 5.dp.toPx(), paint)
        }
    }
}

/**
 * 百分比偏移
 * 注：不可缺少属性 fillMaxSize()或具有一定的宽
 */
fun Modifier.percentOffsetX(percent: Float): Modifier = this.layout { measurable, contraints ->
    val placeable = measurable.measure(contraints)
    layout(placeable.width, placeable.height) {
        val offset = IntOffset((percent * placeable.width).roundToInt(), 0)
        placeable.placeRelative(offset)
    }
}