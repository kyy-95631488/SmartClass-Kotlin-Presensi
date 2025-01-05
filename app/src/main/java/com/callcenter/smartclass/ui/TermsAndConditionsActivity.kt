package com.callcenter.smartclass.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callcenter.smartclass.R
import com.callcenter.smartclass.data.LocaleManager

class TermsAndConditionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LocaleManager.setLocale(this) // Atur locale sebelum setContent
        super.onCreate(savedInstanceState)
        setContent {
            TermsAndConditionsScreen(
                onBackPressed = { finish() },
                onChangeLanguage = { langCode ->
                    LocaleManager.persistLanguage(this, langCode)
                    LocaleManager.setLocale(this)
                    recreate() // refresh Activity dengan locale baru
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
    onBackPressed: () -> Unit,
    onChangeLanguage: (String) -> Unit
) {
    var agreed by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }

    if (showToast) {
        ShowToast()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        ),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (agreed) {
                                onBackPressed()
                            } else {
                                showToast = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    LanguageDropdown(onChangeLanguage = onChangeLanguage)
                }
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .animateContentSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.terms_and_conditions_title),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Text(
                        text = stringResource(R.string.terms_intro),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.padding(bottom = 24.dp),
                        textAlign = TextAlign.Justify
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.primary,
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Text(
                        text = stringResource(R.string.agree_header),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 18.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TermsAndConditionsText()

                    Spacer(modifier = Modifier.height(32.dp))

                    AcceptButton(onAgree = { agreed = true })
                    if (agreed) {
                        Text(
                            text = stringResource(R.string.thank_you_text),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(top = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ShowToast() {
    val context = LocalContext.current
    Toast.makeText(
        context,
        stringResource(id = R.string.agree_instruction),
        Toast.LENGTH_SHORT
    ).show()
}

@Composable
fun TermsAndConditionsText() {
    val terms = stringArrayResource(id = R.array.terms_points)
    terms.forEachIndexed { index, term ->
        Text(
            text = "${index + 1}. $term",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Justify
        )
    }
}

@Composable
fun AcceptButton(onAgree: () -> Unit) {
    var agreed by remember { mutableStateOf(false) }

    Button(
        onClick = {
            agreed = true
            onAgree()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = if (agreed) stringResource(id = R.string.thank_you_text) else stringResource(id = R.string.agree_button_text),
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun LanguageDropdown(onChangeLanguage: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box {
        TextButton(
            onClick = { expanded = true },
        ) {
            Text(
                text = stringResource(id = R.string.change_language),
                color = Color.White,
                fontSize = 16.sp
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.language_english)) },
                onClick = {
                    onChangeLanguage("en")
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.language_indonesian)) },
                onClick = {
                    onChangeLanguage("id")
                    expanded = false
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TermsAndConditionsPreview() {
    TermsAndConditionsScreen(onBackPressed = {}, onChangeLanguage = {})
}
