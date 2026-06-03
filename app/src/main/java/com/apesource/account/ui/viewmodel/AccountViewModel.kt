package com.apesource.account.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.apesource.account.data.entity.Bill
import com.apesource.account.data.entity.Book
import com.apesource.account.data.entity.Category
import com.apesource.account.utils.PreferencesHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

data class SimpleAccount(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val initialBalance: Double,
    val type: String,
    val accountNumber: String = "",
    val remark: String = "",
    val includeInTotal: Boolean = true,
    val isDefault: Boolean = false
)

data class AccountingPeriod(
    val startDay: Int = 1,
    val endDay: Int = -1
)

data class AppSettings(
    val accountingPeriod: AccountingPeriod = AccountingPeriod(),
    val isGlobalShared: Boolean = false
)

class AccountViewModel(application: Application) : AndroidViewModel(application) {
    
    private val preferencesHelper = PreferencesHelper(application)
    
    private val savedBills = preferencesHelper.getBills()
    private val _bills = MutableStateFlow<List<Bill>>(if (savedBills.isNotEmpty()) savedBills else getSampleBills())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val savedCategories = preferencesHelper.getCategories()
    private val _categories = MutableStateFlow<List<Category>>(if (savedCategories.isNotEmpty()) savedCategories else getDefaultCategories())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val savedAccounts = preferencesHelper.getAccounts()
    private val _accounts = MutableStateFlow<List<SimpleAccount>>(if (savedAccounts.isNotEmpty()) savedAccounts else getDefaultAccounts())
    val accounts: StateFlow<List<SimpleAccount>> = _accounts.asStateFlow()
    
    init {
        if (savedCategories.isEmpty()) {
            preferencesHelper.saveCategories(getDefaultCategories())
        }
        if (savedAccounts.isEmpty()) {
            preferencesHelper.saveAccounts(getDefaultAccounts())
        }
        // 修复历史数据中 id=0 的账单，为它们分配唯一 ID
        val billsNeedFix = _bills.value.any { it.id == 0L }
        if (billsNeedFix) {
            _bills.value = _bills.value.map { bill ->
                if (bill.id == 0L) bill.copy(id = System.currentTimeMillis() + bill.hashCode())
                else bill
            }
            preferencesHelper.saveBills(_bills.value)
        }
    }

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _showAddBillDialog = MutableStateFlow(false)
    val showAddBillDialog: StateFlow<Boolean> = _showAddBillDialog.asStateFlow()

    private val savedBooks = preferencesHelper.getBooks()
    private val initialBooks = if (savedBooks.isNotEmpty()) savedBooks else getDefaultBooks()
    
    private val _books = MutableStateFlow<List<Book>>(initialBooks)
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _currentBook = MutableStateFlow<Book>(initialBooks.first())
    val currentBook: StateFlow<Book> = _currentBook.asStateFlow()

    private val _showAddBookDialog = MutableStateFlow(false)
    val showAddBookDialog: StateFlow<Boolean> = _showAddBookDialog.asStateFlow()

    private val savedAppSettings = preferencesHelper.getAppSettings()
    private val _appSettings = MutableStateFlow(savedAppSettings ?: AppSettings())
    val appSettings: StateFlow<AppSettings> = _appSettings.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(preferencesHelper.getMonthlyBudget())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _periodOffset = MutableStateFlow(preferencesHelper.getPeriodOffset())
    val periodOffset: StateFlow<Int> = _periodOffset.asStateFlow()
    
    private val _selectedDayOffset = MutableStateFlow(0)
    val selectedDayOffset: StateFlow<Int> = _selectedDayOffset.asStateFlow()
    
    private var lastBudgetResetDate: Long = 0L

    fun setMonthlyBudget(budget: Double) {
        _monthlyBudget.value = budget
        preferencesHelper.saveMonthlyBudget(budget)
    }
    
    fun isCurrentDateInPeriod(): Boolean {
        val period = _appSettings.value.accountingPeriod
        val startDay = period.startDay
        
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_MONTH)
        
