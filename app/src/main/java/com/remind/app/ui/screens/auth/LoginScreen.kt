package com.remind.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.remind.app.R
import com.remind.app.ui.theme.*

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .systemBarsPadding(),
    ) {
        // Ambient orbs
        AmbientOrb(
            color    = PastelBlue.copy(alpha = 0.35f),
            size     = 260.dp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-80).dp),
        )
        AmbientOrb(
            color    = PastelGreen.copy(alpha = 0.28f),
            size     = 300.dp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-80).dp, y = 100.dp),
        )
        AmbientOrb(
            color    = PastelPink.copy(alpha = 0.22f),
            size     = 120.dp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-40).dp),
        )

        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            TopSection()
            BottomSection(
                isLoading    = isLoading,
                onLoginClick = onLoginClick,
            )
        }
    }
}

// ── Sections ──────────────────────────────────────────────────────────────────

@Composable
private fun TopSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.padding(top = 80.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo_app),
            contentDescription = "App Logo",
            modifier = Modifier.size(100.dp)
        )//logo

        Spacer(Modifier.height(28.dp))

        Text(
            text = buildAnnotatedString {
                append("ReMind")
                withStyle(SpanStyle(color = PastelBlue)) { append("+") }
            },
            fontSize      = 38.sp,
            fontWeight    = FontWeight.Bold,
            color         = TextPrimary,
            letterSpacing = (-0.5).sp,
            lineHeight    = 38.sp,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text          = "Remember everything that matters.",
            fontWeight    = FontWeight.Normal,
            fontSize      = 14.sp,
            color         = TextSecondary,
            letterSpacing = 0.3.sp,
            textAlign     = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeaturePill(label = "Reminders", background = PastelBlueLight)
            FeaturePill(label = "Notes",     background = PastelGreenLight)
            FeaturePill(label = "Insights",  background = PastelPinkLight)
        }
    }
}

@Composable
private fun BottomSection(
    isLoading    : Boolean,
    onLoginClick : () -> Unit,
) {
    Column(
        modifier            = Modifier.padding(horizontal = 28.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GoogleSignInButton(
            isLoading = isLoading,
            onClick   = onLoginClick,
        )

        Text(
            text = buildAnnotatedString {
                append("By continuing, you agree to our\n")
                withStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color          = TextSecondary,
                    )
                ) { append("Terms of Service") }
                append(" & ")
                withStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color          = TextSecondary,
                    )
                ) { append("Privacy Policy") }
            },
            fontWeight = FontWeight.Light,
            fontSize   = 11.sp,
            color      = TextTertiary,
            textAlign  = TextAlign.Center,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun AmbientOrb(
    color    : Color,
    size     : androidx.compose.ui.unit.Dp,
    modifier : Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun FeaturePill(label: String, background: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(background)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text          = label,
            fontWeight    = FontWeight.Normal,
            fontSize      = 12.sp,
            color         = CharcoalDark,
            letterSpacing = 0.2.sp,
        )
    }
}

@Composable
private fun GoogleSignInButton(
    isLoading : Boolean,
    onClick   : () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label         = "btnScale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier         = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isLoading) CharcoalDark.copy(alpha = 0.75f) else CharcoalDark)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = !isLoading,
                onClick           = onClick,
            ),
    ) {
        // Label + Google G  (hidden while loading)
        AnimatedVisibility(
            visible = !isLoading,
            enter   = fadeIn(tween(200)),
            exit    = fadeOut(tween(150)),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Google "G" badge
                Icon(
                    painter = painterResource(R.drawable.ic_google_logo),
                    contentDescription = "Google Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(20.dp)
                )

                Text(
                    text          = "Continue with Google",
                    fontWeight    = FontWeight.Medium,
                    fontSize      = 15.sp,
                    color         = Cream,
                    letterSpacing = 0.1.sp,
                )
            }
        }

        // Spinner (visible while loading)
        AnimatedVisibility(
            visible = isLoading,
            enter   = fadeIn(tween(200)),
            exit    = fadeOut(tween(150)),
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CircularProgressIndicator(
                    modifier        = Modifier.size(20.dp),
                    color           = Cream,
                    trackColor      = Cream.copy(alpha = 0.25f),
                    strokeWidth     = 2.dp,
                    strokeCap       = StrokeCap.Round,
                )
                Text(
                    text          = "Signing you in…",
                    fontWeight    = FontWeight.Medium,
                    fontSize      = 15.sp,
                    color         = Cream.copy(alpha = 0.85f),
                    letterSpacing = 0.1.sp,
                )
            }
        }
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Idle")
@Composable
private fun LoginScreenIdlePreview() {
    ReMindTheme { LoginScreen(onLoginClick = {}) }
}

@Preview(showBackground = true, showSystemUi = true, name = "Loading")
@Composable
private fun LoginScreenLoadingPreview() {
    ReMindTheme { LoginScreen(onLoginClick = {}, isLoading = true) }
}