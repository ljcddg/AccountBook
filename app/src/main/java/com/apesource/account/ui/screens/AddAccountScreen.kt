package com.apesource.account.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apesource.account.ui.theme.*
import com.apesource.account.ui.viewmodel.AccountViewModel
import com.apesource.account.ui.viewmodel.SimpleAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel(),
    editingAccount: SimpleAccount? = null
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var selectedAccountType by remember { mutableStateOf<AccountType?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加账户", fontWeight = FontWeight.Bold) },
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
                AccountSection(
                    title = "资金账户(资产)",
                    accounts = listOf(
                        AccountItemData("储蓄卡", "CreditCard", Color(0xFF1989FA)),
                        AccountItemData("微信", "MessageCircle", Color(0xFF07C160)),
                        AccountItemData("支付宝", "Wallet", Color(0xFF1677FF)),
                        AccountItemData("现金", "Money", Color(0xFFFAAD14)),
                        AccountItemData("自定义", "Edit", Color(0xFF999999))
                    ),
                    onAccountClick = { type ->
                        selectedAccountType = type
                        showNameDialog = true
                    }
                )
            }
            
            item {
                AccountSection(
                    title = "信用账户(负债)",
                    accounts = listOf(
                        AccountItemData("信用卡", "CreditCard", Color(0xFFF5222D)),
                        AccountItemData("花呗", "Circle", Color(0xFF1677FF)),
                        AccountItemData("借呗", "Handshake", Color(0xFF52C41A)),
                        AccountItemData("京东白条", "ShoppingCart", Color(0xFFE53935)),
                        AccountItemData("自定义", "Edit", Color(0xFF999999))
                    ),
                    onAccountClick = { type ->
                        selectedAccountType = type
                        showNameDialog = true
                    }
                )
            }
            
            item {
                AccountSection(
                    title = "充值账户(资产)",
                    accounts = listOf(
                        AccountItemData("饭卡", "UtensilsCrossed", Color(0xFFFA8C16)),
                        AccountItemData("公交卡", "Bus", Color(0xFF722ED1)),
                        AccountItemData("会员卡", "Star", Color(0xFFFAAD14)),
                        AccountItemData("自定义", "Edit", Color(0xFF999999))
                    ),
                    onAccountClick = { type ->
                        selectedAccountType = type
                        showNameDialog = true
                    }
                )
            }
            
            item {
                AccountSection(
                    title = "理财账户(资产)",
                    accounts = listOf(
                        AccountItemData("股票", "TrendingUp", Color(0xFF52C41A)),
                        AccountItemData("基金", "BarChart", Color(0xFF1677FF)),
                        AccountItemData("余额宝", "PiggyBank", Color(0xFFFAAD14)),
                        AccountItemData("零钱通", "Wallet", Color(0xFF07C160)),
                        AccountItemData("定期存款", "Calendar", Color(0xFF1989FA)),
                        AccountItemData("自定义", "Edit", Color(0xFF999999))
                    ),
                    onAccountClick = { type ->
                        selectedAccountType = type
                        showNameDialog = true
                    }
                )
            }
            
            item {
                AccountSection(
                    title = "借贷账户",
                    accounts = listOf(
                        AccountItemData("借出", "ArrowUpRight", Color(0xFF52C41A)),
                        AccountItemData("借入", "ArrowDownLeft", Color(0xFFF5222D))
                    ),
                    onAccountClick = { type ->
                        selectedAccountType = type
                        showNameDialog = true
                    }
                )
            }
        }
    }
    
    if (showNameDialog && selectedAccountType != null) {
        AccountNameDialog(
            accountType = selectedAccountType!!,
            editingAccount = editingAccount,
            onDismiss = {
                showNameDialog = false
                selectedAccountType = null
            },
            onConfirm = { name, balance, accountNumber, remark ->
                if (editingAccount != null) {
                    val type = if (selectedAccountType!!.isLiability) "liability" else "asset"
                    viewModel.updateAccount(
                        editingAccount.copy(
                            name = name,
                            icon = selectedAccountType!!.icon,
                            initialBalance = balance,
                            type = type,
                            accountNumber = accountNumber,
                            remark = remark
                        )
                    )
                } else {
                    val type = if (selectedAccountType!!.isLiability) "liability" else "asset"
                    viewModel.addAccount(
                        SimpleAccount(
                            name = name,
                            icon = selectedAccountType!!.icon,
                            initialBalance = balance,
                            type = type,
                            accountNumber = accountNumber,
                            remark = remark
                        )
                    )
                }
                showNameDialog = false
                selectedAccountType = null
                onBack()
            }
        )
    }
}

data class AccountItemData(
    val name: String,
    val icon: String,
    val color: Color
)

data class AccountType(
    val name: String,
    val icon: String,
    val color: Color,
    val isLiability: Boolean
)

@Composable
fun AccountSection(
    title: String,
    accounts: List<AccountItemData>,
    onAccountClick: (AccountType) -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            accounts.forEach { account ->
                Column(
                    modifier = Modifier.clickable {
                            onAccountClick(
                                AccountType(
                                    name = account.name,
                                    icon = account.icon,
                                    color = account.color,
                                    isLiability = title.contains("负债") || account.name == "借入"
                                )
                            )
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = account.color.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getIconByName(account.icon),
                                contentDescription = null,
                                tint = account.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = account.name,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountNameDialog(
    accountType: AccountType,
    editingAccount: SimpleAccount? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String) -> Unit
) {
    var accountName by remember { mutableStateOf(editingAccount?.name ?: accountType.name) }
    var balance by remember { mutableStateOf(if (editingAccount != null) String.format("%.2f", Math.abs(editingAccount.initialBalance)) else "0.00") }
    var accountNumber by remember { mutableStateOf(editingAccount?.accountNumber ?: "") }
    var remark by remember { mutableStateOf(editingAccount?.remark ?: "") }
    val isEdit = editingAccount != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑账户" else "添加账户", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("账户名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = { Text("卡号/账号") },
                    placeholder = { Text("选填，如卡号后四位") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = balance,
                    onValueChange = { balance = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("余额") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("¥") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    placeholder = { Text("选填") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val balanceValue = try {
                        balance.toDouble()
                    } catch (e: NumberFormatException) {
                        0.0
                    }
                    onConfirm(accountName, balanceValue, accountNumber, remark)
                }
            ) {
                Text(if (isEdit) "保存" else "确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun getIconByName(name: String) = when (name) {
    "CreditCard" -> Icons.Default.CreditCard
    "MessageCircle" -> Icons.Default.Chat
    "Wallet" -> Icons.Default.Wallet
    "Money" -> Icons.Default.Money
    "Edit" -> Icons.Default.Edit
    "Circle" -> Icons.Default.Circle
    "Handshake" -> Icons.Default.Handshake
    "ShoppingCart" -> Icons.Default.ShoppingCart
    "UtensilsCrossed" -> Icons.Default.Restaurant
    "Bus" -> Icons.Default.DirectionsBus
    "Star" -> Icons.Default.Star
    "TrendingUp" -> Icons.Default.TrendingUp
    "BarChart" -> Icons.Default.BarChart
    "PiggyBank" -> Icons.Default.Savings
    "Calendar" -> Icons.Default.Event
    "ArrowUpRight" -> Icons.Default.ArrowForward
    "ArrowDownLeft" -> Icons.Default.ArrowBack
    else -> Icons.Default.AccountBalance
}