        return today >= startDay
    }
    
    fun checkAndResetBudgetIfNewPeriod() {
        val period = _appSettings.value.accountingPeriod
        val startDay = period.startDay
        
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis
        
        if (lastBudgetResetDate == 0L) {
            lastBudgetResetDate = todayStart
            return
        }
        
        val lastResetCal = Calendar.getInstance()
        lastResetCal.timeInMillis = lastBudgetResetDate
        
        if (lastResetCal.get(Calendar.MONTH) != cal.get(Calendar.MONTH) ||
            lastResetCal.get(Calendar.YEAR) != cal.get(Calendar.YEAR)) {
            
            val lastDayOfLastMonth = Calendar.getInstance()
            lastDayOfLastMonth.add(Calendar.MONTH, -1)
            lastDayOfLastMonth.set(Calendar.DAY_OF_MONTH, lastDayOfLastMonth.getActualMaximum(Calendar.DAY_OF_MONTH))
            
            if (startDay <= lastDayOfLastMonth.get(Calendar.DAY_OF_MONTH)) {
                if (cal.get(Calendar.DAY_OF_MONTH) == startDay) {
                    _monthlyBudget.value = 3000.0
                    lastBudgetResetDate = todayStart
                }
            } else {
                if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
                    _monthlyBudget.value = 3000.0
                    lastBudgetResetDate = todayStart
                }
            }
        }
    }

    fun getDaysInMonth(calendar: Calendar = Calendar.getInstance()): Int {
        val startDay = _currentBook.value.startDay
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        
        if (today >= startDay) {
            val currentMonthMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val effectiveStartDay = minOf(startDay, currentMonthMaxDay)
            
            val nextMonth = calendar.clone() as Calendar
            nextMonth.add(Calendar.MONTH, 1)
            val nextMonthMaxDay = nextMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val endDay = minOf(startDay - 1, nextMonthMaxDay)
            if (endDay <= 0) return currentMonthMaxDay
            return (currentMonthMaxDay - effectiveStartDay + 1) + endDay
        } else {
            val lastMonth = calendar.clone() as Calendar
            lastMonth.add(Calendar.MONTH, -1)
            val lastMonthMaxDay = lastMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val effectiveStartDay = minOf(startDay, lastMonthMaxDay)
            
            val currentMonthMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val endDay = minOf(startDay - 1, currentMonthMaxDay)
            if (endDay <= 0) return lastMonthMaxDay
            return (lastMonthMaxDay - effectiveStartDay + 1) + endDay
        }
    }

    fun getRemainingDays(calendar: Calendar = Calendar.getInstance()): Int {
        val (_, monthEnd) = getMonthRange(calendar)
        val todayStart = calendar.clone() as Calendar
        todayStart.set(Calendar.HOUR_OF_DAY, 0)
        todayStart.set(Calendar.MINUTE, 0)
        todayStart.set(Calendar.SECOND, 0)
        todayStart.set(Calendar.MILLISECOND, 0)
        
        if (todayStart.timeInMillis > monthEnd) return 0
        val diffMillis = monthEnd - todayStart.timeInMillis
        return (diffMillis / (1000 * 60 * 60 * 24)).toInt() + 1
    }

    fun getDailyBudget(): Double {
        val daysInMonth = getDaysInMonth()
        return if (daysInMonth > 0) _monthlyBudget.value / daysInMonth else 0.0
    }

    fun updateAccountingPeriod(period: AccountingPeriod) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(accountingPeriod = period)
        preferencesHelper.saveAppSettings(_appSettings.value)
    }

    fun updateGlobalShared(isGlobal: Boolean) {
        val currentSettings = _appSettings.value
        _appSettings.value = currentSettings.copy(isGlobalShared = isGlobal)
        preferencesHelper.saveAppSettings(_appSettings.value)
    }

    fun setPeriodOffset(offset: Int) {
        _periodOffset.value = offset
        preferencesHelper.savePeriodOffset(offset)
    }

    fun nextPeriod() {
        if (_periodOffset.value < 0) {
            _periodOffset.value++
            preferencesHelper.savePeriodOffset(_periodOffset.value)
        }
    }

    fun prevPeriod() {
        _periodOffset.value--
        preferencesHelper.savePeriodOffset(_periodOffset.value)
    }

    fun resetPeriod() {
        _periodOffset.value = 0
        _selectedDayOffset.value = 0
        preferencesHelper.savePeriodOffset(0)
    }
    
    fun nextDay() {
        if (_selectedDayOffset.value < 0) {
            _selectedDayOffset.value++
        }
    }
    
    fun prevDay() {
        _selectedDayOffset.value--
    }
    
    fun resetDay() {
        _selectedDayOffset.value = 0
    }
    
    fun getSelectedDayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, _selectedDayOffset.value)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return start to end
    }
    
    fun getSelectedDayDisplayText(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, _selectedDayOffset.value)
        return when (_selectedDayOffset.value) {
            0 -> "今天"
            -1 -> "昨天"
            -2 -> "前天"
            else -> SimpleDateFormat("M月d日", Locale.getDefault()).format(Date(cal.timeInMillis))
        }
    }
    
    fun getSelectedDayFullText(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, _selectedDayOffset.value)
        val dateFormat = SimpleDateFormat("M月d日 EEEE", Locale.getDefault())
        return getSelectedDayDisplayText() + " " + dateFormat.format(Date(cal.timeInMillis))
    }

    fun isCurrentPeriod(): Boolean {
        return _periodOffset.value == 0
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun showAddBillDialog(show: Boolean) {
        _showAddBillDialog.value = show
    }

    fun showAddBookDialog(show: Boolean) {
        _showAddBookDialog.value = show
    }

    fun selectBook(book: Book) = viewModelScope.launch {
        _currentBook.value = book.copy(isDefault = true)
        val updatedBooks = _books.value.map { 
            if (it.id == book.id) it.copy(isDefault = true) 
            else it.copy(isDefault = false) 
        }
        _books.value = updatedBooks
        preferencesHelper.saveBooks(updatedBooks)
        refreshBooks()
    }

    fun updateBook(updatedBook: Book) = viewModelScope.launch {
        if (updatedBook.isDefault) {
            _books.value = _books.value.map { 
                if (it.id == updatedBook.id) updatedBook 
                else it.copy(isDefault = false) 
            }
            _currentBook.value = updatedBook
        } else {
            _books.value = _books.value.map { if (it.id == updatedBook.id) updatedBook else it }
        }
        preferencesHelper.saveBooks(_books.value)
        refreshBooks()
    }

    fun deleteBook(book: Book) = viewModelScope.launch {
        _books.value = _books.value.filter { it.id != book.id }
        if (_currentBook.value.id == book.id) {
            _currentBook.value = _books.value.firstOrNull() ?: getDefaultBooks().first()
        }
        preferencesHelper.saveBooks(_books.value)
        refreshBooks()
    }

    fun addBook(book: Book) = viewModelScope.launch {
        val newBook = book.copy(id = System.currentTimeMillis(), isDefault = book.isDefault)
        if (book.isDefault) {
            _books.value = _books.value.map { it.copy(isDefault = false) } + newBook
            _currentBook.value = newBook
        } else {
            _books.value = _books.value + newBook
        }
        preferencesHelper.saveBooks(_books.value)
        refreshBooks()
    }
    
    fun refreshBooks() {
        val savedBooks = preferencesHelper.getBooks()
        if (savedBooks.isNotEmpty()) {
            _books.value = savedBooks
            val currentBookId = _currentBook.value.id
            _currentBook.value = savedBooks.find { it.id == currentBookId } ?: savedBooks.first()
        }
    }

    fun getDefaultBooks(): List<Book> {
        return listOf(
            Book(
                id = 1,
                name = "系统账本",
                cover = "",
                startDay = 1,
                isDefault = true
            )
        )
    }

    fun getExpenseCategories(): Flow<List<Category>> =
        _categories.map { it.filter { cat -> cat.type == "expense" } }

    fun getIncomeCategories(): Flow<List<Category>> =
        _categories.map { it.filter { cat -> cat.type == "income" } }

    fun getAllBills(): StateFlow<List<Bill>> = bills

    fun getAssetAccounts(): Flow<List<SimpleAccount>> =
        _accounts.map { it.filter { acc -> acc.type == "asset" } }

    fun getLiabilityAccounts(): Flow<List<SimpleAccount>> =
        _accounts.map { it.filter { acc -> acc.type == "liability" } }

    fun getBillsByDateRange(startTime: Long, endTime: Long): Flow<List<Bill>> =
        combine(_bills, _currentBook) { bills, book ->
            bills.filter { bill -> 
                bill.book == book.name && 
                bill.dateTime in startTime..endTime 
            }
        }

    fun getBillsByDate(startOfDay: Long, endOfDay: Long): Flow<List<Bill>> =
        combine(_bills, _currentBook) { bills, book ->
            bills.filter { bill -> 
                bill.book == book.name && 
                bill.dateTime in startOfDay..endOfDay 
            }
        }

    fun getTotalExpense(startTime: Long, endTime: Long): Flow<Double> =
        combine(_bills, _currentBook) { bills, book ->
            val accountsMap = _accounts.value.associateBy { it.name }
            bills.filter { bill ->
                bill.book == book.name &&
                bill.type == "expense" &&
                bill.dateTime in startTime..endTime &&
                bill.includeInBalance &&
                (accountsMap[bill.account]?.includeInTotal ?: true)
            }.sumOf { bill -> bill.amount }
        }

    fun getTotalIncome(startTime: Long, endTime: Long): Flow<Double> =
        combine(_bills, _currentBook) { bills, book ->
            val accountsMap = _accounts.value.associateBy { it.name }
            bills.filter { bill ->
                bill.book == book.name &&
                bill.type == "income" &&
                bill.dateTime in startTime..endTime &&
                bill.includeInBalance &&
                (accountsMap[bill.account]?.includeInTotal ?: true)
            }.sumOf { bill -> bill.amount }
        }

    fun insertBill(bill: Bill) = viewModelScope.launch {
        val billWithId = if (bill.id == 0L) bill.copy(id = System.currentTimeMillis()) else bill
        _bills.value = _bills.value + billWithId
        preferencesHelper.saveBills(_bills.value)
    }

    fun deleteBill(bill: Bill) = viewModelScope.launch {
        _bills.value = _bills.value.filter { it.id != bill.id }
        preferencesHelper.saveBills(_bills.value)
    }

    fun updateBill(updatedBill: Bill) = viewModelScope.launch {
        _bills.value = _bills.value.map { 
            if (it.id == updatedBill.id) updatedBill else it 
        }
        preferencesHelper.saveBills(_bills.value)
    }

    fun searchBills(
        keyword: String = "",
        categoryName: String = "",
        startTime: Long? = null,
        endTime: Long? = null
    ): Flow<List<Bill>> {
        return combine(_bills, _currentBook) { bills, book ->
            bills.filter { bill ->
                val matchBook = bill.book == book.name
                val matchKeyword = if (keyword.isBlank()) true else {
                    bill.categoryName.contains(keyword, ignoreCase = true) ||
                    bill.remark.contains(keyword, ignoreCase = true) ||
                    bill.amount.toString().contains(keyword) ||
                    bill.account.contains(keyword, ignoreCase = true)
                }
                
                val matchCategory = if (categoryName.isBlank()) true else {
                    bill.categoryName.contains(categoryName, ignoreCase = true)
                }
                
                val matchTime = when {
                    startTime != null && endTime != null -> bill.dateTime in startTime..endTime
                    startTime != null -> bill.dateTime >= startTime
                    endTime != null -> bill.dateTime <= endTime
                    else -> true
                }
                
                matchBook && matchKeyword && matchCategory && matchTime
            }.sortedByDescending { it.dateTime }
        }
    }

    fun getMonthRange(calendar: Calendar = Calendar.getInstance(), offset: Int = 0): Pair<Long, Long> {
        val startDay = _currentBook.value.startDay
        
        val cal = calendar.clone() as Calendar
        val today = cal.get(Calendar.DAY_OF_MONTH)
        
        if (offset == 0 && today < startDay) {
            cal.add(Calendar.MONTH, -1)
        } else {
            cal.add(Calendar.MONTH, offset)
        }
        
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val effectiveStartDay = minOf(startDay, maxDay)
        cal.set(Calendar.DAY_OF_MONTH, effectiveStartDay)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        val startMonth = cal.get(Calendar.MONTH)
        val startYear = cal.get(Calendar.YEAR)
        
        val endMonth = if (startMonth == Calendar.DECEMBER) Calendar.JANUARY else startMonth + 1
        val endYear = if (startMonth == Calendar.DECEMBER) startYear + 1 else startYear
        
        cal.set(Calendar.YEAR, endYear)
        cal.set(Calendar.MONTH, endMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        if (startDay <= 1) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        } else {
            val endMaxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            cal.set(Calendar.DAY_OF_MONTH, minOf(startDay - 1, endMaxDay))
        }
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return start to end
    }

    fun getPeriodDisplayText(): String {
        val offset = _periodOffset.value
        val startDay = _currentBook.value.startDay
        
        val cal = Calendar.getInstance()
        val today = cal.get(Calendar.DAY_OF_MONTH)
        
        if (offset == 0 && today < startDay) {
            cal.add(Calendar.MONTH, -1)
        } else {
            cal.add(Calendar.MONTH, offset)
        }
        
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        
        val endMonth = if (cal.get(Calendar.MONTH) == Calendar.DECEMBER) Calendar.JANUARY else cal.get(Calendar.MONTH) + 1
        val endYear = if (cal.get(Calendar.MONTH) == Calendar.DECEMBER) cal.get(Calendar.YEAR) + 1 else cal.get(Calendar.YEAR)
        
        cal.set(Calendar.YEAR, endYear)
        cal.set(Calendar.MONTH, endMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val endDay: Int
        val displayEndYear: Int
        val displayEndMonth: Int
        if (startDay <= 1) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
            endDay = cal.get(Calendar.DAY_OF_MONTH)
            displayEndYear = cal.get(Calendar.YEAR)
            displayEndMonth = cal.get(Calendar.MONTH) + 1
        } else {
            val endMaxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            endDay = minOf(startDay - 1, endMaxDay)
            displayEndYear = endYear
            displayEndMonth = endMonth + 1
        }
        
        val dateRange = if (year == displayEndYear) {
            if (month == displayEndMonth) {
                "${year}.${month}.${startDay}-${endDay}"
            } else {
                "${year}.${month}.${startDay}-${displayEndMonth}.${endDay}"
            }
        } else {
            "${year}.${month}.${startDay}-${displayEndYear}.${displayEndMonth}.${endDay}"
        }
        
        if (offset == 0) {
            return "$dateRange (当前)"
        } else if (offset == -1) {
            return "$dateRange (上周期)"
        } else if (offset == -2) {
            return "$dateRange (上两周期)"
        } else {
            return dateRange
        }
    }

    fun getPeriodMonthName(): String {
        val offset = _periodOffset.value
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset)
        
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        
        return "${year}年${month}月"
    }

    fun getAccountingPeriod(): AccountingPeriod {
        return _appSettings.value.accountingPeriod
    }

    fun getAccountingPeriodDisplayText(): String {
        val period = _appSettings.value.accountingPeriod
        val startDay = period.startDay
        val endDay = if (period.endDay == -1) "月末" else period.endDay.toString()
        
        if (startDay <= (if (period.endDay == -1) 31 else period.endDay)) {
            return "${startDay}日-${endDay}"
        } else {
            return "上月${startDay}日-本月${endDay}"
        }
    }

    fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return start to end
    }

    fun getCategoryStatistics(startTime: Long, endTime: Long, type: String): Flow<Map<String, Double>> =
        combine(_bills, _currentBook) { bills, book ->
            bills.filter { bill -> bill.book == book.name && bill.type == type && bill.dateTime in startTime..endTime }
                .groupBy { it.categoryName }
                .mapValues { (_, bills) -> bills.sumOf { it.amount } }
        }

    fun getDailyTrend(startTime: Long, endTime: Long, type: String): Flow<List<Pair<Long, Double>>> =
        combine(_bills, _currentBook) { bills, book ->
            bills.filter { bill -> bill.book == book.name && bill.type == type && bill.dateTime in startTime..endTime }
                .groupBy { getStartOfDay(it.dateTime) }
                .map { (day, bills) -> day to bills.sumOf { it.amount } }
                .sortedBy { it.first }
        }

    fun getBillRanking(startTime: Long, endTime: Long, type: String): Flow<List<Bill>> =
        combine(_bills, _currentBook) { bills, book ->
            bills.filter { bill -> bill.book == book.name && bill.type == type && bill.dateTime in startTime..endTime }
                .sortedByDescending { it.amount }
                .take(10)
        }

    fun getTotalAssets(): Flow<Double> =
        _accounts.map { accounts ->
            accounts.filter { it.type == "asset" }.sumOf { it.initialBalance }
        }

    fun getTotalLiabilities(): Flow<Double> =
        _accounts.map { accounts ->
            Math.abs(accounts.filter { it.type == "liability" }.sumOf { it.initialBalance })
        }

    fun getNetAssets(): Flow<Double> =
        combine(getTotalAssets(), getTotalLiabilities()) { assets, liabilities ->
            assets - liabilities
        }

    fun getDebtRatio(): Flow<Double> =
        combine(getTotalAssets(), getTotalLiabilities()) { assets, liabilities ->
            if (assets == 0.0) 0.0 else (liabilities / assets * 100)
        }

    fun addAccount(account: SimpleAccount) = viewModelScope.launch {
        val newAccount = account.copy(id = System.currentTimeMillis())
        if (account.isDefault) {
            _accounts.value = _accounts.value.map { it.copy(isDefault = false) } + newAccount
        } else {
            _accounts.value = _accounts.value + newAccount
        }
        preferencesHelper.saveAccounts(_accounts.value)
    }

    fun updateAccount(updatedAccount: SimpleAccount) = viewModelScope.launch {
        _accounts.value = _accounts.value.map {
            if (it.id == updatedAccount.id) updatedAccount else it
        }
        preferencesHelper.saveAccounts(_accounts.value)
    }

    fun deleteAccount(account: SimpleAccount) = viewModelScope.launch {
        _accounts.value = _accounts.value.filter { it.id != account.id }
        preferencesHelper.saveAccounts(_accounts.value)
    }

    fun updateAccountBalanceForBillChange(
        oldAccount: String,
        oldAmount: Double,
        newAccount: String,
        newAmount: Double,
        includeInBalance: Boolean
    ) = viewModelScope.launch {
        val accountsList = _accounts.value.toMutableList()

        if (includeInBalance) {
            val oldAccountIndex = accountsList.indexOfFirst { it.name == oldAccount }
            if (oldAccountIndex != -1) {
                val oldAcc = accountsList[oldAccountIndex]
                val reverseOldAmount = -oldAmount
                accountsList[oldAccountIndex] = oldAcc.copy(
                    initialBalance = oldAcc.initialBalance + reverseOldAmount
                )
            }

            val newAccountIndex = accountsList.indexOfFirst { it.name == newAccount }
            if (newAccountIndex != -1) {
                val newAcc = accountsList[newAccountIndex]
                val applyNewAmount = newAmount
                accountsList[newAccountIndex] = newAcc.copy(
                    initialBalance = newAcc.initialBalance + applyNewAmount
                )
            }
        }

        _accounts.value = accountsList
        preferencesHelper.saveAccounts(_accounts.value)
    }

    fun recalculateAllAccountBalances() = viewModelScope.launch {
        val newBalances = mutableMapOf<String, Double>()

        _accounts.value.forEach { account ->
            newBalances[account.name] = account.initialBalance
        }

        _bills.value.forEach { bill ->
            if (bill.includeInBalance) {
                val currentBalance = newBalances[bill.account] ?: 0.0
                val adjustment = if (bill.type == "expense") {
                    bill.amount
                } else {
                    bill.amount
                }
                newBalances[bill.account] = currentBalance + adjustment
            }
        }

        _accounts.value = _accounts.value.map { account ->
            account.copy(initialBalance = newBalances[account.name] ?: account.initialBalance)
        }
        preferencesHelper.saveAccounts(_accounts.value)
    }

    fun getAccountsWithBalances(): StateFlow<List<SimpleAccount>> {
        return accounts
    }

    fun getNetAssetsTrend(): Flow<List<Pair<String, Double>>> =
        flow {
            val months = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            
            val trend = mutableListOf<Pair<String, Double>>()
            for (i in 5 downTo 0) {
                val monthIndex = (currentMonth - i + 12) % 12
                trend.add(months[monthIndex] to getMonthNetAssets(monthIndex))
            }
            emit(trend)
        }

    private fun getMonthNetAssets(monthIndex: Int): Double {
        val mockData = mapOf(
            0 to 500.0,
            1 to 800.0,
            2 to 1200.0,
            3 to 1600.0,
            4 to 1300.0,
            5 to 200.0
        )
        return mockData.getOrDefault((monthIndex + 6) % 6, 500.0)
    }

    fun getWeekRange(calendar: Calendar = Calendar.getInstance(), offset: Int = _periodOffset.value): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.WEEK_OF_YEAR, offset)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.DAY_OF_MONTH, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return start to end
    }

    fun getYearRange(calendar: Calendar = Calendar.getInstance(), offset: Int = _periodOffset.value): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.add(Calendar.YEAR, offset)
        val year = cal.get(Calendar.YEAR)
        cal.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(year, Calendar.DECEMBER, 31, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return start to end
    }

    fun getPeriodDisplayText(periodType: Int): String {
        val offset = _periodOffset.value
        val cal = Calendar.getInstance()
        
        return when (periodType) {
            0 -> {
                cal.add(Calendar.WEEK_OF_YEAR, offset)
                val year = cal.get(Calendar.YEAR)
                val week = cal.get(Calendar.WEEK_OF_YEAR)
                if (offset == 0) {
                    return "${year}年第${week}周 (当前)"
                } else if (offset == -1) {
                    return "${year}年第${week}周 (上周)"
                } else {
                    return "${year}年第${week}周"
                }
            }
            1 -> {
                cal.add(Calendar.MONTH, offset)
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1
                if (offset == 0) {
                    return "${year}年${month}月 (当前)"
                } else if (offset == -1) {
                    return "${year}年${month}月 (上月)"
                } else {
                    return "${year}年${month}月"
                }
            }
            2 -> {
                cal.add(Calendar.YEAR, offset)
                val year = cal.get(Calendar.YEAR)
                if (offset == 0) {
                    return "${year}年 (当前)"
                } else if (offset == -1) {
                    return "${year}年 (去年)"
                } else {
                    return "${year}年"
                }
            }
            else -> {
                cal.add(Calendar.MONTH, offset)
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1
                return "${year}年${month}月"
            }
        }
    }

    fun getPeriodShortName(periodType: Int): String {
        val offset = _periodOffset.value
        val cal = Calendar.getInstance()
        
        return when (periodType) {
            0 -> {
                cal.add(Calendar.WEEK_OF_YEAR, offset)
                val year = cal.get(Calendar.YEAR)
                val week = cal.get(Calendar.WEEK_OF_YEAR)
                "${year}年第${week}周"
            }
            1 -> {
                cal.add(Calendar.MONTH, offset)
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1
                "${year}年${month}月"
            }
            2 -> {
                cal.add(Calendar.YEAR, offset)
                val year = cal.get(Calendar.YEAR)
                "${year}年"
            }
            else -> {
                cal.add(Calendar.MONTH, offset)
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH) + 1
                "${year}年${month}月"
            }
        }
    }

    fun getTotalRecordCount(): Int {
        return _bills.value.size
    }

    /** 获取指定月份中有账单记录的日期集合 */
    fun getBillDaysInMonth(year: Int, month: Int): Set<Int> {
        val cal = Calendar.getInstance()
        return _bills.value.mapNotNull { bill ->
            cal.timeInMillis = bill.dateTime
            if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) + 1 == month) {
                cal.get(Calendar.DAY_OF_MONTH)
            } else null
        }.toSet()
    }

    fun getRecordDaysCount(): Int {
        val days = _bills.value.map { getStartOfDay(it.dateTime) }.distinct()
        return days.size
    }

    fun getMaxConsecutiveDays(): Int {
        val days = _bills.value.map { getStartOfDay(it.dateTime) }
            .distinct()
            .sorted()
        if (days.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1
        val dayMs = 24 * 60 * 60 * 1000L

        for (i in 1 until days.size) {
            if (days[i] - days[i - 1] == dayMs) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                currentStreak = 1
            }
        }

        // 检查最近连续（包含今天）
        val todayStart = getStartOfDay(System.currentTimeMillis())
        val todayStreak = if (days.lastOrNull() == todayStart) {
            var streak = 1
            for (i in days.size - 2 downTo 0) {
                if (days[i + 1] - days[i] == dayMs) streak++ else break
            }
            streak
        } else {
            val yesterdayStart = todayStart - dayMs
            if (days.lastOrNull() == yesterdayStart) {
                var streak = 1
                for (i in days.size - 2 downTo 0) {
                    if (days[i + 1] - days[i] == dayMs) streak++ else break
                }
                streak
            } else {
                0
            }
        }

        return todayStreak
    }

    private fun getStartOfDay(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private val gson = Gson()

    internal data class ExportData(
        val exportTime: Long = System.currentTimeMillis(),
        val startTime: Long,
        val endTime: Long,
        val billCount: Int,
        val bills: List<Bill>
    )

    fun exportBillsAsJson(startTime: Long, endTime: Long): String {
        val filtered = _bills.value.filter { it.dateTime in startTime..endTime }
            .sortedByDescending { it.dateTime }
        val exportData = ExportData(
            startTime = startTime,
            endTime = endTime,
            billCount = filtered.size,
            bills = filtered
        )
        return gson.toJson(exportData)
    }

    fun importBillsFromJson(json: String): Int {
        return try {
            val exportData = gson.fromJson(json, ExportData::class.java)
            val importedBills = exportData.bills
            if (importedBills.isEmpty()) return 0

            val existingIds = _bills.value.map { it.id }.toSet()
            val newBills = importedBills.filter { it.id !in existingIds }.map { bill ->
                bill.copy(createTime = System.currentTimeMillis())
            }

            if (newBills.isNotEmpty()) {
                _bills.value = (_bills.value + newBills).sortedByDescending { it.dateTime }
                preferencesHelper.saveBills(_bills.value)
            }

            newBills.size
        } catch (e: Exception) {
            -1
        }
    }

    fun exportBillsToFile(context: Context, startTime: Long, endTime: Long, fileName: String) {
        viewModelScope.launch {
            try {
                val json = exportBillsAsJson(startTime, endTime)
                val file = java.io.File(context.cacheDir, fileName)
                file.writeText(json)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "导出账单"))
            } catch (e: Exception) {
                Toast.makeText(context, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun importBillsFromUri(context: Context, uri: Uri, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val json = reader.readText()
                reader.close()

                val count = importBillsFromJson(json)
                when {
                    count > 0 -> {
                        Toast.makeText(context, "成功导入 $count 条账单", Toast.LENGTH_SHORT).show()
                        onResult(count)
                    }
                    count == 0 -> {
                        Toast.makeText(context, "没有新的账单需要导入", Toast.LENGTH_SHORT).show()
                        onResult(0)
                    }
                    else -> {
                        Toast.makeText(context, "导入失败，文件格式不正确", Toast.LENGTH_SHORT).show()
                        onResult(-1)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "导入失败: ${e.message}", Toast.LENGTH_SHORT).show()
                onResult(-1)
            }
        }
    }

    fun getPresetTimeRanges(): List<Triple<String, Long, Long>> {
        val now = Calendar.getInstance()

        val todayStart = now.clone() as Calendar
        todayStart.set(Calendar.HOUR_OF_DAY, 0)
        todayStart.set(Calendar.MINUTE, 0)
        todayStart.set(Calendar.SECOND, 0)
        todayStart.set(Calendar.MILLISECOND, 0)

        val todayEnd = now.clone() as Calendar
        todayEnd.set(Calendar.HOUR_OF_DAY, 23)
        todayEnd.set(Calendar.MINUTE, 59)
        todayEnd.set(Calendar.SECOND, 59)
        todayEnd.set(Calendar.MILLISECOND, 999)

        val weekStart = now.clone() as Calendar
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        weekStart.set(Calendar.HOUR_OF_DAY, 0)
        weekStart.set(Calendar.MINUTE, 0)
        weekStart.set(Calendar.SECOND, 0)
        weekStart.set(Calendar.MILLISECOND, 0)

        val weekEnd = now.clone() as Calendar
        weekEnd.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        weekEnd.set(Calendar.HOUR_OF_DAY, 23)
        weekEnd.set(Calendar.MINUTE, 59)
        weekEnd.set(Calendar.SECOND, 59)
        weekEnd.set(Calendar.MILLISECOND, 999)

        val monthStart = now.clone() as Calendar
        monthStart.set(Calendar.DAY_OF_MONTH, 1)
        monthStart.set(Calendar.HOUR_OF_DAY, 0)
        monthStart.set(Calendar.MINUTE, 0)
        monthStart.set(Calendar.SECOND, 0)
        monthStart.set(Calendar.MILLISECOND, 0)

        val monthEnd = now.clone() as Calendar
        monthEnd.set(Calendar.DAY_OF_MONTH, monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH))
        monthEnd.set(Calendar.HOUR_OF_DAY, 23)
        monthEnd.set(Calendar.MINUTE, 59)
        monthEnd.set(Calendar.SECOND, 59)
        monthEnd.set(Calendar.MILLISECOND, 999)

        val yearStart = now.clone() as Calendar
        yearStart.set(Calendar.DAY_OF_YEAR, 1)
        yearStart.set(Calendar.HOUR_OF_DAY, 0)
        yearStart.set(Calendar.MINUTE, 0)
        yearStart.set(Calendar.SECOND, 0)
        yearStart.set(Calendar.MILLISECOND, 0)

        val yearEnd = now.clone() as Calendar
        yearEnd.set(Calendar.DAY_OF_YEAR, yearEnd.getActualMaximum(Calendar.DAY_OF_YEAR))
        yearEnd.set(Calendar.HOUR_OF_DAY, 23)
        yearEnd.set(Calendar.MINUTE, 59)
        yearEnd.set(Calendar.SECOND, 59)
        yearEnd.set(Calendar.MILLISECOND, 999)

        val allStart = Calendar.getInstance().apply {
            set(2000, Calendar.JANUARY, 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val allEnd = now.clone() as Calendar
        allEnd.add(Calendar.YEAR, 10)
        allEnd.set(Calendar.MONTH, Calendar.DECEMBER)
        allEnd.set(Calendar.DAY_OF_MONTH, 31)
        allEnd.set(Calendar.HOUR_OF_DAY, 23)
        allEnd.set(Calendar.MINUTE, 59)
        allEnd.set(Calendar.SECOND, 59)
        allEnd.set(Calendar.MILLISECOND, 999)

        return listOf(
            Triple("今天", todayStart.timeInMillis, todayEnd.timeInMillis),
            Triple("本周", weekStart.timeInMillis, weekEnd.timeInMillis),
            Triple("本月", monthStart.timeInMillis, monthEnd.timeInMillis),
            Triple("本年", yearStart.timeInMillis, yearEnd.timeInMillis),
            Triple("全部", allStart.timeInMillis, allEnd.timeInMillis)
        )
    }
}

private fun getDefaultCategories(): List<Category> {
    val expenseCategories = listOf(
        Category(id = 1, type = "expense", name = "餐饮", icon = "restaurant", sortOrder = 0),
        Category(id = 2, type = "expense", name = "交通", icon = "directions_car", sortOrder = 1),
        Category(id = 3, type = "expense", name = "购物", icon = "shopping_cart", sortOrder = 2),
        Category(id = 4, type = "expense", name = "娱乐", icon = "sports_esports", sortOrder = 3),
        Category(id = 5, type = "expense", name = "住房", icon = "house", sortOrder = 4),
        Category(id = 6, type = "expense", name = "母婴", icon = "child_care", sortOrder = 5),
        Category(id = 7, type = "expense", name = "宠物", icon = "pets", sortOrder = 6),
        Category(id = 8, type = "expense", name = "人情", icon = "favorite", sortOrder = 7),
        Category(id = 9, type = "expense", name = "学习办公", icon = "school", sortOrder = 8),
        Category(id = 10, type = "expense", name = "医疗", icon = "medical_services", sortOrder = 9)
    )

    val incomeCategories = listOf(
        Category(id = 11, type = "income", name = "工资", icon = "payments", sortOrder = 0),
        Category(id = 12, type = "income", name = "生活费", icon = "account_balance_wallet", sortOrder = 1),
        Category(id = 13, type = "income", name = "红包", icon = "redeem", sortOrder = 2),
        Category(id = 14, type = "income", name = "退款", icon = "refund", sortOrder = 3),
        Category(id = 15, type = "income", name = "经营", icon = "business", sortOrder = 4),
        Category(id = 16, type = "income", name = "分红", icon = "savings", sortOrder = 5),
        Category(id = 17, type = "income", name = "理财", icon = "trending_up", sortOrder = 6),
        Category(id = 18, type = "income", name = "年终奖", icon = "card_giftcard", sortOrder = 7),
        Category(id = 19, type = "income", name = "借入", icon = "swap_horiz", sortOrder = 8),
        Category(id = 20, type = "income", name = "其他收入", icon = "add_circle", sortOrder = 9)
    )

    return expenseCategories + incomeCategories
}

private fun getDefaultAccounts(): List<SimpleAccount> {
        return listOf(
            SimpleAccount(id = 1, name = "微信", icon = "message", initialBalance = 2067.59, type = "asset"),
            SimpleAccount(id = 2, name = "招商银行", icon = "account_balance", initialBalance = 46.07, type = "asset", accountNumber = "8857"),
            SimpleAccount(id = 3, name = "花呗", icon = "credit_card", initialBalance = -357.24, type = "liability")
        )
    }

private fun getSampleBills(): List<Bill> {
    val now = System.currentTimeMillis()
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 10)
    cal.set(Calendar.MINUTE, 30)
    val time1 = cal.timeInMillis
    
    cal.set(Calendar.HOUR_OF_DAY, 14)
    cal.set(Calendar.MINUTE, 15)
    val time2 = cal.timeInMillis
    
    cal.set(Calendar.HOUR_OF_DAY, 18)
    cal.set(Calendar.MINUTE, 30)
    val time3 = cal.timeInMillis
    
    return listOf(
        Bill(
            id = 1,
            type = "expense",
            categoryName = "餐饮",
            categoryIcon = "restaurant",
            amount = -45.5,
            account = "微信",
            book = "系统账本",
            remark = "午餐",
            dateTime = time1
        ),
        Bill(
            id = 2,
            type = "expense",
            categoryName = "购物",
            categoryIcon = "shopping_cart",
            amount = -128.0,
            account = "招商银行",
            book = "系统账本",
            remark = "日用品",
            dateTime = time2
        ),
        Bill(
            id = 3,
            type = "income",
            categoryName = "工资",
            categoryIcon = "payments",
            amount = 5000.0,
            account = "招商银行",
            book = "系统账本",
            remark = "",
            dateTime = time3
        )
    )
}
