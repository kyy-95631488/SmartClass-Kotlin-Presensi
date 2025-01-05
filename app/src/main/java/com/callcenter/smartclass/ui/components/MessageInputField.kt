package com.callcenter.smartclass.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callcenter.smartclass.R
import com.callcenter.smartclass.ui.theme.DarkText
import com.callcenter.smartclass.ui.theme.WhiteColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputField(onSendMessage: (String) -> Unit, onSelectImages: () -> Unit, onTakePicture: () -> Unit) {
    val message = remember { mutableStateOf("") }

    TextField(
        value = message.value,
        onValueChange = {
            message.value = it
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .navigationBarsPadding()
            .imePadding()
            .height(56.dp),
        textStyle = TextStyle(
            color = Color(0xFFCCCCCC),
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(R.font.sen))
        ),
        placeholder = {
            Text(
                text = "Type a message...",
                style = TextStyle(
                    color = Color(0xFFCCCCCC),
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.sen))
                )
            )
        },
        trailingIcon = {
            Row {
                IconButton(
                    onClick = onSelectImages,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFCCCCCC)
                    )
                ) {
                    Icon(imageVector = Icons.Outlined.AttachFile, contentDescription = null)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Add button to take a picture
                IconButton(
                    onClick = {
                        onTakePicture() // Trigger the take picture action
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFCCCCCC)
                    )
                ) {
                    Icon(imageVector = Icons.Outlined.CameraAlt, contentDescription = null) // Use appropriate icon
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (message.value.isNotEmpty()) {
                            onSendMessage(message.value)
                            message.value = ""
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color(0xFFCCCCCC)
                    )
                ) {
                    @Suppress("DEPRECATION")
                    Icon(imageVector = Icons.Outlined.Send, contentDescription = null)
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
            unfocusedTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
            disabledTextColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = ContentAlpha.disabled),
            errorTextColor = MaterialTheme.colorScheme.error,
            cursorColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
            focusedContainerColor = Color(0xFF2B2B2B),
            unfocusedContainerColor = Color(0xFF2B2B2B),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            selectionColors = TextSelectionColors(
                handleColor = if (isSystemInDarkTheme()) WhiteColor else DarkText,
                backgroundColor = if (isSystemInDarkTheme()) WhiteColor else DarkText.copy(alpha = 0.4f)
            )
        )
    )
}

