package com.gameora.app.ui.screens.store

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gameora.app.data.model.DisplayPrice
import com.gameora.app.data.model.ProductCategory
import com.gameora.app.ui.components.ProductCard

@Composable
fun StoreScreen(
    onProductClick: (String) -> Unit,
    onCreateProductClick: () -> Unit,
    viewModel: StoreViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        floatingActionButton = {
            if (uiState.isSeller) {
                FloatingActionButton(
                    onClick = onCreateProductClick,
                    shape = RoundedCornerShape(18.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة منتج"
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 22.dp,
                        bottom = 14.dp
                    )
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Column {
                        Text(
                            text = "المتجر",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = "حسابات ومنتجات وخدمات الألعاب",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "تحديث المتجر"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("ابحث باسم اللعبة أو الإعلان أو البائع")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث"
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                StoreFiltersRow(
                    uiState = uiState,
                    onGameSelected = viewModel::onGameFilterChanged,
                    onCategorySelected = viewModel::onCategoryFilterChanged,
                    onSortSelected = viewModel::onSortOptionChanged
                )
            }

            when {

                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null && uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "تعذر تحميل المتجر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = uiState.errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(onClick = { viewModel.loadProducts() }) {
                                Text("إعادة المحاولة")
                            }
                        }
                    }
                }

                uiState.results.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "لا توجد نتائج",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "جرّب كلمة بحث أو فلتر مختلف",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 28.dp
                        )
                    ) {
                        items(
                            items = uiState.results,
                            key = { it.id }
                        ) { product ->

                            ProductCard(
                                product = product,

                                displayPrice =
                                    uiState.displayPrices[product.id]
                                        ?: DisplayPrice(
                                            amount = product.originalPrice,
                                            currencyCode = product.originalCurrency
                                        ),

                                onClick = {
                                    onProductClick(product.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * صف فلاتر بسيط: اللعبة، التصنيف، والترتيب.
 * كل قائمة مبنية على بيانات حقيقية (الألعاب من Backend، التصنيفات من Enum المشروع).
 */
@Composable
private fun StoreFiltersRow(
    uiState: StoreUiState,
    onGameSelected: (String?) -> Unit,
    onCategorySelected: (ProductCategory?) -> Unit,
    onSortSelected: (StoreSortOption) -> Unit
) {
    var gameMenuExpanded by remember { mutableStateOf(false) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val selectedGameLabel =
        uiState.games.firstOrNull { it.id == uiState.selectedGameId }
            ?.let { it.nameAr.ifBlank { it.name } }
            ?: "كل الألعاب"

    val selectedCategoryLabel = uiState.selectedCategory?.name ?: "كل التصنيفات"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Box {
            FilterChip(
                selected = uiState.selectedGameId != null,
                onClick = { gameMenuExpanded = true },
                label = { Text(selectedGameLabel) }
            )

            DropdownMenu(
                expanded = gameMenuExpanded,
                onDismissRequest = { gameMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("كل الألعاب") },
                    onClick = {
                        onGameSelected(null)
                        gameMenuExpanded = false
                    }
                )

                uiState.games.forEach { game ->
                    DropdownMenuItem(
                        text = { Text(game.nameAr.ifBlank { game.name }) },
                        onClick = {
                            onGameSelected(game.id)
                            gameMenuExpanded = false
                        }
                    )
                }
            }
        }

        Box {
            FilterChip(
                selected = uiState.selectedCategory != null,
                onClick = { categoryMenuExpanded = true },
                label = { Text(selectedCategoryLabel) }
            )

            DropdownMenu(
                expanded = categoryMenuExpanded,
                onDismissRequest = { categoryMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("كل التصنيفات") },
                    onClick = {
                        onCategorySelected(null)
                        categoryMenuExpanded = false
                    }
                )

                ProductCategory.entries.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            onCategorySelected(category)
                            categoryMenuExpanded = false
                        }
                    )
                }
            }
        }

        Box {
            FilterChip(
                selected = uiState.sortOption != StoreSortOption.NEWEST,
                onClick = { sortMenuExpanded = true },
                label = { Text(uiState.sortOption.labelAr) }
            )

            DropdownMenu(
                expanded = sortMenuExpanded,
                onDismissRequest = { sortMenuExpanded = false }
            ) {
                StoreSortOption.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.labelAr) },
                        onClick = {
                            onSortSelected(option)
                            sortMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}
