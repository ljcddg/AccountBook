package com.apesource.account.data.entity

data class Book(
    val id: Long = 0,
    val name: String,
    val cover: String = "",
    val startDay: Int = 1,
    val isDefault: Boolean = false,
    val createTime: Long = System.currentTimeMillis()
)