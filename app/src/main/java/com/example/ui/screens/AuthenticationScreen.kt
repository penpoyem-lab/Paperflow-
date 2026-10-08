package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthFieldTarget
import com.example.auth.AuthResult
import com.example.auth.AuthSessionState
import com.example.auth.PaperflowAuthManager
import com.example.ui.components.PlayStoreOrganicBlobSpinner
import com.example.ui.theme.CrystalTeal
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LiquidCyan
import com.example.ui.theme.LiquidMagenta
import com.example.ui.theme.PrismPurple
import com.example.ui.theme.PrismViolet
import com.example.ui.theme.SolarAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AuthMode {
    SIGN_IN,
    CREATE_ACCOUNT,
    FORGOT_PASSWORD,
    FORGOT_PASSWORD_INBOX_SENT,
    PASSWORD_RESET,
    EMAIL_VERIFICATION,
    SUCCESS
}

@Composable
fun AuthenticationScreen(
    authManager: PaperflowAuthManager,
    sessionState: AuthSessionState,
    onAuthCompleted: () -> Unit,
    onCloseOrDismiss: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    // Form States
    var fullName by remember { mutableStateOf(sessionState.userFullName) }
    var email by remember { mutableStateOf(sessionState.userEmail) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(sessionState.rememberMe) }

    // Verification / Reset hints for local vault testing
    var generatedCodeHint by remember { mutableStateOf<String?>(null) }
    var successUserName by remember { mutableStateOf("") }

    // Loading & Error States
    var isAuthenticating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var errorFieldTarget by remember { mutableStateOf(AuthFieldTarget.GENERAL) }
    var oauthProviderNotice by remember { mutableStateOf<String?>(null) }
    var footerDialogInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Subtle horizontal shake animation for error states (never shakes the entire screen)
    val shakeOffset = remember { Animatable(0f) }

    suspend fun triggerSubtleFieldShake() {
        val keyframes = listOf(-10f, 10f, -7f, 7f, -3f, 3f, 0f)
        for (value in keyframes) {
            shakeOffset.animateTo(value, animationSpec = tween(durationMillis = 42))
        }
    }

    BackHandler(enabled = authMode != AuthMode.SIGN_IN || onCloseOrDismiss != null) {
        if (authMode != AuthMode.SIGN_IN && authMode != AuthMode.SUCCESS) {
            errorMessage = null
            authMode = AuthMode.SIGN_IN
        } else {
            onCloseOrDismiss?.invoke()
        }
    }

    // 1. Entry Animation for the Floating Glass Authentication Card
    // Starts slightly lower and smaller -> fades in -> moves upward -> scales smoothly -> increases glass glow -> settles naturally
    var cardEntered by remember { mutableStateOf(false) }
    var titleEntered by remember { mutableStateOf(false) }
    var subtitleEntered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(60)
        cardEntered = true
        delay(130)
        titleEntered = true
        delay(110)
        subtitleEntered = true
    }

    val cardScale by animateFloatAsState(
        targetValue = if (cardEntered) 1f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "card_scale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (cardEntered) 1f else 0f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "card_alpha"
    )
    val cardOffsetY by animateDpAsState(
        targetValue = if (cardEntered) 0.dp else 36.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "card_offset_y"
    )

    // 2. Continuously Animated Dark Liquid-Glass Background Orbs
    val infiniteTransition = rememberInfiniteTransition(label = "auth_ambient_bg")
    val orbDriftA by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_drift_a"
    )
    val orbDriftB by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_drift_b"
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07060E))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .testTag("paperflow_auth_screen")
    ) {
        // Slowly moving blurred ambient light orbs behind the dark glass panel
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Deep dark violet/navy base gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF140B29),
                        Color(0xFF0B0A1A),
                        Color(0xFF090816),
                        Color(0xFF1A0B2E)
                    )
                )
            )

            // Top-right Violet / Magenta ambient refraction orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PrismPurple.copy(alpha = 0.26f * glowPulse),
                        IridescentPink.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(w * (0.82f - 0.18f * orbDriftA), h * (0.12f + 0.08f * orbDriftB)),
                    radius = w * 0.78f
                ),
                radius = w * 0.78f,
                center = Offset(w * (0.82f - 0.18f * orbDriftA), h * (0.12f + 0.08f * orbDriftB))
            )

            // Center-left Electric Blue / Cyan ambient orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.22f * glowPulse),
                        LiquidCyan.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * (0.15f + 0.16f * orbDriftB), h * (0.46f - 0.09f * orbDriftA)),
                    radius = w * 0.75f
                ),
                radius = w * 0.75f,
                center = Offset(w * (0.15f + 0.16f * orbDriftB), h * (0.46f - 0.09f * orbDriftA))
            )

            // Bottom Magenta / Purple ambient glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        IridescentPink.copy(alpha = 0.24f * glowPulse),
                        PrismViolet.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(w * (0.65f - 0.20f * orbDriftB), h * (0.88f - 0.05f * orbDriftA)),
                    radius = w * 0.82f
                ),
                radius = w * 0.82f,
                center = Offset(w * (0.65f - 0.20f * orbDriftB), h * (0.88f - 0.05f * orbDriftA))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar (Back / Close button when navigated from Home/Settings or sub-flows)
            Row(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (authMode != AuthMode.SIGN_IN && authMode != AuthMode.SUCCESS) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                            .clickable {
                                errorMessage = null
                                authMode = AuthMode.SIGN_IN
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Sign In",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else if (onCloseOrDismiss != null) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                            .testTag("auth_close_button")
                            .clickable(onClick = onCloseOrDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close authentication",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(46.dp))
                }

                // Subtle Paperflow Security Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(CrystalTeal)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "PAPERFLOW ID",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.3.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Large Floating Dark Translucent Glass Authentication Card
            val cardShape = RoundedCornerShape(34.dp)
            Box(
                modifier = Modifier
                    .widthIn(max = 460.dp)
                    .fillMaxWidth()
                    .offset(y = cardOffsetY)
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .graphicsLayer(
                        scaleX = cardScale,
                        scaleY = cardScale,
                        alpha = cardAlpha
                    )
                    .shadow(
                        elevation = 28.dp,
                        shape = cardShape,
                        ambientColor = PrismPurple.copy(alpha = 0.35f * glowPulse),
                        spotColor = ElectricBlue.copy(alpha = 0.35f * glowPulse)
                    )
                    .clip(cardShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF151324).copy(alpha = 0.88f),
                                Color(0xFF110F1D).copy(alpha = 0.92f),
                                Color(0xFF141122).copy(alpha = 0.90f)
                            )
                        )
                    )
                    .drawBehind {
                        // Top inner glass highlight + subtle edge refraction
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.09f),
                                    Color.Transparent,
                                    PrismPurple.copy(alpha = 0.06f)
                                )
                            ),
                            cornerRadius = CornerRadius(34.dp.toPx(), 34.dp.toPx())
                        )
                    }
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.26f),
                                PrismViolet.copy(alpha = 0.32f),
                                LiquidCyan.copy(alpha = 0.22f),
                                IridescentPink.copy(alpha = 0.28f),
                                Color.White.copy(alpha = 0.16f)
                            )
                        ),
                        shape = cardShape
                    )
                    .padding(horizontal = 24.dp, vertical = 30.dp)
            ) {
                AnimatedContent(
                    targetState = authMode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(320)) +
                            slideInHorizontally(animationSpec = tween(320)) { if (targetState.ordinal > initialState.ordinal) it / 5 else -it / 5 } +
                            scaleIn(initialScale = 0.96f, animationSpec = tween(320)))
                            .togetherWith(
                                fadeOut(animationSpec = tween(240)) +
                                    slideOutHorizontally(animationSpec = tween(240)) { if (targetState.ordinal > initialState.ordinal) -it / 5 else it / 5 } +
                                    scaleOut(targetScale = 0.96f, animationSpec = tween(240))
                            )
                            .using(SizeTransform(clip = false))
                    },
                    label = "auth_mode_transition"
                ) { mode ->
                    when (mode) {
                        AuthMode.SIGN_IN -> {
                            SignInFormContent(
                                titleEntered = titleEntered,
                                subtitleEntered = subtitleEntered,
                                email = email,
                                onEmailChange = {
                                    email = it
                                    if (errorFieldTarget == AuthFieldTarget.EMAIL || errorFieldTarget == AuthFieldTarget.GENERAL) {
                                        errorMessage = null
                                    }
                                },
                                password = password,
                                onPasswordChange = {
                                    password = it
                                    if (errorFieldTarget == AuthFieldTarget.PASSWORD || errorFieldTarget == AuthFieldTarget.GENERAL) {
                                        errorMessage = null
                                    }
                                },
                                rememberMe = rememberMe,
                                onRememberMeChange = { rememberMe = it },
                                isAuthenticating = isAuthenticating,
                                errorMessage = errorMessage,
                                errorFieldTarget = errorFieldTarget,
                                onForgotPasswordClick = {
                                    errorMessage = null
                                    authMode = AuthMode.FORGOT_PASSWORD
                                },
                                onSignInSubmit = {
                                    if (isAuthenticating) return@SignInFormContent
                                    focusManager.clearFocus()
                                    scope.launch {
                                        isAuthenticating = true
                                        errorMessage = null
                                        val result = authManager.signIn(email, password, rememberMe)
                                        isAuthenticating = false
                                        when (result) {
                                            is AuthResult.Success -> {
                                                successUserName = result.fullName
                                                authMode = AuthMode.SUCCESS
                                            }
                                            is AuthResult.Error -> {
                                                errorMessage = result.message
                                                errorFieldTarget = result.fieldTarget
                                                triggerSubtleFieldShake()
                                            }
                                            else -> {}
                                        }
                                    }
                                },
                                onGoogleClick = {
                                    oauthProviderNotice = "Google OAuth Provider Setup Required\n\nPaperflow operates in local-first privacy mode and does not fake OAuth sign-ins. To enable Google Sign-In, configure an OAuth 2.0 Web Client ID for package com.aistudio.glasspaper.vzxkqp or sign in with your local Paperflow account."
                                },
                                onAppleClick = {
                                    oauthProviderNotice = "Sign in with Apple Configuration Required\n\nPaperflow does not simulate fake OAuth responses. Configure an Apple Services ID & OAuth redirect URI to enable Apple authentication, or create a local Paperflow account below."
                                },
                                onSwitchToCreateAccount = {
                                    errorMessage = null
                                    authMode = AuthMode.CREATE_ACCOUNT
                                }
                            )
                        }

                        AuthMode.CREATE_ACCOUNT -> {
                            CreateAccountFormContent(
                                fullName = fullName,
                                onFullNameChange = {
                                    fullName = it
                                    if (errorFieldTarget == AuthFieldTarget.FULL_NAME) errorMessage = null
                                },
                                email = email,
                                onEmailChange = {
                                    email = it
                                    if (errorFieldTarget == AuthFieldTarget.EMAIL) errorMessage = null
                                },
                                password = password,
                                onPasswordChange = {
                                    password = it
                                    if (errorFieldTarget == AuthFieldTarget.PASSWORD) errorMessage = null
                                },
                                confirmPassword = confirmPassword,
                                onConfirmPasswordChange = {
                                    confirmPassword = it
                                    if (errorFieldTarget == AuthFieldTarget.CONFIRM_PASSWORD) errorMessage = null
                                },
                                isAuthenticating = isAuthenticating,
                                errorMessage = errorMessage,
                                errorFieldTarget = errorFieldTarget,
                                onCreateAccountSubmit = {
                                    if (isAuthenticating) return@CreateAccountFormContent
                                    focusManager.clearFocus()
                                    scope.launch {
                                        isAuthenticating = true
                                        errorMessage = null
                                        val result = authManager.registerAccount(
                                            fullName = fullName,
                                            email = email,
                                            password = password,
                                            confirmPassword = confirmPassword
                                        )
                                        isAuthenticating = false
                                        when (result) {
                                            is AuthResult.VerificationRequired -> {
                                                generatedCodeHint = result.verificationCodeHint
                                                verificationCode = result.verificationCodeHint
                                                authMode = AuthMode.EMAIL_VERIFICATION
                                            }
                                            is AuthResult.Error -> {
                                                errorMessage = result.message
                                                errorFieldTarget = result.fieldTarget
                                                triggerSubtleFieldShake()
                                            }
                                            else -> {}
                                        }
                                    }
                                },
                                onSwitchToSignIn = {
                                    errorMessage = null
                                    authMode = AuthMode.SIGN_IN
                                }
                            )
                        }

                        AuthMode.FORGOT_PASSWORD -> {
                            ForgotPasswordFormContent(
                                email = email,
                                onEmailChange = {
                                    email = it
                                    errorMessage = null
                                },
                                isAuthenticating = isAuthenticating,
                                errorMessage = errorMessage,
                                onSendResetLink = {
                                    if (isAuthenticating) return@ForgotPasswordFormContent
                                    focusManager.clearFocus()
                                    scope.launch {
                                        isAuthenticating = true
                                        errorMessage = null
                                        val result = authManager.requestPasswordReset(email)
                                        isAuthenticating = false
                                        when (result) {
                                            is AuthResult.ResetCodeGenerated -> {
                                                generatedCodeHint = result.resetTokenHint
                                                verificationCode = result.resetTokenHint
                                                authMode = AuthMode.FORGOT_PASSWORD_INBOX_SENT
                                            }
                                            is AuthResult.Error -> {
                                                errorMessage = result.message
                                                errorFieldTarget = result.fieldTarget
                                                triggerSubtleFieldShake()
                                            }
                                            else -> {}
                                        }
                                    }
                                },
                                onBackToSignIn = {
                                    errorMessage = null
                                    authMode = AuthMode.SIGN_IN
                                }
                            )
                        }

                        AuthMode.FORGOT_PASSWORD_INBOX_SENT -> {
                            CheckYourInboxContent(
                                email = email,
                                resetCodeHint = generatedCodeHint,
                                onProceedToResetPassword = {
                                    errorMessage = null
                                    password = ""
                                    confirmPassword = ""
                                    authMode = AuthMode.PASSWORD_RESET
                                },
                                onBackToSignIn = {
                                    errorMessage = null
                                    authMode = AuthMode.SIGN_IN
                                }
                            )
                        }

                        AuthMode.PASSWORD_RESET -> {
                            PasswordResetFormContent(
                                email = email,
                                resetCode = verificationCode,
                                onResetCodeChange = {
                                    verificationCode = it
                                    errorMessage = null
                                },
                                newPassword = password,
                                onNewPasswordChange = {
                                    password = it
                                    errorMessage = null
                                },
                                confirmNewPassword = confirmPassword,
                                onConfirmNewPasswordChange = {
                                    confirmPassword = it
                                    errorMessage = null
                                },
                                isAuthenticating = isAuthenticating,
                                errorMessage = errorMessage,
                                onConfirmReset = {
                                    if (isAuthenticating) return@PasswordResetFormContent
                                    focusManager.clearFocus()
                                    scope.launch {
                                        isAuthenticating = true
                                        errorMessage = null
                                        val result = authManager.confirmPasswordReset(
                                            email = email,
                                            resetCode = verificationCode,
                                            newPassword = password,
                                            confirmNewPassword = confirmPassword
                                        )
                                        isAuthenticating = false
                                        when (result) {
                                            is AuthResult.Success -> {
                                                successUserName = result.fullName
                                                authMode = AuthMode.SUCCESS
                                            }
                                            is AuthResult.Error -> {
                                                errorMessage = result.message
                                                errorFieldTarget = result.fieldTarget
                                                triggerSubtleFieldShake()
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            )
                        }

                        AuthMode.EMAIL_VERIFICATION -> {
                            EmailVerificationContent(
                                email = email,
                                verificationCode = verificationCode,
                                onCodeChange = {
                                    verificationCode = it
                                    errorMessage = null
                                },
                                codeHint = generatedCodeHint,
                                isAuthenticating = isAuthenticating,
                                errorMessage = errorMessage,
                                onVerifySubmit = {
                                    if (isAuthenticating) return@EmailVerificationContent
                                    focusManager.clearFocus()
                                    scope.launch {
                                        isAuthenticating = true
                                        errorMessage = null
                                        val result = authManager.verifyEmailCode(email, verificationCode)
                                        isAuthenticating = false
                                        when (result) {
                                            is AuthResult.Success -> {
                                                successUserName = result.fullName
                                                authMode = AuthMode.SUCCESS
                                            }
                                            is AuthResult.Error -> {
                                                errorMessage = result.message
                                                errorFieldTarget = result.fieldTarget
                                                triggerSubtleFieldShake()
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            )
                        }

                        AuthMode.SUCCESS -> {
                            AuthSuccessAnimationContent(
                                userName = successUserName.ifBlank { fullName.ifBlank { "Reader" } },
                                onFinished = onAuthCompleted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Footer: © 2026 Paperflow  •  Privacy  •  Terms + Encrypted Status Indicator
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "© 2026 Paperflow",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.4.sp
                        ),
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "Privacy",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.4.sp
                        ),
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.clickable {
                            footerDialogInfo = "Paperflow Privacy" to "Your credentials are hashed locally using 256-bit random salt and 4,096 rounds of SHA-256. Your PDF documents, annotations, and study notes remain strictly on your device."
                        }
                    )
                    Text(
                        text = "Terms",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.4.sp
                        ),
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.clickable {
                            footerDialogInfo = "Paperflow Terms" to "Paperflow is a local-first PDF Reader, Annotation Studio, and Document Utility Suite. You retain 100% ownership and offline control of your files."
                        }
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(CrystalTeal.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(CrystalTeal)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Secure & encrypted • Local SHA-256 Vault",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }

    // OAuth Configuration Notice Dialog (No fake OAuth)
    oauthProviderNotice?.let { notice ->
        AlertDialog(
            onDismissRequest = { oauthProviderNotice = null },
            title = { Text("OAuth Provider Configuration") },
            text = { Text(notice) },
            confirmButton = {
                Button(onClick = { oauthProviderNotice = null }) {
                    Text("Got It")
                }
            }
        )
    }

    footerDialogInfo?.let { (title, body) ->
        AlertDialog(
            onDismissRequest = { footerDialogInfo = null },
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                Button(onClick = { footerDialogInfo = null }) {
                    Text("Done")
                }
            }
        )
    }
}

// ==================== 1. SIGN IN FORM CONTENT ====================

@Composable
private fun SignInFormContent(
    titleEntered: Boolean,
    subtitleEntered: Boolean,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    isAuthenticating: Boolean,
    errorMessage: String?,
    errorFieldTarget: AuthFieldTarget,
    onForgotPasswordClick: () -> Unit,
    onSignInSubmit: () -> Unit,
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit,
    onSwitchToCreateAccount: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    // Title & Subtitle Smooth Settling Animations
    val titleAlpha by animateFloatAsState(
        targetValue = if (titleEntered) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "title_alpha"
    )
    val titleOffsetY by animateDpAsState(
        targetValue = if (titleEntered) 0.dp else 14.dp,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "title_offset"
    )
    val titleLetterSpacing by animateFloatAsState(
        targetValue = if (titleEntered) 0.3f else 2.2f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "title_spacing"
    )
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (subtitleEntered) 1f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "subtitle_alpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column {
            Text(
                text = "Welcome back",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 31.sp,
                    letterSpacing = titleLetterSpacing.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .offset(y = titleOffsetY)
                    .alpha(titleAlpha)
                    .testTag("auth_welcome_title")
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sign in to continue to Paperflow",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.5.sp
                ),
                color = Color(0xFFB4BCD0),
                modifier = Modifier.alpha(subtitleAlpha)
            )
        }

        // Animated Error Banner if present
        AnimatedErrorBanner(errorMessage = errorMessage)

        // Email Address Field
        GlassAuthInputField(
            label = "Email address",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "you@company.com",
            isValid = PaperflowAuthManager.isValidEmail(email),
            isError = errorFieldTarget == AuthFieldTarget.EMAIL && errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            testTag = "auth_email_input"
        )

        // Password Field + "Forgot password?" link
        GlassAuthPasswordField(
            label = "Password",
            value = password,
            onValueChange = onPasswordChange,
            forgotPasswordLabel = "Forgot password?",
            onForgotPasswordClick = onForgotPasswordClick,
            isError = errorFieldTarget == AuthFieldTarget.PASSWORD && errorMessage != null,
            imeAction = ImeAction.Done,
            onImeDone = onSignInSubmit,
            testTag = "auth_password_input"
        )

        // Custom Animated "Remember me" Glass Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { onRememberMeChange(!rememberMe) }
                .padding(vertical = 4.dp)
                .testTag("auth_remember_me")
        ) {
            AnimatedGlassCheckbox(
                checked = rememberMe,
                onCheckedChange = onRememberMeChange
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Remember me",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.4.sp
                ),
                color = Color(0xFFCBD5E1)
            )
        }

        // Main Liquid-Gradient Glass Login Button: "Continue to Paperflow ->"
        LiquidGradientAuthButton(
            text = "Continue to Paperflow",
            loadingText = "Authenticating...",
            isLoading = isAuthenticating,
            onClick = onSignInSubmit,
            testTag = "auth_submit_button"
        )

        // Divider: OR CONTINUE WITH
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Text(
                text = "OR CONTINUE WITH",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 2.0.sp
                ),
                color = Color(0xFF64748B),
                modifier = Modifier.padding(horizontal = 14.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f))
            )
        }

        // Alternative OAuth Glass Buttons: Google & Apple
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OAuthGlassButton(
                label = "Google",
                isGoogle = true,
                onClick = onGoogleClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("auth_google_button")
            )
            OAuthGlassButton(
                label = "Apple",
                isGoogle = false,
                onClick = onAppleClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("auth_apple_button")
            )
        }

        // Bottom: "Don't have an account? Create account"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.3.sp
                ),
                color = Color(0xFF94A3B8)
            )
            Text(
                text = "Create account",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    textDecoration = TextDecoration.Underline,
                    letterSpacing = 0.3.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .testTag("auth_switch_create_account")
                    .clickable(onClick = onSwitchToCreateAccount)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

// ==================== 2. CREATE ACCOUNT FORM CONTENT ====================

@Composable
private fun CreateAccountFormContent(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    isAuthenticating: Boolean,
    errorMessage: String?,
    errorFieldTarget: AuthFieldTarget,
    onCreateAccountSubmit: () -> Unit,
    onSwitchToSignIn: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Create account",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 30.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Set up your encrypted Paperflow workspace",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontStyle = FontStyle.Italic
                ),
                color = Color(0xFFB4BCD0)
            )
        }

        AnimatedErrorBanner(errorMessage = errorMessage)

        GlassAuthInputField(
            label = "Full name",
            value = fullName,
            onValueChange = onFullNameChange,
            placeholder = "Alex Rivera",
            isValid = fullName.trim().length >= 2,
            isError = errorFieldTarget == AuthFieldTarget.FULL_NAME && errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            testTag = "register_name_input"
        )

        GlassAuthInputField(
            label = "Email",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "you@company.com",
            isValid = PaperflowAuthManager.isValidEmail(email),
            isError = errorFieldTarget == AuthFieldTarget.EMAIL && errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            testTag = "register_email_input"
        )

        GlassAuthPasswordField(
            label = "Password",
            value = password,
            onValueChange = onPasswordChange,
            forgotPasswordLabel = null,
            onForgotPasswordClick = null,
            isError = errorFieldTarget == AuthFieldTarget.PASSWORD && errorMessage != null,
            imeAction = ImeAction.Next,
            onImeDone = { focusManager.moveFocus(FocusDirection.Down) },
            testTag = "register_password_input"
        )

        GlassAuthPasswordField(
            label = "Confirm password",
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            forgotPasswordLabel = null,
            onForgotPasswordClick = null,
            isError = errorFieldTarget == AuthFieldTarget.CONFIRM_PASSWORD && errorMessage != null,
            imeAction = ImeAction.Done,
            onImeDone = onCreateAccountSubmit,
            testTag = "register_confirm_password_input"
        )

        Spacer(modifier = Modifier.height(4.dp))

        LiquidGradientAuthButton(
            text = "Create Paperflow Account",
            loadingText = "Creating account...",
            isLoading = isAuthenticating,
            onClick = onCreateAccountSubmit,
            testTag = "register_submit_button"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = Color(0xFF94A3B8)
            )
            Text(
                text = "Sign in",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    textDecoration = TextDecoration.Underline
                ),
                color = Color.White,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSwitchToSignIn)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}

