package com.callcenter.smartclass.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.Message
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MessageBox(message: Message) {

    val user = FirebaseAuth.getInstance().currentUser
    val profileImageUrl = user?.photoUrl?.toString()

    // Define the modifier for the message bubble
    val modifier = if (message.isMe) {
        Modifier
            .padding(start = 16.dp, end = 8.dp)
            .defaultMinSize(minHeight = 60.dp)
            .clip(RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp, bottomStart = 20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF007EF4),
                        Color(0xFF2A75BC),
                    )
                )
            )
    } else {
        Modifier
            .padding(start = 8.dp, end = 16.dp)
            .defaultMinSize(minHeight = 60.dp)
            .clip(RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp, bottomEnd = 20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF454545),
                        Color(0xFF2B2B2B),
                    )
                )
            )
    }

    // Use Box for flexible arrangement
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.CenterStart // Align content to the start
    ) {
        // Profile picture on the left if not 'me'
        if (!message.isMe) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(8.dp)
                    .align(Alignment.CenterStart) // Align profile picture to the start (left)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        }

        // Message bubble container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (message.isMe) 0.dp else 48.dp, end = if (message.isMe) 48.dp else 0.dp), // Leave space for the profile picture
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
        ) {
            // Message bubble
            Box(modifier = modifier) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = if (message.isMe) Alignment.End else Alignment.Start
                ) {
                    Text(
                        text = message.message,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.sen)),
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message.time,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 12.sp,
                            fontFamily = FontFamily(Font(R.font.sen)),
                        )
                    )
                }
            }
        }

        // Profile picture on the right if 'me'
        if (message.isMe) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(8.dp)
                    .align(Alignment.CenterEnd)
            ) {
                if (profileImageUrl != null) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = null,
                        modifier = Modifier.clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.Black
                    )
                }
            }
        }
    }
}
