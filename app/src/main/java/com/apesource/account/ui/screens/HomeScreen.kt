package com.apesource.account.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apesource.account.data.entity.Bill
import com.apesource.account.ui.components.AddBillDialog
import com.apesource.account.ui.components.CategorySelectDialog
import com.apesource.account.ui.theme.*
import com.apesource.account.ui.viewmodel.AccountViewModel
import com.apesource.account.utils.IconUtils
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AccountViewModel = viewModel(),
    onOpenDrawer: () -> Unit,
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToBudget: () -> Unit = {},
    onBillClick: (Bill) -> Unit = {},
    onNavigateToAssets: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToBookEdit: () -> Unit = {}
) {
    val expenseCategories by viewModel.getExpenseCategories().collectAsState(initial = emptyList())
    val incomeCategories by viewModel.getIncomeCategories().collectAsState(initial = emptyList())
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    val periodOffset by viewModel.periodOffset.collectAsState()
    val selectedDayOffset by viewModel.selectedDayOffset.collectAsState()
    val isCurrentPeriod = remember(periodOffset) { periodOffset == 0 }
    val isToday = remember(selectedDayOffset) { selectedDayOffset == 0 }
    val currentBook by viewModel.currentBook.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.checkAndResetBudgetIfNewPeriod()
    }
    
    val monthRange = remember(periodOffset) { viewModel.getMonthRange(offset = periodOffset) }
    val (monthStart, monthEnd) = monthRange
    val selectedDayRange = remember(selectedDayOffset) { viewModel.getSelectedDayRange() }
    val (dayStart, dayEnd) = selectedDayRange
    
    var monthExpense by remember { mutableStateOf(0.0) }
    var monthIncome by remember { mutableStateOf(0.0) }
    var selectedDayBills by remember { mutableStateOf(emptyList<Bill>()) }
    var periodBills by remember { mutableStateOf(emptyList<Bill>()) }

    LaunchedEffect(monthStart, monthEnd) {
        launch { viewModel.getTotalExpense(monthStart, monthEnd).collect { monthExpense = it } }
        launch { viewModel.getTotalIncome(monthStart, monthEnd).collect { monthIncome = it } }
        launch { viewModel.getBillsByDateRange(monthStart, monthEnd).collect { periodBills = it } }
    }
    LaunchedEffect(dayStart, dayEnd) {
        viewModel.getBillsByDate(dayStart, dayEnd).collect { selectedDayBills = it }
    }
    
    val showAddBillDialog by viewModel.showAddBillDialog.collectAsState()
    val budget by viewModel.monthlyBudget.collectAsState()
    
    val netAssets by viewModel.getNetAssets().collectAsState(initial = 0.0)
    val totalAssets by viewModel.getTotalAssets().collectAsState(initial = 0.0)
    val totalLiabilities by viewModel.getTotalLiabilities().collectAsState(initial = 0.0)
    
    var billToEditCategory by remember { mutableStateOf<Bill?>(null) }
    
    var cardIndex by remember { mutableStateOf(0) }
    var offsetX by remember { mutableStateOf(0f) }
    val cardCount = 2
    val swipeThreshold = 50.dp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "菜单")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        onClick = { onNavigateToSearch() },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "搜索",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.showAddBillDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("记一笔", fontSize = 14.sp)
                    }
                }
            }
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
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { viewModel.prevPeriod() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "上月",
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            val periodDisplayText = remember(periodOffset) { viewModel.getPeriodDisplayText() }
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = periodDisplayText,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentPeriod) Primary else TextPrimary,
                                    maxLines = 1
                                )
                                if (!isCurrentPeriod) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        onClick = { viewModel.resetPeriod() },
                                        shape = RoundedCornerShape(10.dp),
                                        color = Primary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "返回当前",
                                            fontSize = 9.sp,
                                            color = Primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            
                            IconButton(
                                onClick = { viewModel.nextPeriod() },
                                enabled = !isCurrentPeriod,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "下月",
                                    tint = if (isCurrentPeriod) TextSecondary else Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                val density = LocalDensity.current
                val swipeThresholdPx = with(density) { swipeThreshold.toPx() }
                val screenWidth = with(density) { (LocalConfiguration.current.screenWidthDp.dp).toPx() }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (offsetX > swipeThresholdPx) {
                                        cardIndex = maxOf(0, cardIndex - 1)
                                    } else if (offsetX < -swipeThresholdPx) {
                                        cardIndex = minOf(cardCount - 1, cardIndex + 1)
                                    }
                                    offsetX = 0f
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount
                            }
                        }
                ) {
                    MonthSummaryCard(
                        expense = monthExpense,
                        income = monthIncome,
                        budget = budget,
                        todayExpense = selectedDayBills.filter { it.type == "expense" }.sumOf { it.amount },
                        viewModel = viewModel,
                        onNavigateToStatistics = onNavigateToStatistics,
                        onNavigateToBudget = onNavigateToBudget,
                        modifier = Modifier.offset { 
                            val baseOffset = if (cardIndex == 1) -screenWidth else 0f
                            androidx.compose.ui.unit.IntOffset((baseOffset + offsetX).roundToInt(), 0) 
                        }
                    )
                    
                    NetAssetsCard(
                        netAssets = netAssets,
                        totalAssets = totalAssets,
                        totalLiabilities = totalLiabilities,
                        onViewAssets = onNavigateToAssets,
                        modifier = Modifier.offset { 
                            val baseOffset = if (cardIndex == 0) screenWidth else 0f
                            androidx.compose.ui.unit.IntOffset((baseOffset + offsetX).roundToInt(), 0) 
                        }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(cardCount) { index ->
                        Surface(
                            modifier = Modifier
                                .size(if (index == cardIndex) 8.dp else 4.dp)
                                .padding(2.dp)
                                .clip(CircleShape),
                            color = if (index == cardIndex) Primary else Color(0xFFE0E0E0)
                        ) {}
                    }
                }
            }

            item {
                DateHeader(
                    selectedDayOffset = selectedDayOffset,
                    bills = selectedDayBills,
                    currentBookName = currentBook.name,
                    onPrevDay = { viewModel.prevDay() },
                    onNextDay = { viewModel.nextDay() },
                    onResetDay = { viewModel.resetDay() },
                    onAddBill = { viewModel.showAddBillDialog(true) },
                    onSwitchBook = onNavigateToBookEdit
                )
            }

            val displayBills = if (isCurrentPeriod) selectedDayBills else periodBills
            
            if (displayBills.isEmpty()) {
                item {
                    EmptyState()
                }
            } else {
                items(displayBills) { bill ->
                    BillItem(
                        bill = bill,
                        onClick = { onBillClick(bill) },
                        onCategoryClick = { if (isCurrentPeriod) billToEditCategory = bill }
                    )
                }
            }
        }
    }

    if (showAddBillDialog) {
        AddBillDialog(
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            accounts = accounts,
            currentBookName = viewModel.currentBook.value.name,
            onDismiss = { viewModel.showAddBillDialog(false) },
            onConfirm = { bill ->
                viewModel.insertBill(bill)
                viewModel.showAddBillDialog(false)
            }
        )
    }

    billToEditCategory?.let { bill ->
        val categories = if (bill.type == "expense") expenseCategories else incomeCategories
        val selectedCategory = categories.find { it.name == bill.categoryName }
        CategorySelectDialog(
            type = bill.type,
            categories = categories,
            selectedCategory = selectedCategory,
            onDismiss = { billToEditCategory = null },
            onConfirm = { category ->
                val updatedBill = bill.copy(
                    categoryName = category.name,
                    categoryIcon = category.icon
                )
                viewModel.updateBill(updatedBill)
                billToEditCategory = null
            }
        )
    }
}

