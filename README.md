# 闪记

一款基于 **Kotlin + Jetpack Compose (Material 3)** 构建的 Android 个人记账应用，简洁高效，专注记账本身。

## 功能特性

### 记账管理
- **收支记录** — 快速记账，支持多账本、多账户
- **账单详情** — 点击账单查看详情，支持修改金额/分类/日期/备注
- **数字键盘** — 内置自定义数字键盘，金额输入方便快捷

### 数据统计
- **趋势图表** — 周/月/年支出/收入趋势柱状图，含 Y 轴参考刻度
- **分类饼图** — 支出/收入分类占比一目了然
- **消费排行** — 按金额排序的账单排行榜

### 资产管理
- **资产概览** — 净资产、总资产、总负债一览
- **账户管理** — 添加/编辑资产账户（现金、银行卡、支付宝、微信等）

### 预算与日历
- **预算设置** — 月度预算，剩余天数提醒
- **日历标记** — 抽屉日历自动标记有账单记录的日期（橙色高亮）
- **坚持记录** — 统计坚持天数、总笔数、连续记录天数

### 其他
- **搜索** — 按分类、金额、备注搜索账单
- **深色模式** — 浅色/深色主题切换
- **返回键导航** — 任意页面按返回键回到主页，主页再按退出

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material 3 |
| 架构 | 单 ViewModel (AndroidViewModel) |
| 状态管理 | MutableStateFlow |
| 数据持久化 | SharedPreferences + Gson |
| 系统返回键 | BackHandler |
| 构建系统 | Gradle Kotlin DSL |
| Target SDK | 36 (Android 16) |

## 架构设计

### 导航

不使用 Navigation Compose 库，通过 `currentScreen` 字符串状态 + `when` 分支实现页面切换。系统返回键由 `BackHandler` 统一拦截：

```
主页(home) ←── 统计 / 预算 / 设置 / 搜索 / 账本 / 资产
  ↑              ↑
  └── 账单详情 ──┘  (子页面: 添加账本→账本管理, 添加账户→资产)
```

### 状态订阅

对于参数变化的 Flow（如按日期范围过滤账单），使用 `LaunchedEffect(key)` + `mutableStateOf` 确保参数变化时正确切换数据源。

### 账单 ID 唯一性

三层防护确保每笔账单 ID 唯一：
1. 新建账单使用 `System.currentTimeMillis()` 生成 ID
2. `insertBill()` 兜底检查 `id == 0` 时自动分配
3. 启动时自动迁移历史数据中 `id=0` 的账单

## 项目结构

```
app/src/main/java/com/apesource/account/
├── MainActivity.kt              # 入口 Activity + 导航 + 抽屉菜单
├── data/entity/                 # 数据模型
│   ├── Bill.kt                  # 账单
│   ├── Book.kt                  # 账本
│   └── Category.kt              # 收支分类
├── ui/
│   ├── theme/                   # Material 3 主题 (Color/Theme/Type)
│   ├── components/              # 可复用组件
│   │   ├── AddBillDialog.kt     # 记账弹窗 (含数字键盘)
│   │   └── CategorySelectDialog.kt
│   ├── screens/                 # 页面
│   │   ├── HomeScreen.kt        # 主页
│   │   ├── StatisticsScreen.kt  # 统计
│   │   ├── AssetScreen.kt       # 资产
│   │   ├── BookEditScreen.kt    # 账本管理
│   │   ├── BudgetScreen.kt      # 预算
│   │   ├── SearchScreen.kt      # 搜索
│   │   ├── SettingsScreen.kt    # 设置
│   │   ├── AddAccountScreen.kt  # 添加账户
│   │   └── RecordDetailScreen.kt # 账单详情
│   └── viewmodel/
│       └── AccountViewModel.kt  # 全局 ViewModel
└── utils/
    ├── IconUtils.kt             # 图标映射
    └── PreferencesHelper.kt     # 本地持久化
```

## 构建与运行

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 运行单元测试
./gradlew test

# 清理构建
./gradlew clean
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 许可证

MIT License
