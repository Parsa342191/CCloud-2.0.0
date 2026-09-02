package com.pira.ccloud.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Adds a clearly visible highlight (subtle scale-up + colored border) when the element
 * gains D-pad/keyboard focus - e.g. when navigating with an Android TV remote.
 *
 * Plain `Modifier.clickable()` is technically D-pad focusable, but by default gives no
 * visual indication of which item currently has focus, which makes remote navigation on
 * TV confusing. Pass the same [interactionSource] to the `clickable`/`selectable` modifier
 * on the same element so focus state is shared.
 *
 * Usage:
 * ```
 * val interactionSource = remember { MutableInteractionSource() }
 * Card(
 *     modifier = Modifier
 *         .tvFocusIndication(interactionSource)
 *         .clickable(interactionSource = interactionSource, indication = LocalIndication.current) { onClick() }
 * )
 * ```
 */
@Composable
fun Modifier.tvFocusIndication(
    interactionSource: MutableInteractionSource,
    shape: Shape = RoundedCornerShape(12.dp),
    focusedScale: Float = 1.08f,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    borderWidth: Dp = 3.dp
): Modifier {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1f,
        animationSpec = tween(durationMillis = 150),
        label = "tvFocusScale"
    )

    return this
        .scale(scale)
        .then(
            if (isFocused) {
                Modifier.border(width = borderWidth, color = borderColor, shape = shape)
            } else {
                Modifier
            }
        )
}
