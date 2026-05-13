package org.jellyplus.client.ui.mobile.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val AuthButtonShape = RoundedCornerShape(14.dp)
private val AuthAccent = Color(0xFFFFB300)

@Composable
internal fun MobileAuthScaffold(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MobileAuthLogo(modifier = Modifier.size(112.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
    }
}

@Composable
internal fun MobileAuthLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = Color(0xFF07101F),
            cornerRadius = CornerRadius(w * 0.22f, h * 0.22f),
        )
        drawOval(
            color = Color(0xFF00D4A8).copy(alpha = 0.08f),
            topLeft = Offset(w * 0.18f, h * 0.2f),
            size = Size(w * 0.64f, h * 0.54f),
        )

        val bell = Path().apply {
            moveTo(w * 0.16f, h * 0.55f)
            quadraticTo(w * 0.15f, h * 0.10f, w * 0.50f, h * 0.08f)
            quadraticTo(w * 0.85f, h * 0.10f, w * 0.84f, h * 0.55f)
            quadraticTo(w * 0.84f, h * 0.80f, w * 0.50f, h * 0.81f)
            quadraticTo(w * 0.16f, h * 0.80f, w * 0.16f, h * 0.55f)
            close()
        }
        drawPath(bell, Color(0xFF00D4A8).copy(alpha = 0.74f))

        val shimmer = Path().apply {
            moveTo(w * 0.23f, h * 0.50f)
            quadraticTo(w * 0.24f, h * 0.22f, w * 0.50f, h * 0.20f)
            quadraticTo(w * 0.76f, h * 0.22f, w * 0.77f, h * 0.50f)
            quadraticTo(w * 0.72f, h * 0.58f, w * 0.50f, h * 0.59f)
            quadraticTo(w * 0.28f, h * 0.58f, w * 0.23f, h * 0.50f)
            close()
        }
        drawPath(shimmer, Color.White.copy(alpha = 0.14f))

        val play = Path().apply {
            moveTo(w * 0.42f, h * 0.39f)
            lineTo(w * 0.42f, h * 0.61f)
            lineTo(w * 0.63f, h * 0.50f)
            close()
        }
        drawPath(play, Color.White.copy(alpha = 0.72f))
    }
}

@Composable
internal fun MobileAuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: (@Composable () -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AuthAccent, contentColor = Color.Black),
        shape = AuthButtonShape,
    ) {
        if (content != null) {
            content()
        } else {
            Text(text, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun MobileAuthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        shape = AuthButtonShape,
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}
