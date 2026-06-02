package com.apesource.account

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.*
import com.apesource.account.data.entity.Bill
import com.apesource.account.ui.screens.*
import com.apesource.account.ui.theme.AccountTheme
import com.apesource.account.ui.viewmodel.AccountViewModel
import com.apesource.account.ui.viewmodel.SimpleAccount
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccountTheme {
                AccountApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountApp(viewModel: AccountViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("home") }
    val screenWidth = LocalConfiguration.current.screenWidthDp
    var selectedBill by remember { mutableStateOf<Bill?>(null) }
    var editingAccount by remember { mutableStateOf<SimpleAccount?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width((screenWidth * 2 / 3).dp),
                drawerContainerColor = Color.White
            ) {
                DrawerContent(
                    onNavigate = { screen ->
                        currentScreen = screen
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        }
    ) {
        when (currentScreen) {
            "statistics" -> {
                StatisticsScreen(
                    onBack = { currentScreen = "home" },
                    viewModel = viewModel
                )
            }
            "budget" -> {
                BudgetScreen(
                    onBack = { currentScreen = "home" },
                    viewModel = viewModel
                )
            }
            "settings" -> {
                SettingsScreen(
                    onBack = { currentScreen = "home" },
                    viewModel = viewModel
                )
            }
            "search" -> {
                SearchScreen(
                    onBack = { currentScreen = "home" },
                    viewModel = viewModel,
                    onBillClick = { bill -> selectedBill = bill }
                )
            }
            "bookEdit" -> {
                BookEditScreen(
                    onBack = { currentScreen = "home" },
                    onAddBook = { currentScreen = "bookAdd" },
                    viewModel = viewModel
                )
            }
            "bookAdd" -> {
                BookAddScreen(
                    onBack = { currentScreen = "bookEdit" },
                    viewModel = viewModel
                )
            }
            "assets" -> {
                AssetScreen(
                    onBack = { currentScreen = "home" },
                    onNavigateToAddAccount = { currentScreen = "addAccount" },
                    onEditAccount = { account ->
                        editingAccount = account
                        currentScreen = "editAccount"
                    },
                    viewModel = viewModel
                )
            }
            "addAccount" -> {
                AddAccountScreen(
                    onBack = { currentScreen = "assets" },
                    viewModel = viewModel
                )
            }
            "editAccount" -> {
                AddAccountScreen(
                    onBack = {
                        editingAccount = null
                        currentScreen = "assets"
                    },
                    viewModel = viewModel,
                    editingAccount = editingAccount
                )
            }
            else -> {
                if (selectedBill != null) {
                    RecordDetailScreen(
                        bill = selectedBill!!,
                        onBack = { selectedBill = null }
                    )
                } else {
                    HomeScreen(
                        viewModel = viewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToStatistics = { currentScreen = "statistics" },
                        onNavigateToBudget = { currentScreen = "budget" },
                        onBillClick = { bill -> selectedBill = bill },
                        onNavigateToSearch = { currentScreen = "search" },
                        onNavigateToBookEdit = { currentScreen = "bookEdit" },
                        onNavigateToAssets = { currentScreen = "assets" }
                    )
                }
            }
        }
    }
}

@Composable
fun DrawerContent(onNavigate: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        DrawerHeader()
        CalendarSection()
        StatsSection()
        Divider()
        DrawerMenu(onNavigate = onNavigate)
    }
}

@Composable
fun DrawerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = MaterialTheme.shapes.medium,
            color = Color(0xFF00BCD4)
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "用户头像",
                tint = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "小黑黑#1e3c9",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DrawerMenu(onNavigate: (String) -> Unit) {
    val menuItems = listOf(
        "图表统计" to Icons.Default.BarChart,
        "我的物品" to Icons.Default.Inventory2,
        "导入导出" to Icons.Default.ImportExport,
        "自动记账" to Icons.Default.AutoFixHigh,
        "定时记账" to Icons.Default.Schedule,
        "我的账单" to Icons.Default.Receipt,
        "分类管理" to Icons.Default.Category,
        "预算设置" to Icons.Default.Wallet
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "常用功能",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            items(menuItems) { (label, icon) ->
                MenuItem(
                    label = label,
                    icon = icon,
                    onClick = {
                        when (label) {
                            "图表统计" -> onNavigate("statistics")
                            "预算设置" -> onNavigate("budget")
                            else -> {}
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider()

        listOf("社区公约", "意见反馈", "设置").forEach { label ->
            NavigationDrawerItem(
                label = { Text(label, fontSize = 12.sp) },
                selected = false,
                onClick = {
                    when (label) {
                        "设置" -> onNavigate("settings")
                        else -> {}
                    }
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun CalendarSection() {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) + 1
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
    
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                    text = "${year}/${month}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 10.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                var day = 1
                for (week in 0..5) {
                    if (day > daysInMonth) break
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (weekDay in 0..6) {
                            val isFirstWeek = week == 0
                            val isBeforeFirstDay = isFirstWeek && weekDay < firstDayOfMonth - 1
                            
                            if (isBeforeFirstDay || day > daysInMonth) {
                                Box(modifier = Modifier.size(24.dp))
                            } else {
                                val isToday = day == today
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isToday) Color(0xFFFF9800) else Color.Transparent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        fontSize = 12.sp,
                                        color = if (isToday) Color.White else Color(0xFF333333)
                                    )
                                }
                                day++
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(icon = Icons.Default.LocalFireDepartment, label = "坚持记录", value = "1天")
            StatItem(icon = Icons.Default.List, label = "总记录", value = "10条")
            StatItem(icon = Icons.Default.TrendingUp, label = "连续记录", value = "1天")
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF999999)
        )
    }
}

@Composable
fun MenuItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = Color(0xFF666666),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF666666),
                maxLines = 1
            )
        }
    }
}