// ==================== 3. FORGOT PASSWORD & INBOX SENT ====================

@Composable
private fun ForgotPasswordFormContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isAuthenticating: Boolean,
    errorMessage: String?,
    onSendResetLink: () -> Unit,
    onBackToSignIn: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column {
            Text(
                text = "Reset your password",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 28.sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Enter your email and we'll help you regain access.",
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = Color(0xFFB4BCD0)
            )
        }

        AnimatedErrorBanner(errorMessage = errorMessage)

        GlassAuthInputField(
            label = "Email address",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "you@company.com",
            isValid = PaperflowAuthManager.isValidEmail(email),
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSendResetLink() }),
            testTag = "forgot_email_input"
        )

        LiquidGradientAuthButton(
            text = "Send Reset Link",
            loadingText = "Sending reset link...",
            isLoading = isAuthenticating,
            onClick = onSendResetLink,
            testTag = "forgot_submit_button"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Remembered your password? Sign in",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    textDecoration = TextDecoration.Underline
                ),
                color = Color(0xFFCBD5E1),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onBackToSignIn)
                    .padding(6.dp)
            )
        }
    }
}

@Composable
private fun CheckYourInboxContent(
    email: String,
    resetCodeHint: String?,
    onProceedToResetPassword: () -> Unit,
    onBackToSignIn: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(listOf(ElectricBlue.copy(alpha = 0.35f), LiquidCyan.copy(alpha = 0.25f)))
                )
                .border(1.5.dp, LiquidCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MarkEmailRead,
                contentDescription = null,
                tint = LiquidCyan,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = "Check your inbox",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic
            ),
            color = Color.White
        )

        Text(
            text = "We generated a 6-digit recovery token for $email.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center
        )

        if (!resetCodeHint.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ElectricBlue.copy(alpha = 0.14f))
                    .border(1.dp, LiquidCyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Local Vault Recovery Code: $resetCodeHint",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = LiquidCyan
                )
            }
        }

        LiquidGradientAuthButton(
            text = "Enter Reset Code",
            loadingText = "Opening...",
            isLoading = false,
            onClick = onProceedToResetPassword,
            testTag = "proceed_password_reset_button"
        )

        Text(
            text = "Back to Sign in",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontStyle = FontStyle.Italic,
                textDecoration = TextDecoration.Underline
            ),
            color = Color(0xFF94A3B8),
            modifier = Modifier.clickable(onClick = onBackToSignIn)
        )
    }
}

