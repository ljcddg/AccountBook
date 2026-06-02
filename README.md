# 闪记

一款基于 **Kotlin + Jetpack Compose (Material 3)** 构建的 Android 个人记账应用。

## 功能特性

- **收支记录管理** — 添加、编辑、删除账单，支持多账本
- **资产管理** — 净资产概览、资产/负债账户分类、卡号绑定
- **分类统计** — 饼图、趋势图、消费排行
- **预算规划** — 月度预算设置与剩余天数提醒
- **数据可视化** — 周/月/年趋势分析
- **深色模式** — 浅色/深色主题切换
- **搜索功能** — 按分类、金额、备注等维度搜索账单

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose + Material 3 |
| 架构 | 单 ViewModel (AndroidViewModel) |
| 状态管理 | MutableStateFlow + collectAsState |
| 数据持久化 | SharedPreferences + Gson |
| 构建系统 | Gradle Kotlin DSL |
| Target SDK | 36 |

## 项目结构

```
app/src/main/java/com/apesource/account/
├── MainActivity.kt              # 入口 Activity + 导航外壳
├── data/entity/                 # 数据模型 (Bill, Book, Category)
├── ui/
│   ├── theme/                   # Material 3 主题
│   ├── components/              # 可复用组件 (AddBillDialog, CategorySelectDialog)
│   ├── screens/                 # 页面 (Home, Statistics, Asset, Budget, Search 等)
│   └── viewmodel/               # 全局 ViewModel
└── utils/                       # 工具类 (IconUtils, PreferencesHelper)
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
