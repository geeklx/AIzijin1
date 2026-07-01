package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Sector
import com.example.data.Stock
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeutralDarkBg
import com.example.ui.theme.StockGreen
import com.example.ui.theme.StockGreenContainer
import com.example.ui.theme.StockGreenText
import com.example.ui.theme.StockRed
import com.example.ui.theme.StockRedContainer
import com.example.ui.theme.StockRedText
import com.example.viewmodel.SortOrder
import com.example.viewmodel.StockFlowViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: StockFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    StockFlowDashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun StockFlowDashboardScreen(
    viewModel: StockFlowViewModel,
    modifier: Modifier = Modifier
) {
    val sectors by viewModel.displaySectors.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()
    val selectedSector by viewModel.selectedSector.collectAsState()

    // Dynamic metrics
    val totalInflowCount by viewModel.totalInflowSectorsCount.collectAsState()
    val totalOutflowCount by viewModel.totalOutflowSectorsCount.collectAsState()
    val topInflow by viewModel.topInflowSector.collectAsState()
    val topOutflow by viewModel.topOutflowSector.collectAsState()

    // Dynamic local time
    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val timeZone = java.util.TimeZone.getTimeZone("GMT+8")
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        val dateFormat = SimpleDateFormat("MM月dd日", Locale.getDefault()).apply {
            this.timeZone = timeZone
        }
        while (true) {
            val now = Date()
            currentTimeStr = timeFormat.format(now)
            currentDateStr = dateFormat.format(now)
            kotlinx.coroutines.delay(1000)
        }
    }

    // Scroll state for the main page
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- 1. TOP HEADER BRAND PANEL ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${if (currentDateStr.isEmpty()) "07月01日" else currentDateStr}午盘 资金流向",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSimulating) StockRed else Color.Gray)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSimulating) "实时动态数据刷新中" else "静态基准数据（对应上传图）",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // Dynamic Clock
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "时间: ${if (currentTimeStr.isEmpty()) "11:30" else currentTimeStr}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 2. BANNER HIGHLIGHT SUMMARY ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, CardBorderColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "资金极值雷达",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "流入: $totalInflowCount | 流出: $totalOutflowCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Top Inflow Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = StockRedContainer.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("资金榜首", fontSize = 10.sp, color = StockRedText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = topInflow?.name ?: "证券II",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StockRedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "+${String.format("%.2f", topInflow?.flowAmount ?: 57.73)}亿",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = StockRedText
                            )
                        }
                    }

                    // Top Outflow Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = StockGreenContainer.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("资金榜尾", fontSize = 10.sp, color = StockGreenText, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = topOutflow?.name ?: "通信技术",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StockGreenText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${String.format("%.2f", topOutflow?.flowAmount ?: -264.04)}亿",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = StockGreenText
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 3. CONTROLS PANEL (SEARCH & SIMULATION) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_bar"),
                    placeholder = { Text("搜索板块名称 (例如: AI, 芯片, 证券)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "清除搜索")
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simulation toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.toggleSimulation() }
                    ) {
                        Switch(
                            checked = isSimulating,
                            onCheckedChange = { viewModel.toggleSimulation() },
                            modifier = Modifier.testTag("simulation_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = StockRed,
                                checkedTrackColor = StockRedContainer,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "实时模拟行情刷新",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "开启后每3秒模拟资金扰动",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Reset button
                    Button(
                        onClick = { viewModel.resetData() },
                        modifier = Modifier.testTag("reset_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "重置",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("重置数据", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- 4. SECTORS DATA GRID (EXACTLY MATCHING THE 26 ITEMS FROM THE SCREENSHOT) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(3.dp),
            border = BorderStroke(1.dp, CardBorderColor)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Table Header row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "部分板块资金流向排行",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "显示过滤后板块量: ${sectors.size} / 26",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Interactive Sorting toggle button
                    Button(
                        onClick = {
                            val nextOrder = if (sortOrder == SortOrder.NET_FLOW_HIGH_TO_LOW) {
                                SortOrder.NET_FLOW_LOW_TO_HIGH
                            } else {
                                SortOrder.NET_FLOW_HIGH_TO_LOW
                            }
                            viewModel.setSortOrder(nextOrder)
                        },
                        modifier = Modifier.testTag("sort_order_toggle"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (sortOrder == SortOrder.NET_FLOW_HIGH_TO_LOW) "按资金从高到低" else "按资金从低到高",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (sortOrder == SortOrder.NET_FLOW_HIGH_TO_LOW) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = "排序方式",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                if (sectors.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "无结果",
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "未搜索到匹配的行业板块",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    // Dual Column Slice (Exactly matching the 2 column list of the user's uploaded image!)
                    // Ranks 1 to 13 on Left, 14 to 26 on Right.
                    // If filtered, we divide the current list in half and display.
                    val halfSize = (sectors.size + 1) / 2
                    val leftColumnSectors = sectors.take(halfSize)
                    val rightColumnSectors = sectors.drop(halfSize)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Left Column (Ranks 1 to 13)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            leftColumnSectors.forEach { sector ->
                                SectorItemRow(
                                    sector = sector,
                                    overallIndex = sectors.indexOf(sector) + 1,
                                    totalItems = sectors.size,
                                    onClick = { viewModel.selectSector(sector) }
                                )
                            }
                        }

                        // Right Column (Ranks 14 to 26)
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rightColumnSectors.forEach { sector ->
                                SectorItemRow(
                                    sector = sector,
                                    overallIndex = sectors.indexOf(sector) + 1,
                                    totalItems = sectors.size,
                                    onClick = { viewModel.selectSector(sector) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 5. VISUAL DISCLAIMER & TUTORIAL TIPS ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💡 操作技巧: 点击任意板块格子可打开研报分析与核心龙头股名单，还可一键调用智能 AI 分析板块未来趋势。",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "投资有风险，入市需谨慎。所有资金数据均基于模拟或历史截面生成，AI研报不构成任何实质操作建议。",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // --- 6. DRILLED DOWN DETAIL MODAL BOTTOM SHEET ---
    selectedSector?.let { sector ->
        SectorDetailBottomSheet(
            sector = sector,
            viewModel = viewModel,
            onDismiss = { viewModel.selectSector(null) }
        )
    }
}

@Composable
fun SectorItemRow(
    sector: Sector,
    overallIndex: Int,
    totalItems: Int,
    onClick: () -> Unit
) {
    val isPositive = sector.flowAmount >= 0
    val flowColor = if (isPositive) StockRedText else StockGreenText
    val formattedFlow = if (isPositive) {
        "+${String.format("%.2f", sector.flowAmount)}亿"
    } else {
        "${String.format("%.2f", sector.flowAmount)}亿"
    }

    // Determine Rank Badge style based on overall index/rules
    // Ranks 1 to 6 (positive flow): Solid red badge with white text
    // Ranks bottom 3 (index in the last 3 items when sorted descending): Solid green badge with white text
    // Otherwise: Green outline badge with green text
    val isTop6 = isPositive && overallIndex <= 6
    val isBottom3 = !isPositive && (overallIndex > totalItems - 3)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sector_item_${sector.id}")
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Rank and Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when {
                            isTop6 -> StockRed
                            isBottom3 -> StockGreen
                            else -> Color.Transparent
                        }
                    )
                    .then(
                        if (!isTop6 && !isBottom3) {
                            Modifier.border(
                                1.dp,
                                if (isPositive) StockRed.copy(alpha = 0.5f) else StockGreen.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = overallIndex.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isTop6 || isBottom3 -> Color.White
                        isPositive -> StockRedText
                        else -> StockGreenText
                    }
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Sector Name
            Text(
                text = sector.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Mini Custom Progress Bar with dot
        MiniFlowProgressBar(
            progress = sector.progress,
            isPositive = isPositive,
            modifier = Modifier
                .width(42.dp)
                .padding(horizontal = 4.dp)
        )

        // Flow amount text
        Text(
            text = formattedFlow,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = flowColor,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = 52.dp)
        )
    }
}

@Composable
fun MiniFlowProgressBar(
    progress: Float,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val barColor = if (isPositive) StockRed else StockGreen
    val barBgColor = if (isPositive) StockRedContainer else StockGreenContainer
    val absProgress = Math.abs(progress).coerceIn(0.05f, 1.0f)

    Box(
        modifier = modifier
            .height(10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(CircleShape)
                .background(barBgColor)
        )
        // Indicator Fill
        Row(
            modifier = Modifier
                .fillMaxWidth(absProgress)
                .height(4.dp)
                .clip(CircleShape)
                .background(barColor),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Little thumb dot at the end
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectorDetailBottomSheet(
    sector: Sector,
    viewModel: StockFlowViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- Bottom Sheet Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sector.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (sector.flowAmount >= 0) StockRedContainer else StockGreenContainer
                            ),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (sector.flowAmount >= 0) "主力流入" else "主力流出",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (sector.flowAmount >= 0) StockRedText else StockGreenText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "今日主力净流向: ${if (sector.flowAmount >= 0) "+" else ""}${sector.flowAmount}亿",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sector.flowAmount >= 0) StockRedText else StockGreenText
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .testTag("bottom_sheet_close")
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Sector Description ---
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "板块基本面画像",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = sector.description,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 5-DAY HISTORY FLOW CHART ---
            Text(
                text = "5日主力资金流向趋势 (亿)",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, CardBorderColor)
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    FlowLineChart(
                        historyData = sector.historyFlows,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- LEADING STOCKS ---
            Text(
                text = "板块前四大主力核心龙头股",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sector.leadingStocks.forEach { stock ->
                    StockItemRow(stock = stock)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- AI ANALYTICS ACTION CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = "AI量化研报",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini 3.5 AI智能研报分析",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Button to trigger AI
                        Button(
                            onClick = { viewModel.requestAiAnalysis(sector) },
                            enabled = !isAnalyzing,
                            modifier = Modifier.testTag("ai_report_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "启动",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("立即深度研判", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (isAnalyzing) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AI智能量化模型正在对【${sector.name}】进行趋势推算...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else if (analysisResult != null) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = analysisResult ?: "",
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "※ 智能分析报告由大模型动态评估生成。投资需谨慎决策，报告内容不构成直接投资和买卖建议。",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                lineHeight = 14.sp
                            )
                        }
                    } else {
                        Text(
                            text = "通过一键点击，智能分析算法将评估当前【${sector.name}】的资金结构、流入比例、主力博弈强度并生成针对未来交易节点的应对研报。",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun StockItemRow(stock: Stock) {
    val isPositive = stock.changePercent.startsWith("+")
    val textColor = if (isPositive) StockRedText else StockGreenText
    val containerBg = if (isPositive) StockRedContainer.copy(alpha = 0.5f) else StockGreenContainer.copy(alpha = 0.5f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            .border(1.dp, CardBorderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stock.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "净流向: ${if (stock.flowAmount >= 0) "+" else ""}${String.format("%.2f", stock.flowAmount)}亿",
                fontSize = 11.sp,
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stock.price,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(end = 12.dp)
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(containerBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.changePercent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun FlowLineChart(
    historyData: List<Double>,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background == NeutralDarkBg
    val gridColor = if (isDark) Color(0xFF333333) else Color(0xFFE5E5E5)
    val labelColor = if (isDark) Color(0xFF888888) else Color(0xFF666666)

    val maxVal = remember(historyData) {
        val maxAbs = historyData.map { Math.abs(it) }.maxOrNull() ?: 10.0
        (maxAbs * 1.2).coerceAtLeast(10.0)
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val paddingLeft = 40.dp.toPx()
        val paddingRight = 10.dp.toPx()
        val paddingTop = 15.dp.toPx()
        val paddingBottom = 20.dp.toPx()

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Draw Zero Center Line
        val zeroY = paddingTop + chartHeight / 2f
        drawLine(
            color = gridColor,
            start = Offset(paddingLeft, zeroY),
            end = Offset(width - paddingRight, zeroY),
            strokeWidth = 1.dp.toPx()
        )

        // Draw top & bottom boundary lines
        drawLine(
            color = gridColor.copy(alpha = 0.5f),
            start = Offset(paddingLeft, paddingTop),
            end = Offset(width - paddingRight, paddingTop),
            strokeWidth = 0.5.dp.toPx()
        )
        drawLine(
            color = gridColor.copy(alpha = 0.5f),
            start = Offset(paddingLeft, paddingTop + chartHeight),
            end = Offset(width - paddingRight, paddingTop + chartHeight),
            strokeWidth = 0.5.dp.toPx()
        )

        // Plot points
        val pointCount = historyData.size
        val points = mutableListOf<Offset>()
        val stepX = chartWidth / (pointCount - 1).coerceAtLeast(1)

        for (i in 0 until pointCount) {
            val value = historyData[i]
            val pct = (value / maxVal).coerceIn(-1.0, 1.0)
            // Draw Y relative to the center zeroY line
            val y = zeroY - (pct * (chartHeight / 2f)).toFloat()
            val x = paddingLeft + i * stepX
            points.add(Offset(x, y))
        }

        // Draw gradient area underneath the line
        val positivePath = Path()
        val negativePath = Path()

        positivePath.moveTo(paddingLeft, zeroY)
        negativePath.moveTo(paddingLeft, zeroY)

        points.forEach { pt ->
            if (pt.y <= zeroY) {
                positivePath.lineTo(pt.x, pt.y)
                negativePath.lineTo(pt.x, zeroY)
            } else {
                positivePath.lineTo(pt.x, zeroY)
                negativePath.lineTo(pt.x, pt.y)
            }
        }
        positivePath.lineTo(points.last().x, zeroY)
        positivePath.close()

        negativePath.lineTo(points.last().x, zeroY)
        negativePath.close()

        // Fill paths with gradient
        drawPath(
            path = positivePath,
            brush = Brush.verticalGradient(
                colors = listOf(StockRed.copy(alpha = 0.25f), Color.Transparent),
                startY = paddingTop,
                endY = zeroY
            )
        )
        drawPath(
            path = negativePath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, StockGreen.copy(alpha = 0.25f)),
                startY = zeroY,
                endY = paddingTop + chartHeight
            )
        )

        // Draw trend line
        val strokePath = Path()
        strokePath.moveTo(points.first().x, points.first().y)
        for (i in 1 until points.size) {
            strokePath.lineTo(points[i].x, points[i].y)
        }

        drawPath(
            path = strokePath,
            color = if (historyData.last() >= 0) StockRed else StockGreen,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw nodes/points and value labels
        points.forEachIndexed { index, pt ->
            val value = historyData[index]
            val pointColor = if (value >= 0) StockRed else StockGreen
            drawCircle(
                color = pointColor,
                radius = 4.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = pt
            )
        }

        // --- Draw text labels dynamically ---
        // Y-axis labels (+Max, 0, -Max)
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = labelColor.hashCode()
                textSize = 9.dp.toPx()
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawText("+${String.format("%.1f", maxVal)}", paddingLeft - 5.dp.toPx(), paddingTop + 4.dp.toPx(), paint)
            drawText("0.0", paddingLeft - 5.dp.toPx(), zeroY + 3.dp.toPx(), paint)
            drawText("-${String.format("%.1f", maxVal)}", paddingLeft - 5.dp.toPx(), paddingTop + chartHeight + 3.dp.toPx(), paint)

            // X-axis day labels
            paint.textAlign = android.graphics.Paint.Align.CENTER
            val days = listOf("4日前", "3日前", "2日前", "昨日", "今日")
            points.forEachIndexed { index, pt ->
                if (index < days.size) {
                    drawText(days[index], pt.x, height - 2.dp.toPx(), paint)
                }
            }
        }
    }
}