// ==================== 4. PASSWORD RESET & EMAIL VERIFICATION ====================

@Composable
private fun PasswordResetFormContent(
    email: String,
    resetCode: String,
    onResetCodeChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    confirmNewPassword: String,
    onConfirmNewPasswordChange: (String) -> Unit,
    isAuthenticating: Boolean,
    errorMessage: String?,
    onConfirmReset: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Set new password",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = 28.sp
            ),
            color = Color.White
        )
        Text(
            text = "Resetting password for $email",
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = Color(0xFFB4BCD0)
        )

        AnimatedErrorBanner(errorMessage = errorMessage)

        GlassAuthInputField(
            label = "6-digit reset code",
            value = resetCode,
            onValueChange = onResetCodeChange,
            placeholder = "123456",
            isValid = resetCode.trim().length == 6,
            isError = false,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            testTag = "reset_code_input"
        )

        GlassAuthPasswordField(
            label = "New password",
            value = newPassword,
            onValueChange = onNewPasswordChange,
            forgotPasswordLabel = null,
            onForgotPasswordClick = null,
            isError = false,
            imeAction = ImeAction.Next,
            onImeDone = { focusManager.moveFocus(FocusDirection.Down) },
            testTag = "reset_new_password_input"
        )

        GlassAuthPasswordField(
            label = "Confirm new password",
            value = confirmNewPassword,
            onValueChange = onConfirmNewPasswordChange,
            forgotPasswordLabel = null,
            onForgotPasswordClick = null,
            isError = false,
            imeAction = ImeAction.Done,
            onImeDone = onConfirmReset,
            testTag = "reset_confirm_password_input"
        )

        LiquidGradientAuthButton(
            text = "Update Password",
            loadingText = "Updating password...",
            isLoading = isAuthenticating,
            onClick = onConfirmReset,
            testTag = "reset_submit_button"
        )
    }
}

