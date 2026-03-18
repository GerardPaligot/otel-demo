package com.paligot.otel.demo.sponsors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.navigation.NavController
import com.paligot.otel.demo.schedules.AppBottomBar
import com.paligot.otel.demo.schedules.BottomNavItem

// ── Data models ───────────────────────────────────────────────────────────────

enum class SponsorTier(val label: String) {
    PLATINUM("Platinum Sponsors"),
    GOLD("Gold Sponsors"),
    SILVER("Silver Sponsors"),
    BRONZE("Bronze Sponsors"),
}

data class SponsorUi(
    val name: String,
    val category: String,
    val tier: SponsorTier,
    val logoColor: Color,
    val logoTextColor: Color = Color.White,
    val websiteUrl: String = "",
    val logoUrl: String = "",
)

// ── Sample data ───────────────────────────────────────────────────────────────

// Sample data and UI state are managed by SponsorsViewModel

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SponsorsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SponsorsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Our Sponsors",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                },
            )
        },
        bottomBar = {
            AppBottomBar(
                navController = navController,
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                ) {
                    Text(
                        text = "Failed to load sponsors\n${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            else -> {
                LazyColumn(
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Search
                    item {
                        SponsorSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }

                    // Platinum cards — full width stacked
                    items(uiState.platinumSponsors) { sponsor ->
                        PlatinumSponsorCard(
                            sponsor = sponsor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }

                    // Gold tier
                    if (uiState.goldSponsors.isNotEmpty()) {
                        item {
                            TierHeader(
                                label = SponsorTier.GOLD.label,
                                icon = { Icon(Icons.Outlined.Star, null, tint = Color(0xFFFFC107)) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                        item {
                            TwoColumnGrid(
                                sponsors = uiState.goldSponsors,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    // Silver tier
                    if (uiState.silverSponsors.isNotEmpty()) {
                        item {
                            TierHeader(
                                label = SponsorTier.SILVER.label,
                                icon = {
                                    Icon(
                                        Icons.Outlined.WorkspacePremium,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                        item {
                            ThreeColumnGrid(
                                sponsors = uiState.silverSponsors,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    // Bronze tier
                    if (uiState.bronzeSponsors.isNotEmpty()) {
                        item {
                            TierHeader(
                                label = SponsorTier.BRONZE.label,
                                icon = {
                                    Icon(
                                        Icons.Outlined.WorkspacePremium,
                                        null,
                                        tint = Color(0xFFCD7F32),
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                        item {
                            ThreeColumnGrid(
                                sponsors = uiState.bronzeSponsors,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ── Sub-components ────────────────────────────────────────────────────────────

@Composable
private fun SponsorSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text("Search sponsors...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = Color.Transparent,
        ),
        modifier = modifier,
    )
}

@Composable
private fun TierHeader(
    label: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        icon()
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun PlatinumSponsorCard(
    sponsor: SponsorUi,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Column {
            // Logo area
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                if (sponsor.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = sponsor.logoUrl,
                        contentDescription = sponsor.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    )
                } else {
                    SponsorLogo(
                        name = sponsor.name,
                        color = sponsor.logoColor,
                        textColor = sponsor.logoTextColor,
                        size = 100.dp,
                    )
                }
            }
            // Footer
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Column {
                    Text(
                        text = sponsor.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = sponsor.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = "Open website",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun TwoColumnGrid(
    sponsors: List<SponsorUi>,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        sponsors.chunked(2).forEach { rowSponsors ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowSponsors.forEach { sponsor ->
                    GoldSponsorCard(
                        sponsor = sponsor,
                        modifier = Modifier.weight(1f),
                    )
                }
                // fill empty slot if odd number
                if (rowSponsors.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GoldSponsorCard(
    sponsor: SponsorUi,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.4f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                if (sponsor.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = sponsor.logoUrl,
                        contentDescription = sponsor.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    )
                } else {
                    SponsorLogo(
                        name = sponsor.name,
                        color = sponsor.logoColor,
                        textColor = sponsor.logoTextColor,
                        size = 60.dp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sponsor.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = sponsor.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ThreeColumnGrid(
    sponsors: List<SponsorUi>,
    modifier: Modifier = Modifier,
) {
    val itemsWithCta = sponsors + listOf(null) // null = "Become a Sponsor" CTA

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        itemsWithCta.chunked(3).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowItems.forEach { sponsor ->
                    if (sponsor != null) {
                        SilverSponsorCard(
                            sponsor = sponsor,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        BecomeSponsorCard(modifier = Modifier.weight(1f))
                    }
                }
                // pad remaining slots
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SilverSponsorCard(
    sponsor: SponsorUi,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(sponsor.logoColor),
        ) {
            if (sponsor.logoUrl.isNotBlank()) {
                AsyncImage(
                    model = sponsor.logoUrl,
                    contentDescription = sponsor.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                )
            } else {
                Text(
                    text = sponsor.name.take(6),
                    style = MaterialTheme.typography.labelSmall,
                    color = sponsor.logoTextColor.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun BecomeSponsorCard(modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        ) {
            Text(
                text = "Become a Sponsor",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Composable
private fun SponsorLogo(
    name: String,
    color: Color,
    textColor: Color,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(color),
    ) {
        Text(
            text = name.take(9),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(4.dp),
        )
    }
}
