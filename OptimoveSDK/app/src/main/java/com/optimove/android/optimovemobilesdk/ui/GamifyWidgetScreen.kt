package com.optimove.android.optimovemobilesdk.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val CardShape = RoundedCornerShape(12.dp)
private val SectionPadding = 16.dp

@Composable
fun GamifyWidgetScreen(
    userId: String,
    brand: String,
    loyaltyWidgetSrc: String,
    adactUrl: String,
    campaignId: String,
    cid: String,
    adactToken: String,
    onUserIdChange: (String) -> Unit,
    onBrandChange: (String) -> Unit,
    onCampaignIdChange: (String) -> Unit,
    onCidChange: (String) -> Unit,
    onAdactTokenChange: (String) -> Unit,
    onOpenWidget: () -> Unit,
    onOpenAdactCampaign: () -> Unit,
    onCloseAdactCampaign: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(SectionPadding)
    ) {
        Text(
            "Gamify Widget",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(SectionPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = userId,
                    onValueChange = onUserIdChange,
                    label = { Text("User ID (optional — defaults to SDK user)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = brand,
                    onValueChange = onBrandChange,
                    label = { Text("Brand (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors()
                )
                ConfigValue(
                    "Widget URL to open",
                    loyaltyWidgetSrc.ifBlank { "(not available — needs loyaltyWidgetUrl, tenant id and a user)" }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onOpenWidget,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Open Widget")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Adact Campaign",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(SectionPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConfigValue("Adact URL (from tenant config)", adactUrl.ifBlank { "(not configured)" })
                OutlinedTextField(
                    value = campaignId,
                    onValueChange = onCampaignIdChange,
                    label = { Text("Campaign ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = cid,
                    onValueChange = onCidChange,
                    label = { Text("CID (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors()
                )
                OutlinedTextField(
                    value = adactToken,
                    onValueChange = onAdactTokenChange,
                    label = { Text("Token (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = fieldColors()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onOpenAdactCampaign,
            enabled = adactUrl.isNotBlank() && campaignId.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Open Adact Campaign")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onCloseAdactCampaign,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Close Adact Campaign")
        }
    }
}

@Composable
private fun ConfigValue(label: String, value: String) {
    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary
)