@Composable
private fun EmailVerificationContent(
    email: String,
    verificationCode: String,
    onCodeChange: (String) -> Unit,
    codeHint: String?,
    isAuthenticating: Boolean,
    errorMessage: String?,
    onVerifySubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Verify your email",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic,
                fontSize = 28.sp
            ),
            color = Color.White
        )
        Text(
            text = "Enter the 6-digit verification code for $email",
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = Color(0xFFB4BCD0)
        )

        if (!codeHint.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ElectricBlue.copy(alpha = 0.16f))
                    .border(1.dp, LiquidCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Local Verification Code: $codeHint (Auto-filled for convenience)",
                    style = MaterialTheme.typography.labelMedium,
                    color = LiquidCyan
                )
            }
        }

        AnimatedErrorBanner(errorMessage = errorMessage)

        GlassAuthInputField(
            label = "Verification code",
            value = verificationCode,
            onValueChange = onCodeChange,
            placeholder = "6-digit code",
            isValid = verificationCode.trim().length == 6,
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onVerifySubmit() }),
            testTag = "verify_code_input"
        )

        LiquidGradientAuthButton(
            text = "Verify & Continue",
            loadingText = "Verifying...",
            isLoading = isAuthenticating,
            onClick = onVerifySubmit,
            testTag = "verify_submit_button"
        )
    }
}

