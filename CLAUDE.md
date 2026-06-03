# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

一款 Android 记账应用，包名 `com.apesource.account`。使用 Kotlin + Jetpack Compose (Material 3) 构建，采用 Gradle Kotlin DSL 构建系统，target SDK 36（Android 16）。数据持久化通过 SharedPreferences + Gson 序列化实现，无 Room/SQLite。

## 常用命令

```bash
# 构建 debug APK
./gradlew assembleDebug

# 运行单元测试
./gradlew test

# 运行 Android Instrumentation 测试
./gradlew connectedAndroidTest

# 运行单个测试类
./gradlew test --tests "com.apesource.account.ExampleUnitTest"

# 清理构建
./gradlew clean
```

## 架构概览

### 分层结构

```
app/src/main/java/com/apesource/account/
├── MainActivity.kt              # 入口 Activity + 导航外壳(AccountApp) + 抽屉菜单
├── data/entity/                 # 数据模型
│   ├── Bill.kt                  # 账单记录(id 使用 System.currentTimeMillis() 确保唯一)
│   ├── Book.kt                  # 账本
│   └── Category.kt              # 收支分类
├── navigation/Route.kt          # 路由密封类(未使用，目前用字符串路由)
├── ui/
│   ├── theme/                   # Material 3 主题(Color/Theme/Type)
│   ├── components/              # 可复用组件
│   │   ├── AddBillDialog.kt     # 添加/编辑账单弹窗(含数字键盘)
│   │   └── CategorySelectDialog.kt  # 分类选择弹窗
│   ├── screens/                 # 各页面
│   │   ├── HomeScreen.kt        # 主页：月度概览卡片、资产卡片、账单列表
│   │   ├── StatisticsScreen.kt  # 统计页：周/月/年趋势图、饼图、排行(含Y轴参考线)
│   │   ├── AssetScreen.kt       # 资产页：净资产概览、账户列表
│   │   ├── BookEditScreen.kt    # 账本管理
│   │   ├── BudgetScreen.kt      # 预算设置页
│   │   ├── SearchScreen.kt      # 搜索页
│   │   ├── SettingsScreen.kt    # 设置页
│   │   ├── AddAccountScreen.kt  # 添加资产账户页
│   │   └── RecordDetailScreen.kt # 账单详情页
│   └── viewmodel/
│       └── AccountViewModel.kt  # 全局 ViewModel(状态管理、业务逻辑)
└── utils/
    ├── IconUtils.kt             # 字符串名称 → Material Icon 映射
    └── PreferencesHelper.kt     # SharedPreferences 封装(Gson 序列化账单)
```

### 核心架构模式

1. **无导航库**：屏幕切换通过 `MainActivity.kt` 中的 `currentScreen` 字符串状态 + `when` 分支实现。子页面通过 `onBack` 回调返回上级页面。系统返回键通过 `BackHandler` 统一处理：非主页时返回主页，主页时退出应用。

2. **单一 ViewModel**：`AccountViewModel` 继承 `AndroidViewModel`，承载全部应用状态。所有状态均使用 `MutableStateFlow`。

3. **数据持久化**：账单通过 `PreferencesHelper` + Gson 序列化存储到 SharedPreferences。`AccountViewModel.init` 中有历史数据迁移逻辑，修复旧数据中 `id=0` 的账单。

4. **主题色**：橙色为主色(`#FF9800`)，绿色为收入/强调色(`#4CAF50`)，支持浅色/深色模式切换。

5. **计账周期**：支持自定义计账周期（起始日-结束日），默认自然月。`periodOffset` 控制历史月份/周/年的前后翻页。

### 导航层级

```
"home" (主页，返回键退出应用)
├── "statistics" → 返回 home
├── "budget" → 返回 home
├── "settings" → 返回 home
├── "search" → 返回 home
├── "bookEdit" → 返回 home
│   └── "bookAdd" → 返回 bookEdit
├── "assets" → 返回 home
│   └── "addAccount" / "editAccount" → 返回 assets
└── selectedBill != null (账单详情) → 清除 selectedBill 返回 home
```

### 流订阅模式

对于参数变化的 Flow（如 `getBillsByDate(start, end)`），使用 `LaunchedEffect(key)` + `mutableStateOf` 模式而非 `collectAsState()`，确保参数变化时正确切换到新 Flow：

```kotlin
var selectedDayBills by remember { mutableStateOf(emptyList<Bill>()) }
LaunchedEffect(dayStart, dayEnd) {
    viewModel.getBillsByDate(dayStart, dayEnd).collect { selectedDayBills = it }
}
```

### 账单 ID 唯一性保证

三层保护确保每笔账单 ID 唯一：
1. `AddBillDialog` 新建账单时用 `System.currentTimeMillis()` 生成 ID
2. `insertBill()` 兜底检查 `id == 0L` 时自动分配
3. `init {}` 启动时迁移历史数据中 `id=0` 的账单

### 关键技术依赖

- `androidx.compose.material3` — UI 框架
- `gson` — JSON 序列化/反序列化
- `androidx.lifecycle-viewmodel-compose` — ViewModel 与 Compose 集成
- `androidx.activity.compose.BackHandler` — 系统返回键拦截

### 注意事项

- `IconUtils.getIconByName()` 中缺少 "message" 图标的处理（default accounts 中的微信使用了 "message"，会导致返回默认 Circle 图标）
- `getMonthNetAssets()` 返回硬编码的模拟数据，未与真实账户数据关联
- 部分功能如"记笔记"模式、"导入导出"、"自动记账"、"定时记账"仅有 UI 占位，功能未实现
- `getMonthRange()` 的 `offset` 参数默认值为 `0`（非 `_periodOffset.value`），StatisticsScreen 中需显式传入 `periodOffset`
