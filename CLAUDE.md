# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

一款 Android 记账应用，包名 `com.apesource.account`。使用 Kotlin + Jetpack Compose (Material 3) 构建，采用 Gradle Kotlin DSL 构建系统，target SDK 36（Android 16）。目前数据全内存存储，无持久化数据库。

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
├── MainActivity.kt              # 入口 Activity + 导航外壳(AccountApp Composable)
├── data/entity/                 # 数据模型
│   ├── Bill.kt                  # 账单记录
│   ├── Book.kt                  # 账本
│   └── Category.kt              # 收支分类
├── navigation/Route.kt          # 路由密封类(未实际使用于导航，目前用字符串路由)
├── ui/
│   ├── theme/                   # Material 3 主题(Color/Theme/Type)
│   ├── components/              # 可复用组件
│   │   ├── AddBillDialog.kt     # 添加/编辑账单弹窗(含数字键盘)
│   │   └── CategorySelectDialog.kt  # 分类选择弹窗
│   ├── screens/                 # 各页面
│   │   ├── HomeScreen.kt        # 主页：月度概览卡片、资产卡片、今日账单列表
│   │   ├── StatisticsScreen.kt  # 统计页：周/月/年趋势图、饼图、排行
│   │   ├── AssetScreen.kt       # 资产页：净资产概览、账户列表
│   │   ├── BookEditScreen.kt    # 账本管理 + BookAddScreen(添加账本)
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

1. **无导航库**：屏幕切换通过 `MainActivity.kt` 中的 `currentScreen` 字符串状态 + `when` 分支实现，不使用 Navigation Compose 库。

2. **单一 ViewModel**：`AccountViewModel` 继承 `AndroidViewModel`，承载全部应用状态（账单列表、分类、账户、账本、预算、设置等）。所有状态均使用 `MutableStateFlow`，视图通过 `collectAsState()` 订阅。

3. **数据层**：当前为演示状态，`AccountViewModel` 文件底部有硬编码示例数据（`getSampleBills()`, `getDefaultCategories()`, `getDefaultAccounts()`）。数据操作方法是内存级别的增删改查，无 Room/SQLite。

4. **主题色**：橙色为主色(`#FF9800`)，绿色为收入/强调色(`#4CAF50`)，支持浅色/深色模式切换。

5. **计账周期**：支持自定义计账周期（起始日-结束日），默认自然月。`periodOffset` 控制历史月份/周/年的前后翻页。

### 关键技术依赖

- `androidx.compose.material3` — UI 框架
- `androidx.viewpager2` — 声明了依赖但尚未使用
- `gson` — JSON 序列化/反序列化（PreferencesHelper 中用于持久化账单）
- `androidx.lifecycle-viewmodel-compose` — ViewModel 与 Compose 集成

### 注意事项

- `IconUtils.getIconByName()` 中缺少 "message" 图标的处理（default accounts 中的微信使用了 "message"，会导致返回默认 Circle 图标）
- `getMonthNetAssets()` 返回硬编码的模拟数据，未与真实账户数据关联
- 部分功能如"记笔记"模式、"导入导出"、"自动记账"、"定时记账"仅有 UI 占位，功能未实现