// ==================== 5. SUCCESS ANIMATION TRANSITION ====================

@Composable
private fun AuthSuccessAnimationContent(
    userName: String,
    onFinished: () -> Unit
) {
    val checkProgress = remember { Animatable(0f) }
    val glowRadius = remember { Animatable(0.4f) }

    LaunchedEffect(Unit) {
        checkProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
        )
        glowRadius.animateTo(
            targetValue = 1.15f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        delay(650)
        onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val r = size.minDimension / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LiquidCyan.copy(alpha = 0.45f),
                            ElectricBlue.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        radius = r * 1.4f * glowRadius.value
                    ),
                    radius = r * 1.4f * glowRadius.value
                )
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(ElectricBlue, PrismViolet, IridescentPink)
                    ),
                    radius = r * 0.78f
                )

                // Animated Checkmark Path
                val p = checkProgress.value
                val path = Path().apply {
                    val startX = size.width * 0.32f
                    val startY = size.height * 0.52f
                    val midX = size.width * 0.46f
                    val midY = size.height * 0.65f
                    val endX = size.width * 0.68f
                    val endY = size.height * 0.38f

                    moveTo(startX, startY)
                    if (p <= 0.45f) {
                        val t = p / 0.45f
                        lineTo(startX + (midX - startX) * t, startY + (midY - startY) * t)
                    } else {
                        lineTo(midX, midY)
                        val t = (p - 0.45f) / 0.55f
                        lineTo(midX + (endX - midX) * t, midY + (endY - midY) * t)
                    }
                }
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }

        Text(
            text = "Welcome to Paperflow",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Signed in as $userName • Preparing your Liquid Glass library...",
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            color = Color(0xFFCBD5E1),
            textAlign = TextAlign.Center
        )
    }
}

