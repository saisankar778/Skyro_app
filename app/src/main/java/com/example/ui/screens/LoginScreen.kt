package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SkyroViewModel
import com.example.ui.theme.SkyroColors
import com.example.ui.theme.SkyroTypography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: SkyroViewModel,
    isNight: Boolean = false,
    onLoginSuccess: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    
    var googleAccount by remember { mutableStateOf<String?>(null) }
    var googleName by remember { mutableStateOf<String?>(null) }
    var showGoogleChooser by remember { mutableStateOf(false) }
    
    // Auth display stages: "PHONE_ENTRY", "OTP_ENTRY", "SUCCESS"
    var authStage by remember { mutableStateOf("PHONE_ENTRY") }
    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifyingOtp by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableStateOf(30) }

    val coroutineScope = rememberCoroutineScope()

    val backgroundBrush = if (isNight) {
        Brush.verticalGradient(
            colors = listOf(
                SkyroColors.MidnightNav,
                SkyroColors.NightPurple,
                SkyroColors.NightGradientDeep
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFFFEEE3), // Subtle warm orange-cream
                SkyroColors.WarmCream,
                Color.White
            )
        )
    }

    val orangeColor = Color(0xFFFC8019) // Skyro Orange Accent Brand Color

    // Countdown Timer for OTP Resend
    LaunchedEffect(authStage) {
        if (authStage == "OTP_ENTRY") {
            timerSeconds = 30
            while (timerSeconds > 0) {
                delay(1000)
                timerSeconds--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        if (isNight) {
            com.example.ui.components.CustomStarfield()
        }

        AnimatedContent(
            targetState = authStage,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "LoginStagesAnimation"
        ) { stage ->
            when (stage) {
                "PHONE_ENTRY" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header branding
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 40.dp)
                        ) {
                            Text(
                                text = "✨ SKYRO",
                                style = SkyroTypography.Caption.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = orangeColor
                                ),
                                modifier = Modifier.testTag("login_badge_logo")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Create your account",
                                style = SkyroTypography.H1.copy(fontWeight = FontWeight.Black),
                                color = if (isNight) Color.White else Color.Black,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Verify with Google & your mobile number",
                                style = SkyroTypography.Body,
                                color = if (isNight) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Input Display Area
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Compulsory Google login banner container
                            if (googleAccount == null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                        .clickable { showGoogleChooser = true }
                                        .testTag("google_login_button"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isNight) Color(0xFF231535) else Color(0xFFF3F4F6)
                                    ),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (isNight) Color(0xFF7B2FFF) else Color(0xFFFC8019)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("🌐", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sign up with Google (Compulsory)",
                                            color = if (isNight) Color.White else Color.Black,
                                            style = SkyroTypography.Body.copy(fontWeight = FontWeight.Black, fontSize = 13.sp)
                                        )
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isNight) Color(0xFF14301A) else Color(0xFFE8F5E9)
                                    ),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (isNight) Color(0xFF2E7D32) else Color(0xFF4CAF50)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("✅", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = googleName ?: "Sai Sankar",
                                                color = if (isNight) Color(0xFF81C784) else Color(0xFF2E7D32),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = googleAccount ?: "",
                                                color = if (isNight) Color.White.copy(alpha = 0.7f) else Color.DarkGray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Text(
                                            text = "Change",
                                            color = orangeColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .clickable { googleAccount = null }
                                                .padding(horizontal = 6.dp)
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isNight) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f))
                                    .border(
                                        width = 1.5.dp,
                                        color = if (phoneNumber.length == 10 && googleAccount != null) orangeColor else Color.Transparent,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable(enabled = googleAccount == null) { showGoogleChooser = true }
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📱",
                                        fontSize = 20.sp,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "+91 ",
                                        style = SkyroTypography.Body.copy(fontWeight = FontWeight.Bold),
                                        color = if (isNight) Color.White else Color.Black
                                    )
                                    
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        if (phoneNumber.isEmpty()) {
                                            Text(
                                                text = if (googleAccount == null) "Google Sign-In Required" else "Enter 10 digit number",
                                                style = SkyroTypography.Body,
                                                color = if (googleAccount == null) orangeColor else (if (isNight) Color.White.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.7f))
                                            )
                                        }
                                        Text(
                                            text = phoneNumber,
                                            style = SkyroTypography.Body.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.sp
                                            ),
                                            color = if (isNight) Color.White else Color.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Continue Button
                            Button(
                                onClick = {
                                    if (googleAccount == null) {
                                        showGoogleChooser = true
                                    } else if (phoneNumber.length == 10 && !isSendingOtp) {
                                        isSendingOtp = true
                                        coroutineScope.launch {
                                            delay(1500) // Simulated network call
                                            isSendingOtp = false
                                            authStage = "OTP_ENTRY"
                                        }
                                    }
                                },
                                enabled = (googleAccount == null || phoneNumber.length == 10) && !isSendingOtp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("login_get_otp_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (googleAccount == null) Color.Gray else orangeColor,
                                    disabledContainerColor = orangeColor.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (isSendingOtp) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (googleAccount == null) "Complete Google Sign-In" else "Get OTP",
                                            color = Color.White,
                                            style = SkyroTypography.Body.copy(fontWeight = FontWeight.Black)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Filled.KeyboardArrowRight,
                                            contentDescription = "Next arrow",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Virtual Number Pad for pristine tactile touch feel
                        VirtualKeyboard(
                            onKeyPress = { code ->
                                if (googleAccount == null) {
                                    showGoogleChooser = true
                                } else if (phoneNumber.length < 10) {
                                    phoneNumber += code
                                    if (phoneNumber.length == 10 && !isSendingOtp) {
                                        isSendingOtp = true
                                        coroutineScope.launch {
                                            delay(1500) // Simulated network call
                                            isSendingOtp = false
                                            authStage = "OTP_ENTRY"
                                        }
                                    }
                                }
                            },
                            onBackspace = {
                                if (phoneNumber.isNotEmpty()) {
                                    phoneNumber = phoneNumber.dropLast(1)
                                }
                            },
                            isNight = isNight
                        )
                    }
                }

                "OTP_ENTRY" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 40.dp)
                        ) {
                            Text(
                                text = "OTP VERIFICATION",
                                style = SkyroTypography.Caption.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 2.sp,
                                    color = orangeColor
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Verify your mobile",
                                style = SkyroTypography.H1.copy(fontWeight = FontWeight.Black),
                                color = if (isNight) Color.White else Color.Black
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Enter code sent to +91 $phoneNumber",
                                style = SkyroTypography.Body,
                                color = if (isNight) Color.White.copy(alpha = 0.6f) else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }

                        // OTP Digit Squares
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (0..3).forEach { index ->
                                    val char = otpCode.getOrNull(index)
                                    val isFocused = otpCode.length == index
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isNight) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f))
                                            .border(
                                                width = 2.dp,
                                                color = if (isFocused) orangeColor else Color.Transparent,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = char?.toString() ?: "",
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isNight) Color.White else Color.Black
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            TextButton(
                                onClick = {
                                    if (timerSeconds == 0) {
                                        timerSeconds = 30
                                    }
                                },
                                enabled = timerSeconds == 0
                            ) {
                                Text(
                                    text = if (timerSeconds > 0) "Resend OTP in ${timerSeconds}s" else "Resend OTP",
                                    color = if (timerSeconds > 0) Color.Gray else orangeColor,
                                    style = SkyroTypography.Caption.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        // Number Pad for OTP
                        VirtualKeyboard(
                            onKeyPress = { code ->
                                if (otpCode.length < 4) {
                                    otpCode += code
                                    if (otpCode.length == 4) {
                                        // Auto-verify
                                        isVerifyingOtp = true
                                        coroutineScope.launch {
                                            delay(1200)
                                            isVerifyingOtp = false
                                            authStage = "SUCCESS"
                                        }
                                    }
                                }
                            },
                            onBackspace = {
                                if (otpCode.isNotEmpty()) {
                                    otpCode = otpCode.dropLast(1)
                                }
                            },
                            isNight = isNight
                        )
                    }
                }

                "SUCCESS" -> {
                    var runFinishTransition by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        runFinishTransition = true
                        delay(2000)
                        viewModel.login(
                            userName = googleName ?: "Sai Sankar",
                            phoneNumber = phoneNumber,
                            userEmail = googleAccount ?: "saisankaryandamuri@gmail.com"
                        )
                        onLoginSuccess()
                    }

                    // Success bounce scale animation
                    val scaleSuccess by animateFloatAsState(
                        targetValue = if (runFinishTransition) 1f else 0.4f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
                        label = "SuccessBounce"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier
                                .size(100.dp)
                                .scale(scaleSuccess)
                                .testTag("login_success_icon")
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Success!",
                            style = SkyroTypography.H1.copy(fontWeight = FontWeight.Black),
                            color = if (isNight) Color.White else Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Connecting you to autonomous delivery airways...",
                            style = SkyroTypography.Body,
                            color = if (isNight) Color.White.copy(alpha = 0.6f) else Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (showGoogleChooser) {
            AlertDialog(
                onDismissRequest = { showGoogleChooser = false },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showGoogleChooser = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🌐", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
                        Text(
                            text = "Sign in with Google",
                            style = SkyroTypography.H2,
                            fontWeight = FontWeight.Black,
                            color = if (isNight) Color.White else Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Choose an account to continue to Skyro Delivery (Compulsory)",
                            fontSize = 11.sp,
                            color = if (isNight) Color.White.copy(alpha = 0.7f) else Color.DarkGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        // Account Option 1: Sai Sankar
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    googleAccount = "saisankaryandamuri@gmail.com"
                                    googleName = "Sai Sankar"
                                    showGoogleChooser = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNight) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("👤", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Sai Sankar",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isNight) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "saisankaryandamuri@gmail.com",
                                        fontSize = 11.sp,
                                        color = if (isNight) Color.White.copy(alpha = 0.6f) else Color.Gray
                                    )
                                }
                            }
                        }

                        // Account Option 2: SRM Student
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    googleAccount = "srm_student@srmap.edu.in"
                                    googleName = "SRM AP Student"
                                    showGoogleChooser = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNight) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎓", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "SRM AP Student",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isNight) Color.White else Color.Black
                                    )
                                    Text(
                                        text = "srm_student@srmap.edu.in",
                                        fontSize = 11.sp,
                                        color = if (isNight) Color.White.copy(alpha = 0.6f) else Color.Gray
                                    )
                                }
                            }
                        }
                    }
                },
                containerColor = if (isNight) Color(0xFF1E1E2E) else Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun VirtualKeyboard(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    isNight: Boolean
) {
    val keys = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "⌫")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNight) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.04f))
                            .clickable(
                                enabled = key.isNotEmpty(),
                                onClick = {
                                    if (key == "⌫") {
                                        onBackspace()
                                    } else {
                                        onKeyPress(key)
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (key != "") {
                            Text(
                                text = key,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNight) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}
