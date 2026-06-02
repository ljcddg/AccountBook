package com.apesource.account.data.entity

data class Bill(
    val id: Long = 0,
    val type: String,
    val categoryName: String,
    val categoryIcon: String,
    val amount: Double,
    val account: String,
    val book: String,
    val remark: String = "",
    val dateTime: Long,
    val imagePath: String? = null,
    val createTime: Long = System.currentTimeMillis(),
    val includeInBalance: Boolean = true
)
