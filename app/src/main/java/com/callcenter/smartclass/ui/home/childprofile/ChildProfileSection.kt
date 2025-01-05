package com.callcenter.smartclass.ui.home.childprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.ChildProfile
import com.callcenter.smartclass.ui.navigation.MainDestinations
import com.callcenter.smartclass.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ChildProfileSection(navController: NavController) {
    val backgroundColor = if (isSystemInDarkTheme()) MinimalBackgroundDark else MinimalBackgroundLight
    val textColor = if (isSystemInDarkTheme()) MinimalTextDark else MinimalTextLight

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    var childrenProfiles by remember { mutableStateOf<List<ChildProfile>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("children")
                .get()
                .addOnSuccessListener { result ->
                    childrenProfiles = result.mapNotNull { document ->
                        val child = document.toObject(ChildProfile::class.java)
                        child.copy(id = document.id)
                    }
                }
        }
    }

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

        if (childrenProfiles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(id = R.string.no_child_profile),
                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { navController.navigate(MainDestinations.ADD_CHILD_PROFILE_ROUTE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemInDarkTheme()) ButtonDarkColor else ButtonLightColor,
                        contentColor = if (isSystemInDarkTheme()) TextDarkColor else TextDarkColor
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.add),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.add_child))
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                childrenProfiles.forEach { child ->
                    ProfileCard(child = child, backgroundColor = backgroundColor, textColor = textColor, navController = navController)
                }
                AddProfileCard(backgroundColor, textColor, navController)
            }
        }
    }
}

@Composable
fun AddProfileCard(backgroundColor: Color, textColor: Color, navController: NavController) {
    Card(
        onClick = { navController.navigate(MainDestinations.ADD_CHILD_PROFILE_ROUTE) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .width(150.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add_child_profile),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally),
                    tint = textColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(id = R.string.add_child_profile),
                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCard(child: ChildProfile, backgroundColor: Color, textColor: Color, navController: NavController) {
    val normalizedGender = child.gender.trim().lowercase(Locale.getDefault())

    val iconRes = when (normalizedGender) {
        "male", "laki-laki", "pria" -> R.drawable.ic_kids_baby_male
        "female", "perempuan", "wanita" -> R.drawable.ic_kids_baby_female
        else -> R.drawable.assets_logo_smartclass
    }

    Card(
        onClick = { navController.navigate("child_detail/${child.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .width(150.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(id = R.string.add_child_profile),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterHorizontally),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    child.name,
                    style = MaterialTheme.typography.titleMedium.copy(color = textColor),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${child.height} ${stringResource(id = R.string.height_cm)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    calculateAge(child.birthDate, stringResource(id = R.string.age_year_month)),
                    style = MaterialTheme.typography.bodySmall.copy(color = textColor),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

fun calculateAge(birthDate: String, ageFormat: String): String {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val birthDateParsed: Date? = dateFormatter.parse(birthDate)

    if (birthDateParsed == null) {
        return "0 tahun 0 bulan"
    }

    val currentDate = Calendar.getInstance()
    val birthCalendar = Calendar.getInstance().apply { time = birthDateParsed }

    var ageYears = currentDate.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
    var ageMonths = currentDate.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH)

    if (ageMonths < 0) {
        ageYears--
        ageMonths += 12
    }

    return String.format(Locale.getDefault(), ageFormat, ageYears, ageMonths)
}
