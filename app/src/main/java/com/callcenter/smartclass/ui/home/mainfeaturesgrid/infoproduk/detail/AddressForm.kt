package com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.detail

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.ContentAlpha
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.callcenter.smartclass.ui.home.mainfeaturesgrid.infoproduk.data.Address
import com.callcenter.smartclass.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AddressForm(
    existingAddress: Address? = null,
    onAddressSubmit: (Address) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var street by remember { mutableStateOf(existingAddress?.street ?: "") }
    var city by remember { mutableStateOf(existingAddress?.city ?: "") }
    var province by remember { mutableStateOf(existingAddress?.province ?: "") }
    var postalCode by remember { mutableStateOf(existingAddress?.postalCode ?: "") }
    var phoneNumber by remember { mutableStateOf(existingAddress?.phoneNumber ?: "") }

    val isDarkMode = isSystemInDarkTheme()

    val containerColorValue = if (isDarkMode) MinimalBackgroundDark else MinimalBackgroundLight

    val customTextSelectionColors = TextSelectionColors(
        handleColor = MinimalPrimary,
        backgroundColor = if (isDarkMode) WhiteColor else DarkText.copy(alpha = 0.4f)
    )

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = containerColorValue,
        unfocusedContainerColor = containerColorValue,
        disabledContainerColor = containerColorValue.copy(alpha = ContentAlpha.disabled),
        errorContainerColor = containerColorValue,
        focusedBorderColor = MinimalPrimary,
        unfocusedBorderColor = MinimalSecondary,
        disabledBorderColor = MinimalSecondary.copy(alpha = ContentAlpha.disabled),
        errorBorderColor = MaterialTheme.colorScheme.error,
        cursorColor = MinimalPrimary,
        errorCursorColor = MaterialTheme.colorScheme.error,
        focusedTextColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
        unfocusedTextColor = if (isDarkMode) MinimalTextDark else MinimalTextLight,
        disabledTextColor = MinimalTextLight.copy(alpha = ContentAlpha.disabled),
        errorTextColor = MaterialTheme.colorScheme.error,
        focusedLabelColor = MinimalPrimary,
        unfocusedLabelColor = MinimalSecondary,
        disabledLabelColor = MinimalSecondary.copy(alpha = ContentAlpha.disabled),
        errorLabelColor = MaterialTheme.colorScheme.error,
        focusedPlaceholderColor = MinimalTextLight.copy(alpha = ContentAlpha.medium),
        unfocusedPlaceholderColor = MinimalTextLight.copy(alpha = ContentAlpha.medium),
        disabledPlaceholderColor = MinimalTextLight.copy(alpha = ContentAlpha.disabled),
        errorPlaceholderColor = MaterialTheme.colorScheme.error.copy(alpha = ContentAlpha.medium),
        focusedSupportingTextColor = MinimalTextLight.copy(alpha = ContentAlpha.medium),
        unfocusedSupportingTextColor = MinimalTextLight.copy(alpha = ContentAlpha.medium),
        disabledSupportingTextColor = MinimalTextLight.copy(alpha = ContentAlpha.disabled),
        errorSupportingTextColor = MaterialTheme.colorScheme.error.copy(alpha = ContentAlpha.medium)
    )

    val coroutineScope = rememberCoroutineScope()

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Alamat Pengiriman",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = street,
                onValueChange = { street = it },
                label = { Text("Jalan dan Nomor") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Kota") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = province,
                onValueChange = { province = it },
                label = { Text("Provinsi") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = postalCode,
                onValueChange = { postalCode = it },
                label = { Text("Kode Pos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Nomor HP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (street.isNotBlank() && city.isNotBlank() && province.isNotBlank() && postalCode.isNotBlank() && phoneNumber.isNotBlank()) {
                        val newAddress = Address(
                            street = street,
                            city = city,
                            province = province,
                            postalCode = postalCode,
                            phoneNumber = phoneNumber
                        )
                        onAddressSubmit(newAddress)
                    } else {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Silakan isi semua bidang",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Ocean4 else Ocean7,
                    contentColor = Color.White
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = "Simpan Alamat")
            }
        }
    }
}
