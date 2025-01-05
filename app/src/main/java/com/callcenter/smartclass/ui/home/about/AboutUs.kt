package com.callcenter.smartclass.ui.home.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.TeamMember
import com.callcenter.smartclass.ui.components.smartclassDivider
import com.callcenter.smartclass.ui.funcauth.viewmodel.UserViewModel
import com.callcenter.smartclass.ui.theme.ButtonDarkColor
import com.callcenter.smartclass.ui.theme.ButtonLightColor
import com.callcenter.smartclass.ui.theme.smartclassTheme
import com.callcenter.smartclass.ui.theme.TextDarkColor
import com.callcenter.smartclass.ui.theme.TextLightColor

@Composable
fun AboutUs(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = viewModel()
) {
    val isAdmin by userViewModel.isAdmin.collectAsState()

    smartclassTheme {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.about_smartclass),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = stringResource(id = R.string.about_description),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            item {
                Text(
                    text = stringResource(id = R.string.team_smartclass),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            items(getTeamMembers()) { member ->
                TeamMemberCard(member)
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                smartclassDivider(
                    color = Color.Gray,
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                Text(
                    text = stringResource(id = R.string.contact_us),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.contact_description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (isAdmin) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    AdminButton(navController = navController)
                }
            }
        }
    }
}

fun getTeamMembers(): List<TeamMember> {
    return listOf(
        TeamMember(
            "Riska Dewi Yuliyanti",
            "Machine Learning",
            "https://github.com/RiskaDewiYuliyanti",
            "https://www.linkedin.com/in/riskady/",
            R.drawable.assets_logo_smartclass
        ),
        TeamMember(
            "Ashtri Cahyani",
            "Machine Learning",
            "https://github.com/ashcry",
            "https://www.linkedin.com/in/astri",
            R.drawable.assets_logo_smartclass
        ),
        TeamMember(
            "Kartika Rahma Sulistyawati",
            "Machine Learning",
            "https://github.com/tyakartikaa",
            "https://www.linkedin.com/in/kartika-rahma-sulistyawati",
            R.drawable.assets_logo_smartclass
        ),
        TeamMember(
            "Shandy Satria Nugraha",
            "Cloud Computing",
            "https://github.com/ShandySatrian",
            "https://www.linkedin.com/in/shandy-satrian/",
            R.drawable.assets_logo_smartclass
        ),
        TeamMember(
            "Fitri Sri Mulyani",
            "Cloud Computing",
            "https://github.com/fitri786",
            "https://www.linkedin.com/in/fitri-sri-mulyani-923968330",
            R.drawable.assets_logo_smartclass
        ),
        TeamMember(
            "Hendriansyah Rizky Setiawan",
            "Mobile Development",
            "https://github.com/kyy-95631488",
            "https://www.linkedin.com/in/hendriansyah-rizky-setiawan-8b4a68308/",
            R.drawable.assets_logo_smartclass
        ),
        TeamMember(
            "Kenny Josiah Silaen",
            "Mobile Development",
            "https://github.com/kensmoba",
            "https://www.linkedin.com/in/kenny-josiah-silaen-2b7791304/",
            R.drawable.assets_logo_smartclass
        )
        // Tambahkan anggota tim lainnya
    )
}

@Composable
fun TeamMemberCard(member: TeamMember) {
    val uriHandler = LocalUriHandler.current
    val isDarkTheme = isSystemInDarkTheme()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = member.profileImage),
            contentDescription = "Profile Picture of ${member.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = member.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = member.role,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val buttonColor = if (isDarkTheme) ButtonDarkColor else ButtonLightColor
                val textColor = if (isDarkTheme) TextDarkColor else TextDarkColor

                TextButton(
                    onClick = { uriHandler.openUri(member.githubLink) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = textColor
                    ),
                    modifier = Modifier
                        .padding(horizontal = 0.dp, vertical = 2.dp)
                        .defaultMinSize(minWidth = 60.dp, minHeight = 28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_github),
                        contentDescription = "GitHub Icon",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "GitHub",
                        fontSize = 12.sp
                    )
                }

                TextButton(
                    onClick = { uriHandler.openUri(member.linkedinLink) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = textColor
                    ),
                    modifier = Modifier
                        .padding(horizontal = 0.dp, vertical = 2.dp)
                        .defaultMinSize(minWidth = 60.dp, minHeight = 28.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_linkedin),
                        contentDescription = "LinkedIn Icon",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "LinkedIn",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