@Composable
fun MonthSummaryCard(
    expense: Double,
    income: Double,
    budget: Double,
    todayExpense: Double,
    viewModel: AccountViewModel,
    onNavigateToStatistics: () -> Unit,
    onNavigateToBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyBudget = viewModel.getDailyBudget()
    
    val monthlyBalance = income + expense
    val todayAvailable = dailyBudget - Math.abs(todayExpense)
    val monthBudgetRemaining = budget - Math.abs(expense)
    val progress = if (budget > 0) (Math.abs(expense) / budget * 100).coerceAtMost(100.0).toInt() else 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardYellow)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "本月支出",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                OutlinedButton(
                    onClick = onNavigateToStatistics,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                ) {
                    Text("图表分析", color = Primary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Primary, modifier = Modifier.size(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "¥${String.format("%.2f", Math.abs(expense))}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = ExpenseColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(label = "本月结余", value = monthlyBalance, isExpense = monthlyBalance < 0)
                SummaryItem(label = "今日可用", value = todayAvailable, isExpense = todayAvailable < 0)
                BudgetItem(label = "月预算", value = monthBudgetRemaining, progress = progress, onClick = onNavigateToBudget)
            }
        }
    }
}

@Composable
fun NetAssetsCard(
    netAssets: Double,
    totalAssets: Double,
    totalLiabilities: Double,
    onViewAssets: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardCyan)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "净资产",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Icon(Icons.Default.Info, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                }
                OutlinedButton(
                    onClick = onViewAssets,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
                ) {
                    Text("查看资产", color = Primary, fontSize = 10.sp)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Primary, modifier = Modifier.size(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¥${String.format("%.2f", netAssets)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "资产",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥${String.format("%.2f", totalAssets)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = IncomeColor
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "负债",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "¥${String.format("%.2f", totalLiabilities)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseColor
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: Double,
    isExpense: Boolean = false
) {
    Column {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (value >= 0) "¥${String.format("%.2f", value)}" else "-¥${String.format("%.2f", Math.abs(value))}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isExpense) ExpenseColor else IncomeColor
        )
    }
}

@Composable
fun BudgetItem(
    label: String,
    value: Double,
    progress: Int,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "¥${String.format("%.2f", value)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (progress >= 100) ExpenseColor else Primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "$progress%",
                    fontSize = 9.sp,
                    color = if (progress >= 100) Color.White else Primary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun DateHeader(
    selectedDayOffset: Int,
    bills: List<Bill>,
    currentBookName: String = "默认账本",
    onPrevDay: () -> Unit = {},
    onNextDay: () -> Unit = {},
    onResetDay: () -> Unit = {},
    onAddBill: () -> Unit = {},
    onSwitchBook: () -> Unit = {}
) {
    val expense = bills.filter { it.type == "expense" }.sumOf { it.amount }
    val income = bills.filter { it.type == "income" }.sumOf { it.amount }
    val isToday = selectedDayOffset == 0

    val displayText: String
    val subText: String
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, selectedDayOffset)
    when (selectedDayOffset) {
        0 -> { displayText = "今天"; subText = SimpleDateFormat("M月d日 EEEE", Locale.getDefault()).format(Date(cal.timeInMillis)) }
        -1 -> { displayText = "昨天"; subText = SimpleDateFormat("M月d日 EEEE", Locale.getDefault()).format(Date(cal.timeInMillis)) }
        -2 -> { displayText = "前天"; subText = SimpleDateFormat("M月d日 EEEE", Locale.getDefault()).format(Date(cal.timeInMillis)) }
        else -> { displayText = SimpleDateFormat("M月d日", Locale.getDefault()).format(Date(cal.timeInMillis)); subText = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(cal.timeInMillis)) }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevDay,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "前一天",
                    tint = Primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = subText,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                if (!isToday) {
                    Surface(
                        onClick = onResetDay,
                        shape = RoundedCornerShape(10.dp),
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "回到今天",
                            fontSize = 9.sp,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onNextDay,
                enabled = !isToday,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "后一天",
                    tint = if (isToday) TextSecondary else Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "支出 ¥${String.format("%.2f", Math.abs(expense))}",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "收入 ¥${String.format("%.2f", income)}",
                fontSize = 12.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onSwitchBook,
                    shape = RoundedCornerShape(16.dp),
                    color = CardYellow.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentBookName,
                            fontSize = 12.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "切换账本",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BillItem(
    bill: Bill,
    onClick: () -> Unit = {},
    onCategoryClick: () -> Unit = {}
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onCategoryClick),
                shape = CircleShape,
                color = if (bill.type == "expense") CardYellow else CardCyan
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = IconUtils.getIconByName(bill.categoryIcon),
                        contentDescription = bill.categoryName,
                        tint = if (bill.type == "expense") ExpenseColor else IncomeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = bill.categoryName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    if (bill.remark.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (bill.type == "expense") CardYellow else CardCyan
                        ) {
                            Text(
                                text = bill.remark,
                                fontSize = 10.sp,
                                color = if (bill.type == "expense") ExpenseColor else IncomeColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = bill.account,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (bill.type == "expense") "-¥${String.format("%.2f", Math.abs(bill.amount))}" else "+¥${String.format("%.2f", bill.amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (bill.type == "expense") ExpenseColor else IncomeColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeFormat.format(Date(bill.dateTime)),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun NetAssetCard(
    netAssets: Double,
    assets: Double,
    liabilities: Double,
    debtRatio: Double,
    onViewAssets: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardCyan)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "净资产",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Button(
                    onClick = onViewAssets,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("查看资产", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "¥${String.format("%.2f", netAssets)}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = IncomeColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                    content = {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "资产",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¥${String.format("%.2f", assets)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeColor
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Color(0xFFE0E0E0))
                        )
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "负债",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¥${String.format("%.2f", liabilities)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseColor
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "资产负债率",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "${String.format("%.2f", debtRatio)}%",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无记录",
            fontSize = 16.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击下方 + 开始记账",
            fontSize = 14.sp,
            color = TextSecondary.copy(alpha = 0.7f)
        )
    }
}