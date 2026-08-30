package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TranslationEntity
import com.example.translator.PhraseItem
import com.example.translator.PhrasebookData
import com.example.ui.theme.AmberWave
import com.example.ui.theme.EmeraldActive
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
fun PhrasebookScreen(
    starredTranslations: List<TranslationEntity>,
    onSpeakPhrase: (phrase: PhraseItem, speakBangla: Boolean) -> Unit,
    onReplayAudio: (text: String, lang: String, role: String) -> Unit,
    onToggleStar: (TranslationEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Phrasebook, 1: Starred
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPhrases = PhrasebookData.phrases.filter { phrase ->
        val matchesCategory = (selectedCategory == "All") || (phrase.category == selectedCategory)
        val matchesSearch = searchQuery.isBlank() ||
                phrase.banglaText.contains(searchQuery, ignoreCase = true) ||
                phrase.englishText.contains(searchQuery, ignoreCase = true) ||
                phrase.banglaPronunciation.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("phrasebook_screen")
    ) {
        // Frosted Tab Segment Switcher
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            color = FrostedGlassWhite,
            border = BorderStroke(1.dp, FrostedBorder),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tab 0
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedTab = 0 },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedTab == 0) SkyBluePrimary else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (selectedTab == 0) Color.White else SlateMedium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Phrasebook",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 0) Color.White else SlateMedium
                        )
                    }
                }

                // Tab 1
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedTab = 1 },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedTab == 1) SkyBluePrimary else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (selectedTab == 1) Color.White else AmberWave,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Saved (${starredTranslations.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selectedTab == 1) Color.White else SlateMedium
                        )
                    }
                }
            }
        }

        if (selectedTab == 0) {
            // Frosted Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                color = FrostedGlassWhite,
                border = BorderStroke(1.dp, FrostedBorder),
                shadowElevation = 2.dp
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search phrases in Bangla or English...", fontSize = 13.sp, color = SlateMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SkyBluePrimary, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SlateMedium)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("phrase_search_bar"),
                    shape = RoundedCornerShape(18.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }

            // Category Horizontal Chips (Frosted Glass)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PhrasebookData.categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (isSelected) SkyBluePrimary else FrostedGlassWhite,
                        border = BorderStroke(1.dp, if (isSelected) SkyBluePrimary else FrostedBorder),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SlateMedium,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Phrases List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPhrases, key = { it.id }) { phrase ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phrase_card_${phrase.id}"),
                        shape = RoundedCornerShape(20.dp),
                        color = FrostedGlassWhite,
                        border = BorderStroke(1.dp, FrostedBorder),
                        shadowElevation = 3.dp
                    ) {
                        Column(modifier = Modifier.padding(15.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = SkyBlueUltraLight,
                                    border = BorderStroke(1.dp, Color(0xFFBAE6FD).copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = phrase.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SkyBlueDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Phrase", "${phrase.banglaText} - ${phrase.englishText}")
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(15.dp), tint = SlateMedium)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Bangla Text
                            Text(
                                text = phrase.banglaText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateDark
                            )

                            // Bangla Phonetics
                            Text(
                                text = phrase.banglaPronunciation,
                                fontSize = 12.sp,
                                color = SlateMuted
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // English Meaning
                            Text(
                                text = phrase.englishText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SkyBlueDark
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick Speak Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onSpeakPhrase(phrase, true) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SkyBlueUltraLight,
                                        contentColor = SkyBlueDark
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFBAE6FD).copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.Headphones, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("BN (Earbud)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onSpeakPhrase(phrase, false) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = IndigoGlow.copy(alpha = 0.12f),
                                        contentColor = IndigoGlow
                                    ),
                                    border = BorderStroke(1.dp, IndigoGlow.copy(alpha = 0.25f))
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("EN (Speaker)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Starred Translations List
            if (starredTranslations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = FrostedGlassWhite,
                        border = BorderStroke(1.dp, FrostedBorder),
                        modifier = Modifier.padding(24.dp),
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(AmberWave.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = AmberWave,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "No Starred Translations Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SlateDark
                            )
                            Text(
                                text = "Tap the star icon on any conversation bubble to save it here for quick access.",
                                fontSize = 13.sp,
                                color = SlateMuted
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(starredTranslations, key = { it.id }) { item ->
                        TranslationBubbleItem(
                            item = item,
                            onReplayAudio = {
                                onReplayAudio(
                                    item.translatedText,
                                    item.targetLanguage,
                                    if (item.speakerRole == "YOU_BANGLA") "PARTNER_ENGLISH" else "YOU_BANGLA"
                                )
                            },
                            onToggleStar = { onToggleStar(item) },
                            onCopyText = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Translation", item.translatedText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Translation copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

