package com.callcenter.smartclass.ui.home.admin

//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.callcenter.smartclass.data.UserFirebase
import com.callcenter.smartclass.ui.components.smartclassCard
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EditUser(navController: NavController, userUuid: String) {
    var user by remember { mutableStateOf<UserFirebase?>(null) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var profilePicUrl by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userUuid) {
        if (userUuid.isNotEmpty()) {
            FirebaseFirestore.getInstance().collection("users").document(userUuid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        user = document.toObject(UserFirebase::class.java)
                        user?.let {
                            username = it.username
                            email = it.email
                            role = it.role
                            profilePicUrl = it.profilePic ?: ""
                        }
                    } else {
                        errorMessage = "User not found"
                    }
                }
                .addOnFailureListener { exception ->
                    errorMessage = "Error fetching user: ${exception.localizedMessage}"
                }
        } else {
            errorMessage = "Invalid user UUID"
        }
    }

    val isDarkMode = isSystemInDarkTheme()
    val backgroundColor = if (isDarkMode) DarkBackgroundColor else LightBackgroundColor
    val errorTextColor = FunctionalRed
    val inputFieldTextColor = if (isDarkMode) DarkTextColor else LightTextColor
    val buttonColor = if (isDarkMode) ButtonDarkColor else ButtonLightColor
    val buttonTextColor = if (isDarkMode) WhiteColor else DarkText

    val enterTransition = remember { fadeIn(animationSpec = tween(1000)) }
    val exitTransition = remember { fadeOut(animationSpec = tween(500)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(backgroundColor)
    ) {
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = enterTransition,
            exit = exitTransition
        ) {
            Text(
                text = errorMessage ?: "",
                color = errorTextColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )
        }

        AnimatedVisibility(
            visible = true,
            enter = enterTransition,
            exit = exitTransition
        ) {
            Text("Edit User", style = MaterialTheme.typography.headlineSmall, color = if (isDarkMode) DarkTextColor else LightTextColor)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
                .border(4.dp, Color.Gray, CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
                .padding(2.dp)
        ) {
            Column {
                AnimatedVisibility(
                    visible = profilePicUrl.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(profilePicUrl),
                        contentDescription = "User Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        smartclassCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { 40 }) + fadeOut()
                ) {
                    InputField(
                        label = "Username",
                        value = username,
                        onValueChange = { username = it },
                        textColor = inputFieldTextColor,
                        readOnly = true
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { 40 }) + fadeOut()
                ) {
                    InputField(
                        label = "Email",
                        value = email,
                        onValueChange = { email = it },
                        textColor = inputFieldTextColor,
                        readOnly = true
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { 40 }) + fadeOut()
                ) {
                    RoleDropdown(
                        selectedRole = role,
                        onRoleSelected = { role = it },
                        textColor = inputFieldTextColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button with Animated Transition
                AnimatedVisibility(
                    visible = true,
                    enter = scaleIn(initialScale = 0.8f) + fadeIn(),
                    exit = scaleOut(targetScale = 0.8f) + fadeOut()
                ) {
                    Button(
                        onClick = {
                            user?.let {
                                val updatedUser = it.copy(username = username, email = email, role = role)
                                FirebaseFirestore.getInstance().collection("users").document(userUuid)
                                    .set(updatedUser)
                                    .addOnSuccessListener {
                                        navController.popBackStack()
                                    }
                                    .addOnFailureListener { exception ->
                                        errorMessage = "Error updating user: ${exception.localizedMessage}"
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = buttonColor,
                            contentColor = buttonTextColor
                        )
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    textColor: Color,
    readOnly: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight)
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = value,
            onValueChange = { if (!readOnly) onValueChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { /* Handle keyboard done action */ }
            ),
            enabled = !readOnly,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                focusedBorderColor = MinimalPrimary,
                unfocusedBorderColor = MinimalSecondary,
                cursorColor = MinimalPrimary,
                textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight
            )
        )
    }
}

@Composable
fun RoleDropdown(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    textColor: Color
) {
    val roles = listOf("admin", "user")
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedRole,
            onValueChange = {},
            label = { Text("Role", color = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown Icon")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight,
                focusedBorderColor = MinimalPrimary,
                unfocusedBorderColor = MinimalSecondary,
                cursorColor = MinimalPrimary,
                textColor = textColor
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            roles.forEach { role ->
                DropdownMenuItem(onClick = {
                    onRoleSelected(role)
                    expanded = false
                }) {
                    Text(text = role)
                }
            }
        }
    }
}
