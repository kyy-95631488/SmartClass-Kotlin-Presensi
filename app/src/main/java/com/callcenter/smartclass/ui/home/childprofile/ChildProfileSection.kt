package com.callcenter.smartclass.ui.home.childprofile

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.callcenter.smartclass.R
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ChildProfileSection(navController: NavController) {
    val backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight
    val textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.profile_child),
            style = MaterialTheme.typography.titleLarge.copy(color = textColor),
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
