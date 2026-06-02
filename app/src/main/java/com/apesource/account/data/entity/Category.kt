package com.apesource.account.data.entity

data class Category(
    val id: Long = 0,
    val type: String,
    val name: String,
    val icon: String,
    val sortOrder: Int
)