// ==================== REUSABLE LIQUID GLASS AUTH COMPONENTS ====================

@Composable
private fun GlassAuthInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isValid: Boolean,
    isError: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    testTag: String
) {
    var isFocused by remember { mutableStateOf(false) }

    val fieldScale by animateFloatAsState(
        targetValue = if (isFocused) 1.012f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "input_scale"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> LiquidMagenta.copy(alpha = 0.85f)
            isFocused -> LiquidCyan.copy(alpha = 0.85f)
            isValid && value.isNotEmpty() -> CrystalTeal.copy(alpha = 0.55f)
            else -> Color.White.copy(alpha = 0.12f)
        },
        animationSpec = tween(220),
        label = "input_border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF1A182B) else Color(0xFF12101E),
        animationSpec = tween(220),
        label = "input_bg"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontStyle = FontStyle.Italic,
                letterSpacing = 0.5.sp
            ),
            color = if (isFocused) LiquidCyan else Color(0xFFE2E8F0)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(scaleX = fieldScale, scaleY = fieldScale)
                .shadow(
                    elevation = if (isFocused) 12.dp else 0.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = if (isError) LiquidMagenta else ElectricBlue,
                    spotColor = if (isError) LiquidMagenta else LiquidCyan
                )
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor)
                .border(
                    width = if (isFocused || isError) 1.4.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = FontStyle.Italic,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color(0xFF64748B)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            letterSpacing = 0.4.sp
                        ),
                        cursorBrush = SolidColor(LiquidCyan),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isFocused = it.isFocused }
                            .testTag(testTag)
                    )
                }

                AnimatedVisibility(
                    visible = isValid && value.isNotEmpty() && !isError,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Valid input",
                        tint = CrystalTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassAuthPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    forgotPasswordLabel: String?,
    onForgotPasswordClick: (() -> Unit)?,
    isError: Boolean,
    imeAction: ImeAction,
    onImeDone: () -> Unit,
    testTag: String
) {
    var isFocused by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val fieldScale by animateFloatAsState(
        targetValue = if (isFocused) 1.012f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pwd_scale"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> LiquidMagenta.copy(alpha = 0.85f)
            isFocused -> LiquidCyan.copy(alpha = 0.85f)
            value.length >= 6 -> PrismViolet.copy(alpha = 0.55f)
            else -> Color.White.copy(alpha = 0.12f)
        },
        animationSpec = tween(220),
        label = "pwd_border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isFocused) Color(0xFF1A182B) else Color(0xFF12101E),
        animationSpec = tween(220),
        label = "pwd_bg"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.5.sp
                ),
                color = if (isFocused) LiquidCyan else Color(0xFFE2E8F0)
            )
            if (forgotPasswordLabel != null && onForgotPasswordClick != null) {
                Text(
                    text = forgotPasswordLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontStyle = FontStyle.Italic,
                        letterSpacing = 0.4.sp
                    ),
                    color = Color(0xFF94A3B8),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .testTag("auth_forgot_password")
                        .clickable(onClick = onForgotPasswordClick)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer(scaleX = fieldScale, scaleY = fieldScale)
                .shadow(
                    elevation = if (isFocused) 12.dp else 0.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = if (isError) LiquidMagenta else PrismViolet,
                    spotColor = if (isError) LiquidMagenta else LiquidCyan
                )
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor)
                .border(
                    width = if (isFocused || isError) 1.4.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "•••••••••••",
                            style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 2.sp),
                            color = Color(0xFF64748B)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White,
                            letterSpacing = if (passwordVisible) 0.4.sp else 2.sp
                        ),
                        cursorBrush = SolidColor(LiquidCyan),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = imeAction
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onImeDone() },
                            onNext = { onImeDone() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { isFocused = it.isFocused }
                            .testTag(testTag)
                    )
                }

                // Smoothly Animated Eye Visibility Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { passwordVisible = !passwordVisible },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = passwordVisible,
                        transitionSpec = {
                            (fadeIn(tween(180)) + scaleIn(initialScale = 0.7f, animationSpec = tween(180)))
                                .togetherWith(fadeOut(tween(150)) + scaleOut(targetScale = 0.7f, animationSpec = tween(150)))
                        },
                        label = "eye_visibility_anim"
                    ) { visible ->
                        Icon(
                            imageVector = if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (visible) "Hide password" else "Show password",
                            tint = if (visible) LiquidCyan else Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedGlassCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val checkProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "checkbox_draw"
    )
    val boxShape = RoundedCornerShape(7.dp)

    Box(
        modifier = Modifier
            .size(24.dp)
            .shadow(
                elevation = if (checked) 8.dp else 0.dp,
                shape = boxShape,
                ambientColor = LiquidCyan,
                spotColor = ElectricBlue
            )
            .clip(boxShape)
            .background(
                if (checked) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            ElectricBlue.copy(alpha = 0.45f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.White.copy(alpha = 0.03f)
                        )
                    )
                }
            )
            .border(
                width = 1.2.dp,
                color = if (checked) LiquidCyan.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.25f),
                shape = boxShape
            )
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checkProgress > 0.05f) {
            Canvas(modifier = Modifier.size(14.dp)) {
                val path = Path().apply {
                    val x1 = size.width * 0.15f
                    val y1 = size.height * 0.52f
                    val x2 = size.width * 0.42f
                    val y2 = size.height * 0.78f
                    val x3 = size.width * 0.86f
                    val y3 = size.height * 0.22f

                    moveTo(x1, y1)
                    if (checkProgress <= 0.45f) {
                        val t = checkProgress / 0.45f
                        lineTo(x1 + (x2 - x1) * t, y1 + (y2 - y1) * t)
                    } else {
                        lineTo(x2, y2)
                        val t = (checkProgress - 0.45f) / 0.55f
                        lineTo(x2 + (x3 - x2) * t, y2 + (y3 - y2) * t)
                    }
                }
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }
        }
    }
}

