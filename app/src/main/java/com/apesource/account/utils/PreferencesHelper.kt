package com.apesource.account.utils

import android.content.Context
import android.content.SharedPreferences
import com.apesource.account.data.entity.Bill
import com.apesource.account.data.entity.Book
import com.apesource.account.data.entity.Category
import com.apesource.account.ui.viewmodel.SimpleAccount
import com.apesource.account.ui.viewmodel.AppSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesHelper(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREFS_NAME = "account_book_prefs"
        private const val KEY_MONTHLY_BUDGET = "monthly_budget"
        private const val KEY_BILLS = "bills"
        private const val KEY_PERIOD_OFFSET = "period_offset"
        private const val KEY_BOOKS = "books"
        private const val KEY_CATEGORIES = "categories"
        private const val KEY_ACCOUNTS = "accounts"
        private const val KEY_APP_SETTINGS = "app_settings"
        private const val DEFAULT_BUDGET = 5000.0
    }
    
    fun saveMonthlyBudget(budget: Double) {
        prefs.edit().putFloat(KEY_MONTHLY_BUDGET, budget.toFloat()).apply()
    }
    
    fun getMonthlyBudget(): Double {
        return prefs.getFloat(KEY_MONTHLY_BUDGET, DEFAULT_BUDGET.toFloat()).toDouble()
    }
    
    fun saveBills(bills: List<Bill>) {
        val json = gson.toJson(bills)
        prefs.edit().putString(KEY_BILLS, json).apply()
    }
    
    fun getBills(): List<Bill> {
        val json = prefs.getString(KEY_BILLS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Bill>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun savePeriodOffset(offset: Int) {
        prefs.edit().putInt(KEY_PERIOD_OFFSET, offset).apply()
    }
    
    fun getPeriodOffset(): Int {
        return prefs.getInt(KEY_PERIOD_OFFSET, 0)
    }
    
    fun saveBooks(books: List<Book>) {
        val json = gson.toJson(books)
        prefs.edit().putString(KEY_BOOKS, json).apply()
    }
    
    fun getBooks(): List<Book> {
        val json = prefs.getString(KEY_BOOKS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Book>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveCategories(categories: List<Category>) {
        val json = gson.toJson(categories)
        prefs.edit().putString(KEY_CATEGORIES, json).apply()
    }
    
    fun getCategories(): List<Category> {
        val json = prefs.getString(KEY_CATEGORIES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Category>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveAccounts(accounts: List<SimpleAccount>) {
        val json = gson.toJson(accounts)
        prefs.edit().putString(KEY_ACCOUNTS, json).apply()
    }
    
    fun getAccounts(): List<SimpleAccount> {
        val json = prefs.getString(KEY_ACCOUNTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SimpleAccount>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun saveAppSettings(settings: AppSettings) {
        val json = gson.toJson(settings)
        prefs.edit().putString(KEY_APP_SETTINGS, json).apply()
    }
    
    fun getAppSettings(): AppSettings? {
        val json = prefs.getString(KEY_APP_SETTINGS, null) ?: return null
        return try {
            gson.fromJson(json, AppSettings::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
