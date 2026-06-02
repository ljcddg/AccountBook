package com.apesource.account.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.apesource.account.data.entity.Bill
import com.apesource.account.data.entity.Category
import com.apesource.account.ui.theme.*
import com.apesource.account.ui.viewmodel.SimpleAccount
import com.apesource.account.utils.IconUtils
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun AddBillDialog(
    expenseCategories: List<Category>,
    incomeCategories: List<Category>,
    accounts: List<SimpleAccount>,
    currentBookName: String,
    onDismiss: () -> Unit,
    onConfirm: (Bill) -> Unit,
    bill: Bill? = null
) {
    val isEditMode = bill != null
    
    var type by remember { mutableStateOf(bill?.type ?: "expense") }
    var selectedCategory by remember { 
        mutableStateOf<Category?>(
            bill?.let { 
                (if (it.type == "expense") expenseCategories else incomeCategories).find { cat -> cat.name == it.categoryName }
            } ?: null
        )
    }
    var selectedAccount by remember { 
        mutableStateOf(
            bill?.let { accounts.find { acc -> acc.name == it.account } } ?: accounts.firstOrNull()
        ) 
    }
    var amount by remember { 
        mutableStateOf(bill?.let { String.format("%.2f", Math.abs(it.amount)) } ?: "") 
    }
    var remark by remember { mutableStateOf(bill?.remark ?: "") }
    var selectedDate by remember {
        mutableStateOf(
            bill?.let {
                val cal = Calendar.getInstance()
                cal.timeInMillis = it.dateTime
                cal
            } ?: Calendar.getInstance()
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var includeInBalance by remember { mutableStateOf(bill?.includeInBalance ?: true) }
    var showAccountDialog by remember { mutableStateOf(false) }

    val categories = if (type == "expense") expenseCategories else incomeCategories

    LaunchedEffect(categories) {
        if (selectedCategory == null && categories.isNotEmpty()) {
            selectedCategory = categories.first()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TabRow(
                    selectedTabIndex = if (type == "expense") 0 else 1,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = type == "expense",
                        onClick = { type = "expense" }
                    ) {
                        Text(
                            "支出",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (type == "expense") ExpenseColor else TextSecondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    Tab(
                        selected = type == "income",
                        onClick = { type = "income" }
                    ) {
                        Text(
                            "收入",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (type == "income") IncomeColor else TextSecondary,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }

            Divider(color = Divider)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    items(categories) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = selectedCategory == category,
                            isExpense = type == "expense",
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                Divider(color = Divider)

                Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OptionButton(
                    icon = Icons.Default.AccountBalanceWallet,
                    text = selectedAccount?.name ?: "账户",
                    onClick = { showAccountDialog = true },
                    modifier = Modifier.weight(1f),
                    compact = true
                )
                OptionButton(
                    icon = Icons.Default.CalendarToday,
                    text = SimpleDateFormat("M/d", Locale.getDefault()).format(selectedDate.time),
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f),
                    compact = true
                )
                OptionButton(
                    icon = if (includeInBalance) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    text = if (includeInBalance) "计入" else "不计入",
                    onClick = { includeInBalance = !includeInBalance },
                    modifier = Modifier.weight(1f),
                    compact = true,
                    isHighlighted = !includeInBalance
                )
            }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¥",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (type == "expense") ExpenseColor else IncomeColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (amount.isEmpty()) "0.00" else amount,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (type == "expense") ExpenseColor else IncomeColor
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = remark,
                        onValueChange = { remark = it },
                        placeholder = { Text("点击填写备注信息", color = TextSecondary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        )
                    )
                    
                    IconButton(onClick = { /* TODO: 图片 */ }) {
                        Icon(Icons.Default.Image, contentDescription = "图片", tint = TextSecondary)
                    }
                    IconButton(onClick = { /* TODO: 拍照 */ }) {
                        Icon(Icons.Default.Camera, contentDescription = "拍照", tint = TextSecondary)
                    }
                }

                Divider(color = Divider)

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("1", "2", "3", "X").forEach { key ->
                            Box(modifier = Modifier.weight(1f)) {
                                NumericKey(key = key, onClick = { handleKeyPress(key, amount) { amount = it } })
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("4", "5", "6", "+").forEach { key ->
                            Box(modifier = Modifier.weight(1f)) {
                                NumericKey(key = key, onClick = { handleKeyPress(key, amount) { amount = it } })
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("7", "8", "9", "-").forEach { key ->
                            Box(modifier = Modifier.weight(1f)) {
                                NumericKey(key = key, onClick = { handleKeyPress(key, amount) { amount = it } })
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1f)) {
                            NumericKey(key = "再记", onClick = {
                                selectedCategory?.let { category ->
                                    selectedAccount?.let { account ->
                                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                                        val finalAmount = if (type == "expense") {
                                            -amountValue
                                        } else {
                                            amountValue
                                        }
                                        val newBill = Bill(
                                            id = bill?.id ?: 0,
                                            type = type,
                                            categoryName = category.name,
                                            categoryIcon = category.icon,
                                            amount = finalAmount,
                                            account = account.name,
                                            book = bill?.book ?: currentBookName,
                                            remark = remark,
                                            dateTime = selectedDate.timeInMillis,
                                            imagePath = bill?.imagePath,
                                            createTime = bill?.createTime ?: System.currentTimeMillis(),
                                            includeInBalance = includeInBalance
                                        )
                                        onConfirm(newBill)
                                    }
                                }
                                amount = ""
                                remark = ""
                            }, isAction = true)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            NumericKey(key = "0", onClick = { handleKeyPress("0", amount) { amount = it } })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            NumericKey(key = ".", onClick = { handleKeyPress(".", amount) { amount = it } })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            NumericKey(key = "完成", onClick = {
                                selectedCategory?.let { category ->
                                    selectedAccount?.let { account ->
                                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                                        val finalAmount = if (type == "expense") {
                                            -amountValue
                                        } else {
                                            amountValue
                                        }
                                        val newBill = Bill(
                                            id = bill?.id ?: 0,
                                            type = type,
                                            categoryName = category.name,
                                            categoryIcon = category.icon,
                                            amount = finalAmount,
                                            account = account.name,
                                            book = bill?.book ?: currentBookName,
                                            remark = remark,
                                            dateTime = selectedDate.timeInMillis,
                                            imagePath = bill?.imagePath,
                                            createTime = bill?.createTime ?: System.currentTimeMillis(),
                                            includeInBalance = includeInBalance
                                        )
                                        onConfirm(newBill)
                                    }
                                }
                            }, isAction = true, isPrimary = true)
                        }
                    }
                }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = millis
                            selectedDate = calendar
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            title = { Text("选择账户") },
            text = {
                LazyColumn {
                    items(accounts) { account ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAccount = account
                                    showAccountDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = IconUtils.getIconByName(account.icon),
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = account.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                if (!account.includeInTotal) {
                                    Text(
                                        text = "不计入总资产",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                            if (selectedAccount?.id == account.id) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "已选择",
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

fun handleKeyPress(key: String, currentAmount: String, onUpdate: (String) -> Unit) {
    when (key) {
        "X" -> {
            if (currentAmount.isNotEmpty()) {
                onUpdate(currentAmount.dropLast(1))
            }
        }
        "." -> {
            if (!currentAmount.contains(".") && currentAmount.isNotEmpty()) {
                onUpdate(currentAmount + ".")
            }
        }
        else -> {
            val maxLength = if (currentAmount.contains(".")) 11 else 9
            if (currentAmount.length < maxLength) {
                if (currentAmount == "0") {
                    onUpdate(key)
                } else {
                    onUpdate(currentAmount + key)
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    isExpense: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            if (isExpense) ExpenseColor.copy(alpha = 0.15f) else IncomeColor.copy(alpha = 0.15f)
                        } else {
                            Color(0xFFF0F0F0)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconUtils.getIconByName(category.icon),
                    contentDescription = category.name,
                    tint = if (isSelected) {
                        if (isExpense) ExpenseColor else IncomeColor
                    } else {
                        Color(0xFF999999)
                    },
                    modifier = Modifier.size(28.dp)
                )
            }
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (isExpense) ExpenseColor else IncomeColor)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "选中",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            fontSize = 12.sp,
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}

@Composable
fun OptionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    isHighlighted: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 12.dp else 20.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isHighlighted) ExpenseColor else TextPrimary
        ),
        contentPadding = if (compact) PaddingValues(horizontal = 8.dp, vertical = 4.dp) else PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(if (compact) 14.dp else 16.dp))
        Spacer(modifier = Modifier.width(if (compact) 2.dp else 4.dp))
        Text(text, fontSize = if (compact) 11.sp else 13.sp)
    }
}

@Composable
fun NumericKey(
    key: String,
    onClick: () -> Unit,
    isAction: Boolean = false,
    isPrimary: Boolean = false
) {
    val color = when {
        isPrimary -> Primary
        isAction -> Color(0xFF999999)
        else -> Color(0xFF333333)
    }
    val bgColor = if (isPrimary) Primary else Color.Transparent
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = color
        ),
        shape = RoundedCornerShape(0.dp),
        elevation = null
    ) {
        Text(
            key,
            fontSize = if (isAction) 14.sp else 24.sp,
            fontWeight = if (isAction) FontWeight.Normal else FontWeight.Bold,
            color = if (isPrimary) Color.White else color
        )
    }
}
