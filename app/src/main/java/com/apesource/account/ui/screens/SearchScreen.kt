package com.apesource.account.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apesource.account.data.entity.Bill
import com.apesource.account.ui.theme.*
import com.apesource.account.ui.viewmodel.AccountViewModel
import com.apesource.account.utils.IconUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel(),
    onBillClick: (Bill) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    
    var selectedDateRange by remember { mutableStateOf("全部") }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var selectedType by remember { mutableStateOf("全部") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    var showFilters by remember { mutableStateOf(false) }
    
    val expenseCategories by viewModel.getExpenseCategories().collectAsState(initial = emptyList())
    val incomeCategories by viewModel.getIncomeCategories().collectAsState(initial = emptyList())
    
    val dateRangeOptions = listOf("全部", "本周", "本月", "自定义")
    val typeOptions = listOf("全部", "支出", "收入")
    
    val allCategories = expenseCategories + incomeCategories

    val dateRangeState = remember(selectedDateRange, startDate, endDate) {
        derivedStateOf {
            when (selectedDateRange) {
                "本周" -> {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val weekStart = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_WEEK, 6)
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    Pair(weekStart, calendar.timeInMillis)
                }
                "本月" -> {
                    val calendar = Calendar.getInstance()
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val monthStart = calendar.timeInMillis
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                    Pair(monthStart, calendar.timeInMillis)
                }
                "自定义" -> {
                    Pair(startDate, endDate)
                }
                else -> Pair(null, null)
            }
        }
    }

    val filteredBills by remember(searchQuery, dateRangeState.value, selectedType, selectedCategory) {
        derivedStateOf {
            val allBills = viewModel.getAllBills().value
            val (filterStart, filterEnd) = dateRangeState.value
            
            allBills.filter { bill ->
                val matchQuery = searchQuery.isBlank() ||
                    bill.categoryName.contains(searchQuery, ignoreCase = true) ||
                    bill.remark.contains(searchQuery, ignoreCase = true) ||
                    bill.account.contains(searchQuery, ignoreCase = true)
                
                val matchType = when (selectedType) {
                    "支出" -> bill.type == "expense"
                    "收入" -> bill.type == "income"
                    else -> true
                }
                
                val matchCategory = selectedCategory == null || bill.categoryName == selectedCategory
                
                val matchDate = if (filterStart != null && filterEnd != null) {
                    bill.dateTime >= filterStart && bill.dateTime <= filterEnd
                } else {
                    true
                }
                
                matchQuery && matchType && matchCategory && matchDate
            }.sortedByDescending { it.dateTime }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("搜索账单", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("搜索账单...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        onClick = { showFilters = !showFilters },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF5F5F5)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "筛选条件",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Icon(
                                imageVector = if (showFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (showFilters) "收起" else "展开",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    
                    if (showFilters) {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            dateRangeOptions.forEach { option ->
                                FilterChip(
                                    selected = selectedDateRange == option,
                                    onClick = {
                                        selectedDateRange = option
                                        if (option != "自定义") {
                                            startDate = null
                                            endDate = null
                                        }
                                    },
                                    label = { Text(option, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary.copy(alpha = 0.2f),
                                        selectedLabelColor = Primary
                                    )
                                )
                            }
                        }
                        
                        if (selectedDateRange == "自定义") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedCard(
                                    onClick = { showStartDatePicker = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = startDate?.let { formatDate(it) } ?: "开始日期",
                                            fontSize = 12.sp,
                                            color = if (startDate != null) TextPrimary else TextSecondary
                                        )
                                    }
                                }
                                
                                Text(
                                    text = "至",
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                
                                OutlinedCard(
                                    onClick = { showEndDatePicker = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = endDate?.let { formatDate(it) } ?: "结束日期",
                                            fontSize = 12.sp,
                                            color = if (endDate != null) TextPrimary else TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            typeOptions.forEach { option ->
                                FilterChip(
                                    selected = selectedType == option,
                                    onClick = { selectedType = option },
                                    label = { Text(option, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary.copy(alpha = 0.2f),
                                        selectedLabelColor = Primary
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("全部分类", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary.copy(alpha = 0.2f),
                                    selectedLabelColor = Primary
                                )
                            )
                            allCategories.forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category.name,
                                    onClick = { selectedCategory = if (selectedCategory == category.name) null else category.name },
                                    label = { Text(category.name, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary.copy(alpha = 0.2f),
                                        selectedLabelColor = Primary
                                    )
                                )
                            }
                        }
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (searchQuery.isBlank() && selectedDateRange == "全部" && selectedType == "全部" && selectedCategory == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "输入关键词或选择筛选条件",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else if (filteredBills.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = TextSecondary.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "没有找到相关账单",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredBills) { bill ->
                    BillItem(
                        bill = bill,
                        onClick = { onBillClick(bill) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(200.dp))
                }
            }
        }
    }
    
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismiss = { showStartDatePicker = false },
            onConfirm = { date ->
                startDate = date
                showStartDatePicker = false
            }
        )
    }
    
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismiss = { showEndDatePicker = false },
            onConfirm = { date ->
                endDate = date
                showEndDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { date ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = date
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        onConfirm(calendar.timeInMillis)
                    }
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun BillItem(
    bill: Bill,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (bill.type == "expense") CardYellow.copy(alpha = 0.2f) else CardCyan.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = IconUtils.getIconByName(bill.categoryIcon),
                        contentDescription = null,
                        tint = if (bill.type == "expense") ExpenseColor else IncomeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.categoryName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatDateTime(bill.dateTime),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                if (bill.remark.isNotBlank()) {
                    Text(
                        text = bill.remark,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
            
            Text(
                text = "${if (bill.type == "expense") "-" else "+"}¥${String.format("%.2f", bill.amount)}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (bill.type == "expense") ExpenseColor else IncomeColor
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
