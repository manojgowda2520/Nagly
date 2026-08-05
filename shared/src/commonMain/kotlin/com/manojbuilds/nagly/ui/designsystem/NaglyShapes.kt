package com.manojbuilds.nagly.ui.designsystem

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object NaglyShapes {
    val card: RoundedCornerShape = RoundedCornerShape(16.dp)
    val cardLarge: RoundedCornerShape = RoundedCornerShape(18.dp)
    val dialog: RoundedCornerShape = RoundedCornerShape(24.dp)
    val pill: Shape = CircleShape
    val minTapTarget: Dp = 48.dp
}
