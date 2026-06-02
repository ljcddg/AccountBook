package com.apesource.account.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apesource.account.data.entity.Bill
import com.apesource.account.data.entity.Book
import com.apesource.account.ui.components.CategorySelectDialog
import com.apesource.account.ui.theme.*
import com.apesource.account.ui.viewmodel.AccountViewModel
import com.apesource.account.ui.viewmodel.SimpleAccount
import com.apesource.account.utils.IconUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordDetailScreen(
    bill: Bill,
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel()
) {
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    val books by viewModel.books.collectAsState(initial = emptyList())
    val expenseCategories by viewModel.getExpenseCategories().collectAsState(initial = emptyList())
    val incomeCategories by viewModel.getIncomeCategories().collectAsState(initial = emptyList())

    var amountText by remember(bill.id) { mutableStateOf(String.format("%.2f", Math.abs(bill.amount))) }
    var billType by remember(bill.id) { mutableStateOf(bill.type) }
    var selectedCategory by remember(bill.id) { 
        mutableStateOf(
            if (bill.type == "expense") {
                expenseCategories.find { it.name == bill.categoryName }
            } else {
                incomeCategories.find { it.name == bill.categoryName }
            }
        )
    }
    var selectedAccount by remember(bill.id) { mutableStateOf(bill.account) }
    var selectedBookName by remember(bill.id) { mutableStateOf(bill.book) }
    var includeInBalance by remember(bill.id) { mutableStateOf(bill.includeInBalance) }
    var remark by remember(bill.id) { mutableStateOf(bill.remark) }
    var billDateTime by remember(bill.id) { mutableStateOf(bill.dateTime) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showBookDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val categories = if (billType == "expense") expenseCategories else incomeCategories

    LaunchedEffect(billType) {
        selectedCategory = categories.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录详情", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.deleteBill(bill); onBack() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5), contentColor = TextSecondary)
                ) {
                    Text("删除")
                }
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        val finalAmount = if (billType == "expense") -amount else amount
                        val category = selectedCategory ?: categories.firstOrNull()

                        if (selectedAccount != bill.account || billType != bill.type || Math.abs(finalAmount - bill.amount) > 0.01) {
                            viewModel.updateAccountBalanceForBillChange(
                                oldAccount = bill.account,
                                oldAmount = bill.amount,
                                newAccount = selectedAccount,
                                newAmount = finalAmount,
                                includeInBalance = includeInBalance
                            )
                        }

                        val updatedBill = bill.copy(
                            type = billType,
                            amount = finalAmount,
                            account = selectedAccount,
                            book = selectedBookName,
                            categoryName = category?.name ?: bill.categoryName,
                            categoryIcon = category?.icon ?: bill.categoryIcon,
                            includeInBalance = includeInBalance,
                            remark = remark,
                            dateTime = billDateTime
                        )
                        viewModel.updateBill(updatedBill)
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
                ) {
                    Text("保存")
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryDialog = true },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(96.dp),
                            shape = CircleShape,
                            color = if (billType == "expense") CardYellow else CardCyan
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = IconUtils.getIconByName(selectedCategory?.icon ?: bill.categoryIcon),
                                    contentDescription = selectedCategory?.name ?: bill.categoryName,
                                    tint = if (billType == "expense") ExpenseColor else IncomeColor,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedCategory?.name ?: bill.categoryName,
                            fontSize = 16.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "点击修改",
                            fontSize = 12.sp,
                            color = Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (billType == "expense") "-" else "+",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (billType == "expense") ExpenseColor else IncomeColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        BasicTextField(
                            value = amountText,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    amountText = newValue
                                }
                            },
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (billType == "expense") ExpenseColor else IncomeColor,
                                textAlign = TextAlign.Start
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            cursorBrush = SolidColor(if (billType == "expense") ExpenseColor else IncomeColor),
                            decorationBox = { innerTextField ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (amountText.isEmpty()) {
                                        Text(
                                            text = "0.00",
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (billType == "expense") ExpenseColor.copy(alpha = 0.5f) else IncomeColor.copy(alpha = 0.5f)
                                        )
                                    }
                                    innerTextField()
                                    Text(
                                        text = "¥",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (billType == "expense") ExpenseColor else IncomeColor,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { billType = "expense" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (billType == "expense") CardYellow else Color(0xFFF5F5F5),
                            border = if (billType == "expense") BorderStroke(2.dp, ExpenseColor) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (billType == "expense") ExpenseColor else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "支出",
                                    fontSize = 14.sp,
                                    fontWeight = if (billType == "expense") FontWeight.Bold else FontWeight.Normal,
                                    color = if (billType == "expense") ExpenseColor else TextSecondary
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { billType = "income" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (billType == "income") CardCyan else Color(0xFFF5F5F5),
                            border = if (billType == "income") BorderStroke(2.dp, IncomeColor) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = if (billType == "income") IncomeColor else TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "收入",
                                    fontSize = 14.sp,
                                    fontWeight = if (billType == "income") FontWeight.Bold else FontWeight.Normal,
                                    color = if (billType == "income") IncomeColor else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAccountDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("账户", fontSize = 14.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedAccount, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("日期", fontSize = 14.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        Text(dateFormat.format(Date(billDateTime)), fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showBookDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Book, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("所属账本", fontSize = 14.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedBookName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("计入收支", fontSize = 14.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (includeInBalance) "是" else "否",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (includeInBalance) IncomeColor else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = includeInBalance,
                            onCheckedChange = { includeInBalance = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IncomeColor,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.StickyNote2, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("备注", fontSize = 14.sp, color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    BasicTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = TextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        decorationBox = { innerTextField ->
                            Box {
                                if (remark.isEmpty()) {
                                    Text(
                                        text = "添加备注...",
                                        fontSize = 14.sp,
                                        color = TextSecondary.copy(alpha = 0.6f)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAccountDialog) {
        AccountSelectDialog(
            accounts = accounts,
            selectedAccount = selectedAccount,
            onAccountSelected = { account ->
                selectedAccount = account.name
                showAccountDialog = false
            },
            onDismiss = { showAccountDialog = false }
        )
    }

    if (showBookDialog) {
        BookSelectDialog(
            books = books,
            selectedBook = selectedBookName,
            onBookSelected = { book ->
                selectedBookName = book.name
                showBookDialog = false
            },
            onDismiss = { showBookDialog = false }
        )
    }

    if (showCategoryDialog) {
        CategorySelectDialog(
            type = billType,
            categories = categories,
            selectedCategory = selectedCategory,
            onDismiss = { showCategoryDialog = false },
            onConfirm = { category ->
                selectedCategory = category
                showCategoryDialog = false
            }
        )
    }

    if (showDatePicker) {
        BillDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { dateTime ->
                billDateTime = dateTime
                showDatePicker = false
            },
            initialDateTime = billDateTime
        )
    }
}

@Composable
fun AccountSelectDialog(
    accounts: List<SimpleAccount>,
    selectedAccount: String,
    onAccountSelected: (SimpleAccount) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择账户", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                accounts.forEach { account ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccountSelected(account) }
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedAccount == account.name) Primary.copy(alpha = 0.1f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = account.name,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedAccount == account.name) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedAccount == account.name) Primary else TextPrimary
                                )
                                Text(
                                    text = "余额: ¥${String.format("%.2f", account.initialBalance)}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            if (selectedAccount == account.name) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun BookSelectDialog(
    books: List<Book>,
    selectedBook: String,
    onBookSelected: (Book) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择账本", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                books.forEach { book ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBookSelected(book) }
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedBook == book.name) Primary.copy(alpha = 0.1f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = book.name,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedBook == book.name) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedBook == book.name) Primary else TextPrimary
                                )
                                Text(
                                    text = "周期起始日: 每月${book.startDay}日",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            if (selectedBook == book.name) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    initialDateTime: Long
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateTime)
    
    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { date ->
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = initialDateTime
                        val hour = calendar.get(Calendar.HOUR_OF_DAY)
                        val minute = calendar.get(Calendar.MINUTE)
                        
                        val newCalendar = Calendar.getInstance()
                        newCalendar.timeInMillis = date
                        newCalendar.set(Calendar.HOUR_OF_DAY, hour)
                        newCalendar.set(Calendar.MINUTE, minute)
                        newCalendar.set(Calendar.SECOND, 0)
                        newCalendar.set(Calendar.MILLISECOND, 0)
                        onConfirm(newCalendar.timeInMillis)
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
