package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioRoutingMode
import com.example.audio.BluetoothStatus
import com.example.translator.PhrasebookData
import com.example.ui.theme.EmeraldActive
import com.example.ui.theme.EmeraldPill
import com.example.ui.theme.EmeraldPillBg
import com.example.ui.theme.FrostedBorder
import com.example.ui.theme.FrostedBorderSlate
import com.example.ui.theme.FrostedGlassWhite
import com.example.ui.theme.IndigoGlow
import com.example.ui.theme.SkyBlueDark
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueUltraLight
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMedium
import com.example.ui.theme.SlateMuted

@Composable
fun EarbudCompanionScreen(
    bluetoothStatus: BluetoothStatus,
    onRoutingModeChange: (AudioRoutingMode) -> Unit,
    onRefreshBluetooth: () -> Unit,
    onTestEarbudAudio: (isBanglaToEarbud: Boolean) -> Unit
) {
    var testFeedback by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("earbud_companion_screen"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Status Card (Frosted Glass)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                color = FrostedGlassWhite,
                border = BorderStroke(1.dp, FrostedBorder),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (bluetoothStatus.isHeadsetConnected) SkyBluePrimary else FrostedBorderSlate),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (bluetoothStatus.isHeadsetConnected) Icons.Default.Headphones else Icons.Default.BluetoothSearching,
                                    contentDescription = null,
                                    tint = if (bluetoothStatus.isHeadsetConnected) Color.White else SlateMedium,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = if (bluetoothStatus.isHeadsetConnected) bluetoothStatus.deviceName else "No Earbuds Connected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateDark
                                )
                                Text(
                                    text = if (bluetoothStatus.isHeadsetConnected) "Ready for Smart AI Translation" else "Connect your earbuds via phone Bluetooth",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (bluetoothStatus.isHeadsetConnected) SkyBlueDark else SlateMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = onRefreshBluetooth,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SkyBlueUltraLight)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = SkyBluePrimary, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = SkyBlueUltraLight.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, Color(0xFFBAE6FD).copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("MIC INPUT", fontSize = 10.sp, color = SlateLight, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (bluetoothStatus.isHeadsetConnected) "Earbud BT SCO Mic" else "Phone Mic",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBlueDark
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = EmeraldPillBg.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, EmeraldActive.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("AUDIO OUTPUT", fontSize = 10.sp, color = SlateLight, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = bluetoothStatus.routingMode.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPill
                                )
                            }
                        }
                    }
                }
            }
        }

        // Interactive Audio Testing Section (Frosted Glass)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = FrostedGlassWhite,
                border = BorderStroke(1.dp, FrostedBorder),
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SkyBlueUltraLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Earbud Audio Routing Test", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateDark)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Verify that the audio routing sends Bangla into your earbud and English to your phone speaker.",
                        fontSize = 12.sp,
                        color = SlateMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                onTestEarbudAudio(true)
                                testFeedback = "Playing Bangla voice to Earbud..."
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SkyBluePrimary)
                        ) {
                            Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Earbud (BN)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                onTestEarbudAudio(false)
                                testFeedback = "Playing English voice to Phone Speaker..."
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoGlow)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Speaker (EN)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    testFeedback?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldPillBg,
                            border = BorderStroke(1.dp, EmeraldActive.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = msg,
                                fontSize = 12.sp,
                                color = EmeraldPill,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Routing Modes Selection
        item {
            Text(
                text = "Select Audio Routing Mode",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SkyBlueDark,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        items(AudioRoutingMode.values()) { mode ->
            val isSelected = bluetoothStatus.routingMode == mode
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onRoutingModeChange(mode) },
                shape = RoundedCornerShape(18.dp),
                color = if (isSelected) SkyBlueUltraLight.copy(alpha = 0.7f) else FrostedGlassWhite,
                border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary.copy(alpha = 0.6f) else FrostedBorder),
                shadowElevation = if (isSelected) 4.dp else 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SkyBluePrimary else FrostedBorderSlate),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else SlateMedium,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mode.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SlateDark
                        )
                        Text(
                            text = mode.subtitle,
                            fontSize = 12.sp,
                            color = SlateMuted
                        )
                    }
                }
            }
        }

        // Smart Tips & HOCO Setup Guides
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Smart Earbuds Tips & Setup Guide",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SkyBlueDark,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        items(PhrasebookData.earbudTips) { tip ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = FrostedGlassWhite,
                border = BorderStroke(1.dp, FrostedBorder),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SkyBluePrimary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tip.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = SlateDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tip.description,
                            fontSize = 12.sp,
                            color = SlateMuted,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

