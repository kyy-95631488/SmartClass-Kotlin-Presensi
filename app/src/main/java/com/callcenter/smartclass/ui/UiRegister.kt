package com.callcenter.smartclass.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.LocaleManager
import com.callcenter.smartclass.ui.funcauth.FunLoginGoogle
import com.callcenter.smartclass.ui.theme.FunctionalRed
import com.callcenter.smartclass.ui.theme.Lavender0
import com.callcenter.smartclass.ui.theme.Neutral0
import com.callcenter.smartclass.ui.theme.Neutral8
import com.callcenter.smartclass.ui.theme.Ocean6
import com.callcenter.smartclass.ui.theme.Ocean7
import com.callcenter.smartclass.ui.theme.Ocean8
import com.callcenter.smartclass.ui.theme.costum01
import com.callcenter.smartclass.ui.uionly.UiLoginViaEmail
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("UnusedBoxWithConstraintsScope")
@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun UiRegister() {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // State variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Dialog states
    var dialogVisible by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }

    var passwordErrorResId by remember { mutableStateOf<Int?>(null) }
    var confirmPasswordErrorResId by remember { mutableStateOf<Int?>(null) }

    // Back Press Handler
    DisposableEffect(context) {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (context is ComponentActivity) {
                    val intent = Intent(context, FunLoginGoogle::class.java)
                    context.startActivity(intent)
                    context.finish()
                }
            }
        }
        (context as? ComponentActivity)?.onBackPressedDispatcher?.addCallback(callback)
        onDispose {
            callback.remove()
        }
    }

    // Animation for title
    var titleVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        titleVisible = true
    }

    // Set Locale
    LaunchedEffect(Unit) {
        LocaleManager.setLocale(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Lavender0, Ocean6)
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (context is ComponentActivity) {
                            val intent = Intent(context, FunLoginGoogle::class.java)
                            context.startActivity(intent)
                            context.finish()
                        }
                    }
                ) {
                    @Suppress("DEPRECATION")
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_to_login),
                        tint = Ocean7,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Logo/Image with Enhanced Animation
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                exit = fadeOut()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.assets_bg_register),
                    contentDescription = stringResource(id = R.string.register_image),
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .aspectRatio(1f)
                        .padding(bottom = 8.dp)
                )
            }

            // Title with Enhanced Typography
            AnimatedVisibility(
                visible = titleVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
                exit = fadeOut()
            ) {
                Text(
                    text = stringResource(id = R.string.register_account),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.SansSerif
                    ),
                    color = costum01,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Card for Input Fields with Elevated Design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Neutral0)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Email TextField with Icon
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(id = R.string.email), color = Ocean7) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = stringResource(id = R.string.email_icon_description),
                                tint = Ocean8
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Neutral8,
                            unfocusedTextColor = Neutral8,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Ocean8,
                            unfocusedIndicatorColor = Ocean6,
                            cursorColor = Ocean8,
                            errorIndicatorColor = FunctionalRed,
                            selectionColors = TextSelectionColors(
                                handleColor = Ocean8,
                                backgroundColor = Ocean8.copy(alpha = 0.4f)
                            )
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password TextField with Icon and Enhanced Visibility Toggle
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordErrorResId = if (it.length < 8) R.string.password_min_length else null
                        },
                        label = { Text(stringResource(id = R.string.password), color = Ocean7) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(id = R.string.password_icon_description),
                                tint = Ocean8
                            )
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                val icon =
                                    if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                Icon(imageVector = icon, contentDescription = null, tint = Ocean8)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Neutral8,
                            unfocusedTextColor = Neutral8,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Ocean8,
                            unfocusedIndicatorColor = Ocean6,
                            cursorColor = Ocean8,
                            errorIndicatorColor = FunctionalRed,
                            selectionColors = TextSelectionColors(
                                handleColor = Ocean8,
                                backgroundColor = Ocean8.copy(alpha = 0.4f)
                            )
                        )
                    )

                    // Password Error Message with AnimatedVisibility
                    AnimatedVisibility(
                        visible = passwordError.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        Text(
                            text = passwordError,
                            color = FunctionalRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirm Password TextField with Icon and Enhanced Visibility Toggle
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            confirmPasswordErrorResId = if (it != password) R.string.password_mismatch else null
                        },
                        label = { Text(stringResource(id = R.string.confirm_password), color = Ocean7) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(id = R.string.confirm_password_icon),
                                tint = Ocean8
                            )
                        },
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                val icon =
                                    if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                                Icon(imageVector = icon, contentDescription = null, tint = Ocean8)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Neutral8,
                            unfocusedTextColor = Neutral8,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Ocean8,
                            unfocusedIndicatorColor = Ocean6,
                            cursorColor = Ocean8,
                            errorIndicatorColor = FunctionalRed,
                            selectionColors = TextSelectionColors(
                                handleColor = Ocean8,
                                backgroundColor = Ocean8.copy(alpha = 0.4f)
                            )
                        )
                    )

                    // Confirm Password Error Message with AnimatedVisibility
                    AnimatedVisibility(
                        visible = confirmPasswordError.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 300))
                    ) {
                        Text(
                            text = confirmPasswordError,
                            color = FunctionalRed,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Register Button with Icon
                    Button(
                        onClick = {
                            if (email.isNotBlank() && password.length >= 8 && password == confirmPassword) {
                                loading = true
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        loading = false
                                        if (task.isSuccessful) {
                                            auth.currentUser?.sendEmailVerification()
                                                ?.addOnCompleteListener { verificationTask ->
                                                    if (verificationTask.isSuccessful) {
                                                        auth.signOut()
                                                        email = ""
                                                        password = ""
                                                        confirmPassword = ""
                                                        passwordError = ""
                                                        confirmPasswordError = ""
                                                        dialogMessage = context.getString(R.string.registration_success)
                                                    } else {
                                                        dialogMessage = context.getString(
                                                            R.string.verification_email_failed,
                                                            verificationTask.exception?.message ?: ""
                                                        )
                                                    }
                                                    dialogVisible = true
                                                }
                                        } else {
                                            dialogMessage = context.getString(
                                                R.string.registration_failed,
                                                task.exception?.message ?: ""
                                            )
                                            dialogVisible = true
                                        }
                                    }
                            } else {
                                dialogMessage = context.getString(R.string.fill_fields_correctly)
                                dialogVisible = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Ocean7,
                            contentColor = Neutral0
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(end = 8.dp),
                                color = Neutral0
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = stringResource(id = R.string.register_icon),
                                tint = Neutral0,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = stringResource(id = R.string.register_button), fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val annotatedText = buildAnnotatedString {
                            append(stringResource(id = R.string.register_terms_intro))
                            pushStringAnnotation(
                                tag = "terms",
                                annotation = "terms"
                            )
                            withStyle(
                                style = SpanStyle(
                                    color = Ocean8,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append(stringResource(id = R.string.terms_conditions))
                            }
                            pop()
                            append(stringResource(id = R.string.register_terms_outro))
                        }

                        Text(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = if (screenWidth < 600.dp) 12.sp else 16.sp,
                                color = Neutral8
                            ),
                            onTextLayout = { textLayoutResult = it },
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures { offsetPosition ->
                                    textLayoutResult?.let { layoutResult ->
                                        val position = layoutResult.getOffsetForPosition(offsetPosition)
                                        annotatedText.getStringAnnotations(
                                            tag = "terms",
                                            start = position,
                                            end = position
                                        ).firstOrNull()?.let {
                                            if (context is ComponentActivity) {
                                                val intent = Intent(context, TermsAndConditionsActivity::class.java)
                                                context.startActivity(intent)
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.already_have_account),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.SansSerif
                            ),
                            color = Neutral8
                        )
                        Text(
                            text = stringResource(id = R.string.login),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.SansSerif,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Ocean7,
                            modifier = Modifier.clickable {
                                if (context is ComponentActivity) {
                                    context.setContent { UiLoginViaEmail() }
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // General Dialog for Messages
        if (dialogVisible) {
            AlertDialog(
                onDismissRequest = { dialogVisible = false },
                title = null,
                text = {
                    Text(
                        text = dialogMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Ocean7,
                        modifier = Modifier.padding(16.dp)
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { dialogVisible = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Ocean7,
                            contentColor = Neutral0
                        )
                    ) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = Lavender0,
                tonalElevation = 16.dp,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
