package com.apesource.account.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apesource.account.data.entity.Bill
import com.apesource.account.ui.theme.*
import com.apesource.account.ui.viewmodel.AccountViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: AccountViewModel = viewModel(),
    onBack: () -> Unit
) {
    var selectedPeriod by remember { mutableStateOf(1) }
    var chartTab by remember { mutableStateOf(0) }
    var classifyTab by remember { mutableStateOf(0) }
    var rankTab by remember { mutableStateOf(0) }
    
    val periods = listOf("周", "月", "年", "自定义")
    val chartTabs = listOf("支出", "收入", "结余")
    
    val periodOffset by viewModel.periodOffset.collectAsState()
    val isCurrentPeriod = remember(periodOffset) { periodOffset == 0 }
    
    val (startTime, endTime) = when (selectedPeriod) {
        0 -> viewModel.getWeekRange()
        1 -> viewModel.getMonthRange()
        2 -> viewModel.getYearRange()
        else -> viewModel.getMonthRange()
    }
    
    var expense by remember { mutableStateOf(0.0) }
    var income by remember { mutableStateOf(0.0) }
    val balance = income + expense
    
    var expenseStats by remember { mutableStateOf(emptyMap<String, Double>()) }
    var incomeStats by remember { mutableStateOf(emptyMap<String, Double>()) }
    
    var expenseTrend by remember { mutableStateOf(emptyList<Pair<Long, Double>>()) }
    var incomeTrend by remember { mutableStateOf(emptyList<Pair<Long, Double>>()) }
    
    var expenseRanking by remember { mutableStateOf(emptyList<Bill>()) }
    var incomeRanking by remember { mutableStateOf(emptyList<Bill>()) }
    
    LaunchedEffect(startTime, endTime) {
        launch { viewModel.getTotalExpense(startTime, endTime).collect { expense = it } }
        launch { viewModel.getTotalIncome(startTime, endTime).collect { income = it } }
        launch { viewModel.getCategoryStatistics(startTime, endTime, "expense").collect { expenseStats = it } }
        launch { viewModel.getCategoryStatistics(startTime, endTime, "income").collect { incomeStats = it } }
        launch { viewModel.getDailyTrend(startTime, endTime, "expense").collect { expenseTrend = it } }
        launch { viewModel.getDailyTrend(startTime, endTime, "income").collect { incomeTrend = it } }
        launch { viewModel.getBillRanking(startTime, endTime, "expense").collect { expenseRanking = it } }
        launch { viewModel.getBillRanking(startTime, endTime, "income").collect { incomeRanking = it } }
    }
    
    val chartTotal = when (chartTab) {
        0 -> Math.abs(expense)
        1 -> income
        else -> Math.abs(balance)
    }

    val classifyTotal = when (classifyTab) {
        0 -> Math.abs(expense)
        else -> income
    }

    val daysInPeriod = when (selectedPeriod) {
        0 -> 7
        1 -> viewModel.getDaysInMonth()
        2 -> Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR)
        else -> viewModel.getDaysInMonth()
    }.coerceAtLeast(1)

    val periodDisplayText = remember(periodOffset, selectedPeriod) { viewModel.getPeriodDisplayText(selectedPeriod) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { viewModel.prevPeriod() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "上一周期",
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = periodDisplayText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (!isCurrentPeriod) {
                                    val offsetText = when (selectedPeriod) {
                                        0 -> "周"
                                        1 -> "个月"
                                        2 -> "年"
                                        else -> "个月"
                                    }
                                    Text(
                                        text = "(" + (periodOffset * -1).toString() + offsetText + "前)",
                                        fontSize = 10.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = { viewModel.nextPeriod() },
                                enabled = !isCurrentPeriod,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "下一周期",
                                    tint = if (isCurrentPeriod) TextSecondary else Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            periods.forEachIndexed { index, period ->
                                FilterChip(
                                    selected = selectedPeriod == index,
                                    onClick = { 
                                        selectedPeriod = index
                                        viewModel.resetPeriod()
                                    },
                                    label = { Text(period, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF5F5F5)
                                    ),
                                    modifier = Modifier.height(32.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        icon = Icons.Default.TrendingDown,
                        label = "支出",
                        value = expense,
                        isExpense = true,
                        color = ExpenseColor,
                        bgColor = CardYellow,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.TrendingUp,
                        label = "收入",
                        value = income,
                        isExpense = false,
                        color = IncomeColor,
                        bgColor = CardCyan,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        icon = Icons.Default.Balance,
                        label = "结余",
                        value = balance,
                        isExpense = balance < 0,
                        color = if (balance < 0) ExpenseColor else IncomeColor,
                        bgColor = if (balance < 0) CardYellow else CardCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (chartTab) {
                                    0 -> "每日支出趋势"
                                    1 -> "每日收入趋势"
                                    else -> "每日结余趋势"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row {
                                chartTabs.forEachIndexed { index, tab ->
                                    TextButton(
                                        onClick = { chartTab = index },
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text(
                                            text = tab,
                                            fontSize = 12.sp,
                                            color = if (chartTab == index) Primary else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val trendData = when (chartTab) {
                            0 -> expenseTrend
                            1 -> incomeTrend
                            else -> expenseTrend.map { (date, value) ->
                                val incomeValue = incomeTrend.find { it.first == date }?.second ?: 0.0
                                date to (incomeValue + value)
                            }
                        }
                        DailyTrendChart(
                            data = trendData, 
                            showBalance = chartTab == 2,
                            incomeData = incomeTrend,
                            periodType = selectedPeriod
                        )
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "分类构成",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row {
                                TextButton(
                                    onClick = { classifyTab = 0 },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "支出",
                                        fontSize = 12.sp,
                                        color = if (classifyTab == 0) Primary else TextSecondary
                                    )
                                }
                                TextButton(
                                    onClick = { classifyTab = 1 },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "收入",
                                        fontSize = 12.sp,
                                        color = if (classifyTab == 1) Primary else TextSecondary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val classifyStats = if (classifyTab == 1) incomeStats else expenseStats
                        if (classifyStats.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PieChart(data = classifyStats.toList())
                                Spacer(modifier = Modifier.width(16.dp))
                                CategoryList(stats = classifyStats, total = classifyTotal, isIncome = classifyTab == 1)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "暂无数据", color = TextSecondary)
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "单笔排行",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Row {
                                TextButton(
                                    onClick = { rankTab = 0 },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "支出",
                                        fontSize = 12.sp,
                                        color = if (rankTab == 0) Primary else TextSecondary
                                    )
                                }
                                TextButton(
                                    onClick = { rankTab = 1 },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = "收入",
                                        fontSize = 12.sp,
                                        color = if (rankTab == 1) Primary else TextSecondary
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val ranking = if (rankTab == 1) incomeRanking else expenseRanking
                        if (ranking.isNotEmpty()) {
                            for (bill in ranking) {
                                RankingItem(bill = bill)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "暂无数据", color = TextSecondary)
                            }
                        }
                        
                        if (ranking.size >= 5) {
                            TextButton(
                                onClick = {},
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "展开", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "账单汇总",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            listOf("日期", "支出", "收入", "结余").forEach { label ->
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text(
                                text = "总计",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "-${String.format("%.2f", Math.abs(expense))}¥",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "+${String.format("%.2f", income)}¥",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (balance >= 0) "${String.format("%.2f", balance)}¥" else "-${String.format("%.2f", Math.abs(balance))}¥",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (balance >= 0) IncomeColor else ExpenseColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Text(
                                text = "日均",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "-${String.format("%.2f", Math.abs(expense) / daysInPeriod)}¥",
                                fontSize = 12.sp,
                                color = ExpenseColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "+${String.format("%.2f", income / daysInPeriod)}¥",
                                fontSize = 12.sp,
                                color = IncomeColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (balance >= 0) "${String.format("%.2f", balance / daysInPeriod)}¥" else "-${String.format("%.2f", Math.abs(balance) / daysInPeriod)}¥",
                                fontSize = 12.sp,
                                color = if (balance >= 0) IncomeColor else ExpenseColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Double,
    isExpense: Boolean,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isExpense) "-${String.format("%.2f", Math.abs(value))}¥" else "${String.format("%.2f", value)}¥",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun DailyTrendChart(
    data: List<Pair<Long, Double>>,
    showBalance: Boolean = false,
    incomeData: List<Pair<Long, Double>> = emptyList(),
    periodType: Int = 1
) {
    val (totalUnits, markedUnits) = when (periodType) {
        0 -> 7 to listOf(1, 2, 3, 4, 5, 6, 7)
        1 -> 30 to listOf(1, 8, 16, 23, 30)
        2 -> 12 to listOf(1, 3, 6, 9, 12)
        else -> 30 to listOf(1, 8, 16, 23, 30)
    }
    
    val dataMap = data.toMap()
    
    val allValues = mutableListOf<Double>()
    for (unit in 1..totalUnits) {
        val value = dataMap.entries.find { 
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.key
            when (periodType) {
                0 -> cal.get(Calendar.DAY_OF_WEEK) == (unit % 7 + 1)
                1 -> cal.get(Calendar.DAY_OF_MONTH) == unit
                2 -> cal.get(Calendar.MONTH) + 1 == unit
                else -> cal.get(Calendar.DAY_OF_MONTH) == unit
            }
        }?.value ?: 0.0
        allValues.add(Math.abs(value))
    }
    val maxValue = allValues.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    
    val padding = 8.dp
    val barWidth = when (periodType) {
        0 -> 30.dp
        1 -> 12.dp
        2 -> 24.dp
        else -> 12.dp
    }
    
    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        val canvasWidth = size.width - padding.toPx() * 2
        val canvasHeight = size.height - padding.toPx() * 2 - 20.dp.toPx()
        
        val maxBarArea = if (showBalance) canvasHeight / 2f else canvasHeight
        val centerY = padding.toPx() + canvasHeight / 2f
        
        if (showBalance) {
            drawLine(
                color = Divider,
                start = Offset(padding.toPx(), centerY),
                end = Offset(size.width - padding.toPx(), centerY),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        for (unit in 1..totalUnits) {
            val index = unit - 1
            val x = if (totalUnits > 1) {
                padding.toPx() + index * (canvasWidth / (totalUnits - 1))
            } else {
                padding.toPx() + canvasWidth / 2
            }
            val value = dataMap.entries.find { 
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.key
                when (periodType) {
                    0 -> cal.get(Calendar.DAY_OF_WEEK) == (unit % 7 + 1)
                    1 -> cal.get(Calendar.DAY_OF_MONTH) == unit
                    2 -> cal.get(Calendar.MONTH) + 1 == unit
                    else -> cal.get(Calendar.DAY_OF_MONTH) == unit
                }
            }?.value ?: 0.0
            
            drawRoundRect(
                color = if (showBalance) Divider.copy(alpha = 0.3f) else ExpenseColor.copy(alpha = 0.1f),
                topLeft = Offset(x - barWidth.toPx() / 2, padding.toPx()),
                size = Size(barWidth.toPx(), canvasHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            if (value != 0.0) {
                val barHeight = (Math.abs(value) / maxValue).toFloat() * maxBarArea * 0.8f
                val color = if (showBalance) {
                    if (value >= 0) IncomeColor else ExpenseColor
                } else {
                    ExpenseColor
                }
                
                if (showBalance) {
                    if (value >= 0) {
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x - barWidth.toPx() / 2, centerY - barHeight),
                            size = Size(barWidth.toPx(), barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    } else {
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x - barWidth.toPx() / 2, centerY),
                            size = Size(barWidth.toPx(), barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                } else {
                    val y = padding.toPx() + canvasHeight - barHeight
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x - barWidth.toPx() / 2, y),
                        size = Size(barWidth.toPx(), barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        markedUnits.forEach { unit ->
            val label = when (periodType) {
                0 -> listOf("一", "二", "三", "四", "五", "六", "日")[unit - 1]
                2 -> "${unit}月"
                else -> unit.toString()
            }
            Text(
                text = label,
                fontSize = 10.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PieChart(data: List<Pair<String, Double>>) {
    val total = data.sumOf { it.second }
    val colors = listOf(
        Primary,
        Color(0xFF2196F3),
        Color(0xFF4CAF50),
        Color(0xFF9C27B0),
        Color(0xFFF44336),
        Color(0xFF00BCD4)
    )
    
    Canvas(modifier = Modifier.size(120.dp)) {
        var startAngle = 0f
        
        data.forEachIndexed { index, (_, amount) ->
            val angle = (amount / total).toFloat() * 360f
            val color = colors[index % colors.size]
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = angle,
                useCenter = true,
                size = Size(100.dp.toPx(), 100.dp.toPx()),
                topLeft = Offset(10.dp.toPx(), 10.dp.toPx())
            )
            
            startAngle += angle
        }
    }
}

@Composable
fun CategoryList(stats: Map<String, Double>, total: Double, isIncome: Boolean = false) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "分类",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "占比",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.width(40.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "金额",
                fontSize = 11.sp,
                color = TextSecondary,
                modifier = Modifier.width(70.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        stats.entries.forEachIndexed { index, entry ->
            val name = entry.key
            val amount = entry.value
            val percentage = if (total > 0) (Math.abs(amount) / total * 100).toInt() else 0
            val colors = listOf(
                Primary,
                Color(0xFF2196F3),
                Color(0xFF4CAF50),
                Color(0xFF9C27B0),
                Color(0xFFF44336),
                Color(0xFF00BCD4)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = colors[index % colors.size]
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    fontSize = 12.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$percentage%",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.width(40.dp)
                )
                val sign = if (isIncome) "+" else "-"
                val amountColor = if (isIncome) IncomeColor else ExpenseColor
                
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${sign}${String.format("%.2f", Math.abs(amount))}¥",
                    fontSize = 12.sp,
                    color = amountColor,
                    modifier = Modifier.width(70.dp)
                )
            }
            
            if (index < stats.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun RankingItem(bill: Bill) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = RoundedCornerShape(4.dp),
            color = if (bill.type == "expense") ExpenseColor.copy(alpha = 0.1f) else IncomeColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = "${bill.categoryName.first()}",
                fontSize = 12.sp,
                color = if (bill.type == "expense") ExpenseColor else IncomeColor,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bill.categoryName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            if (bill.remark.isNotEmpty()) {
                Text(
                    text = bill.remark,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        
        Text(
            text = "${if (bill.type == "expense") "-" else "+"}${String.format("%.2f", Math.abs(bill.amount))}¥",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (bill.type == "expense") ExpenseColor else IncomeColor
        )
    }
}