@Composable
private fun LiquidGradientAuthButton(
    text: String,
    loadingText: String,
    isLoading: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "btn_press_scale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "btn_shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "btn_shimmer_x"
    )

    val btnShape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .shadow(
                elevation = if (isPressed) 22.dp else 16.dp,
                shape = btnShape,
                ambientColor = ElectricBlue.copy(alpha = 0.6f),
                spotColor = IridescentPink.copy(alpha = 0.6f)
            )
            .clip(btnShape)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF0EA5E9), // Vibrant Cyan-Blue left like screenshot
                        Color(0xFF6366F1), // Electric Indigo
                        Color(0xFFA855F7), // Prism Purple
                        Color(0xFFEC4899)  // Iridescent Pink right
                    )
                )
            )
            .drawBehind {
                // Idle moving glass light reflection across the button surface
                val center = Offset(size.width * shimmerX, size.height * 0.5f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                        center = center,
                        radius = size.width * 0.42f
                    ),
                    radius = size.width * 0.42f,
                    center = center
                )
            }
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.85f),
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.75f)
                    )
                ),
                shape = btnShape
            )
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(tween(200)).togetherWith(fadeOut(tween(160)))
            },
            label = "button_loading_morph"
        ) { loading ->
            if (loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    PlayStoreOrganicBlobSpinner(
                        indicatorSize = 22.dp,
                        blobColor = Color(0xFF8EC5FF)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = loadingText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            letterSpacing = 0.6.sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OAuthGlassButton(
    label: String,
    isGoogle: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "oauth_scale"
    )
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .height(54.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(shape)
            .background(Color(0xFF141221))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (isPressed) 0.35f else 0.12f),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isGoogle) {
                GoogleBrandIcon(modifier = Modifier.size(20.dp))
            } else {
                AppleBrandIcon(modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontStyle = FontStyle.Italic,
                    letterSpacing = 0.6.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun GoogleBrandIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.22f
        val inset = stroke / 2f
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)

        // Red top
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = -145f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Yellow left
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 145f,
            sweepAngle = 70f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Green bottom
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        // Blue right + horizontal bar
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -10f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = StrokeCap.Butt)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(size.width * 0.52f, size.height * 0.5f),
            end = Offset(size.width * 0.92f, size.height * 0.5f),
            strokeWidth = stroke
        )
    }
}

