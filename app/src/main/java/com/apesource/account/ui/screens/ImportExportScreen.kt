package com.apesource.account.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.apesource.account.ui.theme.*
import com.apesource.account.ui.viewmodel.AccountViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    
    // 导出相关状态
    var selectedPreset by remember { mutableStateOf("本月") }
    var showCustomRange by remember { mutableStateOf(false) }
    var customStartDate by remember { mutableStateOf("") }
    var customEndDate by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var tempDatePickerValue by remember { mutableStateOf(Calendar.getInstance()) }

    // 日期选择器对话框
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempDatePickerValue.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        customStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    }
                    showStartDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tempDatePickerValue.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = millis
                        customEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                    }
                    showEndDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 文件导入启动器
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importBillsFromUri(context, it) { count ->
                // 导入结果已在 ViewModel 中处理
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入导出", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab 切换
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导出", fontSize = 14.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("导入", fontSize = 14.sp)
                        }
                    }
                )
            }

            when (selectedTab) {
                0 -> ExportTab(
                    viewModel = viewModel,
                    selectedPreset = selectedPreset,
                    onSelectPreset = { selectedPreset = it; showCustomRange = false },
                    showCustomRange = showCustomRange,
                    onToggleCustom = { showCustomRange = !showCustomRange },
                    customStartDate = customStartDate,
                    customEndDate = customEndDate,
                    onStartDateClick = { 
                        tempDatePickerValue = Calendar.getInstance()
                        showStartDatePicker = true 
                    },
                    onEndDateClick = { 
                        tempDatePickerValue = Calendar.getInstance()
                        showEndDatePicker = true 
                    }
                )
                1 -> ImportTab(
                    viewModel = viewModel,
                    onImportClick = { importLauncher.launch("application/json") }
                )
            }
        }
    }
}

@Composable
fun ExportTab(
    viewModel: AccountViewModel,
    selectedPreset: String,
    onSelectPreset: (String) -> Unit,
    showCustomRange: Boolean,
    onToggleCustom: () -> Unit,
    customStartDate: String,
    customEndDate: String,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit
) {
    val context = LocalContext.current
    val presetRanges = remember { viewModel.getPresetTimeRanges() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 预设时间范围
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "预设时间范围",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetRanges.forEach { (label, start, end) ->
                        val isSelected = selectedPreset == label && !showCustomRange
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectPreset(label) },
                            label = { 
                                Text(
                                    label, 
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary.copy(alpha = 0.15f),
                                selectedLabelColor = Primary
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 显示选中范围的账单数量
                val selectedRange = if (showCustomRange) null else presetRanges.find { it.first == selectedPreset }
                if (selectedRange != null) {
                    val count = remember(selectedPreset) {
                        viewModel.bills.value.count { it.dateTime in selectedRange.second..selectedRange.third }
                    }
                    Text(
                        text = "该范围内共 $count 条账单",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // 自定义时间范围
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
                        text = "自定义时间范围",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Switch(
                        checked = showCustomRange,
                        onCheckedChange = { onToggleCustom() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = Primary.copy(alpha = 0.3f))
                    )
                }

                if (showCustomRange) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customStartDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("开始日期", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(onClick = onStartDateClick) {
                                    Icon(Icons.Default.DateRange, contentDescription = "选择日期", modifier = Modifier.size(20.dp))
                                }
                            },
                            textStyle = TextStyle(fontSize = 14.sp),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Text(text = "至", color = TextSecondary, fontSize = 14.sp)
                        
                        OutlinedTextField(
                            value = customEndDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("结束日期", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(onClick = onEndDateClick) {
                                    Icon(Icons.Default.DateRange, contentDescription = "选择日期", modifier = Modifier.size(20.dp))
                                }
                            },
                            textStyle = TextStyle(fontSize = 14.sp),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    if (customStartDate.isNotEmpty() && customEndDate.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val count = remember(customStartDate, customEndDate) {
                            try {
                                val startMillis = sdf.parse(customStartDate)?.time ?: 0L
                                val endCal = Calendar.getInstance()
                                endCal.time = sdf.parse(customEndDate) ?: Date()
                                endCal.set(Calendar.HOUR_OF_DAY, 23)
                                endCal.set(Calendar.MINUTE, 59)
                                endCal.set(Calendar.SECOND, 59)
                                endCal.set(Calendar.MILLISECOND, 999)
                                val endMillis = endCal.timeInMillis
                                viewModel.bills.value.count { it.dateTime in startMillis..endMillis }
                            } catch (e: Exception) {
                                0
                            }
                        }
                        Text(
                            text = "该范围内共 $count 条账单",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 导出按钮
        Button(
            onClick = {
                val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                val fileName = "account_export_${sdf.format(Date())}.json"
                
                if (showCustomRange && customStartDate.isNotEmpty() && customEndDate.isNotEmpty()) {
                    val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    try {
                        val startMillis = dateSdf.parse(customStartDate)?.time ?: 0L
                        val endCal = Calendar.getInstance()
                        endCal.time = dateSdf.parse(customEndDate) ?: Date()
                        endCal.set(Calendar.HOUR_OF_DAY, 23)
                        endCal.set(Calendar.MINUTE, 59)
                        endCal.set(Calendar.SECOND, 59)
                        endCal.set(Calendar.MILLISECOND, 999)
                        val endMillis = endCal.timeInMillis
                        viewModel.exportBillsToFile(context, startMillis, endMillis, fileName)
                    } catch (_: Exception) {}
                } else {
                    val presetRanges = viewModel.getPresetTimeRanges()
                    val range = presetRanges.find { it.first == selectedPreset }
                    if (range != null) {
                        viewModel.exportBillsToFile(context, range.second, range.third, fileName)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("导出账单", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ImportTab(
    viewModel: AccountViewModel,
    onImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 导入说明
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "从文件导入账单",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "支持导入本应用导出的 JSON 文件",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }

        // 导入注意事项
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "注意事项",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "- 仅支持 JSON 格式文件", fontSize = 13.sp, color = TextSecondary)
                Text(text = "- 重复的账单 ID 将被自动跳过", fontSize = 13.sp, color = TextSecondary)
                Text(text = "- 导入账单将合并到当前数据中", fontSize = 13.sp, color = TextSecondary)
                Text(text = "- 建议导入前先导出备份数据", fontSize = 13.sp, color = TextSecondary)
            }
        }

        // 导入按钮
        Button(
            onClick = onImportClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("选择文件导入", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
