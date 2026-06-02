package com.apesource.account.ui.screens

import androidx.compose.foundation.layout.*
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
import com.apesource.account.ui.theme.*
import com.apesource.account.ui.viewmodel.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel()
) {
    val budget by viewModel.monthlyBudget.collectAsState()
    var budgetInput by remember { mutableStateOf(String.format("%.2f", budget)) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val (monthStart, monthEnd) = viewModel.getMonthRange()
    val currentExpense by viewModel.getTotalExpense(monthStart, monthEnd).collectAsState(initial = 0.0)
    
    val used = Math.abs(currentExpense)
    val available = budget - used
    val progress = (used / budget * 100).coerceAtMost(100.0).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("本月预算", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showResetConfirmDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = ExpenseColor)
                    ) {
                        Text("重置", fontWeight = FontWeight.Medium)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = viewModel.getAccountingPeriodDisplayText(),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "支出总预算",
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "¥${String.format("%.2f", budget)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            IconButton(
                                onClick = {
                                    budgetInput = String.format("%.2f", budget)
                                    showEditDialog = true
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "编辑预算",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = progress / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = if (progress >= 100) ExpenseColor else Primary,
                        trackColor = Divider
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "已用",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "¥${String.format("%.2f", used)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseColor
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "可用",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "¥${String.format("%.2f", available)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "预算明细",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                TextButton(onClick = {}) {
                    Text("添加", color = Primary, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Inbox,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "尚未添加预算明细",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "快去添加吧",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("修改月预算") },
            text = {
                Column {
                    Text("请输入新的月预算金额")
                    Spacer(modifier = Modifier.height(16.dp))
                    TextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("预算金额") },
                        leadingIcon = { Text("¥") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newBudget = budgetInput.toDoubleOrNull() ?: budget
                        viewModel.setMonthlyBudget(newBudget)
                        showEditDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("确认重置") },
            text = {
                Text("确定要将月预算重置为默认值 ¥5000.00 吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setMonthlyBudget(5000.0)
                        budgetInput = "5000.00"
                        showResetConfirmDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}