@Composable
private fun AppleBrandIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Clean recognizable Apple silhouette
        val bodyPath = Path().apply {
            moveTo(w * 0.50f, h * 0.30f)
            cubicTo(w * 0.38f, h * 0.25f, w * 0.20f, h * 0.34f, w * 0.20f, h * 0.55f)
            cubicTo(w * 0.20f, h * 0.76f, w * 0.34f, h * 0.94f, w * 0.44f, h * 0.94f)
            cubicTo(w * 0.48f, h * 0.94f, w * 0.52f, h * 0.90f, w * 0.56f, h * 0.94f)
            cubicTo(w * 0.66f, h * 0.94f, w * 0.80f, h * 0.76f, w * 0.80f, h * 0.55f)
            cubicTo(w * 0.80f, h * 0.34f, w * 0.62f, h * 0.25f, w * 0.50f, h * 0.30f)
            close()
        }
        drawPath(path = bodyPath, color = Color.White)

        // Top leaf
        val leafPath = Path().apply {
            moveTo(w * 0.50f, h * 0.26f)
            cubicTo(w * 0.50f, h * 0.14f, w * 0.62f, h * 0.06f, w * 0.68f, h * 0.08f)
            cubicTo(w * 0.68f, h * 0.20f, w * 0.56f, h * 0.27f, w * 0.50f, h * 0.26f)
            close()
        }
        drawPath(path = leafPath, color = Color.White)
    }
}

@Composable
private fun AnimatedErrorBanner(errorMessage: String?) {
    AnimatedVisibility(
        visible = !errorMessage.isNullOrBlank(),
        enter = fadeIn(tween(220)) + slideInVertically { -it / 2 },
        exit = fadeOut(tween(180)) + slideOutVertically { -it / 2 }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(LiquidMagenta.copy(alpha = 0.16f))
                .border(1.dp, LiquidMagenta.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.ErrorOutline,
                contentDescription = "Error",
                tint = IridescentPink,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFFCE7F3)
            )
        }
    }
}
