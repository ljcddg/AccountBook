package com.apesource.account.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apesource.account.ui.theme.*
import com.apesource.account.ui.viewmodel.AccountViewModel
import com.apesource.account.ui.viewmodel.SimpleAccount
import com.apesource.account.utils.IconUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetScreen(
    viewModel: AccountViewModel = viewModel(),
    onBack: () -> Unit = {},
    onNavigateToAddAccount: () -> Unit = {},
    onEditAccount: (SimpleAccount) -> Unit = {}
) {
    val netAssets by viewModel.getNetAssets().collectAsState(initial = 0.0)
    val totalAssets by viewModel.getTotalAssets().collectAsState(initial = 0.0)
    val totalLiabilities by viewModel.getTotalLiabilities().collectAsState(initial = 0.0)
    val debtRatio by viewModel.getDebtRatio().collectAsState(initial = 0.0)
    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    val netAssetsTrend by viewModel.getNetAssetsTrend().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("资产", "物品")

    var selectedCardIndex by remember { mutableStateOf(0) }
    var accountToDelete by remember { mutableStateOf<SimpleAccount?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.padding(end = 60.dp),
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Primary,
                                height = 2.dp
                            )
                        },
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        fontSize = 18.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) Primary else TextSecondary
                                    )
                                }
                            )
                        }
                    }
                },
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
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            if (selectedCardIndex == 0) {
                                NetAssetsOverviewCardContent(
                                    netAssets = netAssets,
                                    assets = totalAssets,
                                    liabilities = totalLiabilities,
                                    debtRatio = debtRatio
                                )
                            } else {
                                NetAssetsTrendCardContent(trend = netAssetsTrend)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(2) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .padding(horizontal = 4.dp)
                                    .clip(CircleShape)
                                    .background(if (index == selectedCardIndex) Primary else Color(0xFFE0E0E0))
                                    .clickable { selectedCardIndex = index }
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "资产账户",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    OutlinedButton(
                        onClick = onNavigateToAddAccount,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("添加账户", fontSize = 12.sp)
                    }
                }
            }

            item {
                Text(
                    text = "资金账户(资产)",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            items(accounts.filter { it.type == "asset" }) { account ->
                AccountItem(
                    account = account,
                    onEdit = { onEditAccount(it) },
                    onDelete = { accountToDelete = it }
                )
            }

            item {
                Text(
                    text = "信用账户(负债)",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp)
                )
            }

            items(accounts.filter { it.type == "liability" }) { account ->
                AccountItem(
                    account = account,
                    onEdit = { onEditAccount(it) },
                    onDelete = { accountToDelete = it }
                )
            }
        }
    }

    if (accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text("确认删除", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除账户「${accountToDelete?.name}」吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        accountToDelete?.let { viewModel.deleteAccount(it) }
                        accountToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseColor)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun NetAssetsOverviewCardContent(
    netAssets: Double,
    assets: Double,
    liabilities: Double,
    debtRatio: Double
) {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "净资产",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }

        Text(
            text = "¥${String.format("%.2f", netAssets)}",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = IncomeColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "资产",
                    fontSize = 12.sp,
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "负债",
                    fontSize = 12.sp,
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

        Spacer(modifier = Modifier.height(16.dp))

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
            Text(
                text = "${String.format("%.2f", debtRatio)}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
fun NetAssetsTrendCardContent(trend: List<Pair<String, Double>>) {
    Column(
        modifier = Modifier.padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "净资产趋势",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Primary)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "净资产",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (trend.isNotEmpty()) {
            val maxValue = trend.maxOf { it.second }
            val minValue = trend.minOf { it.second }
            val range = maxValue - minValue

            Column(modifier = Modifier.fillMaxWidth()) {
                repeat(5) { index ->
                    val value = maxValue - (index * range / 4)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${value.toInt()}",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.width(40.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFF0F0F0))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trend.forEach { (month, value) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val heightPercent = if (range == 0.0) 50f else ((value - minValue) / range * 100).toFloat().coerceIn(10f, 100f)
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(100.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(heightPercent.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Primary.copy(alpha = 0.8f))
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = month.replace("月", ""),
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountItem(
    account: SimpleAccount,
    onEdit: (SimpleAccount) -> Unit,
    onDelete: (SimpleAccount) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (account.type == "asset") CardCyan else CardYellow
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = IconUtils.getIconByName(account.icon),
                        contentDescription = account.name,
                        tint = if (account.type == "asset") IncomeColor else ExpenseColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = account.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    if (account.accountNumber.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${account.accountNumber})",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                if (account.remark.isNotEmpty()) {
                    Text(
                        text = account.remark,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Text(
                text = if (account.type == "liability") "-¥${String.format("%.2f", Math.abs(account.initialBalance))}" else "¥${String.format("%.2f", account.initialBalance)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (account.type == "liability") ExpenseColor else IncomeColor
            )

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "更多操作",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    offset = DpOffset(0.dp, 0.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            showMenu = false
                            onEdit(account)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除", color = ExpenseColor) },
                        onClick = {
                            showMenu = false
                            onDelete(account)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = ExpenseColor, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }
        }
    }
}