package com.apesource.account.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconUtils {
    fun getIconByName(name: String): ImageVector {
        return when (name) {
            "restaurant" -> Icons.Default.Restaurant
            "directions_car" -> Icons.Default.DirectionsCar
            "shopping_cart" -> Icons.Default.ShoppingCart
            "sports_esports" -> Icons.Default.SportsEsports
            "house" -> Icons.Default.House
            "child_care" -> Icons.Default.ChildCare
            "pets" -> Icons.Default.Pets
            "favorite" -> Icons.Default.Favorite
            "school" -> Icons.Default.School
            "medical_services" -> Icons.Default.MedicalServices
            "payments" -> Icons.Default.Payments
            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
            "redeem" -> Icons.Default.Redeem
            "refund" -> Icons.Default.Undo
            "business" -> Icons.Default.Business
            "savings" -> Icons.Default.Savings
            "trending_up" -> Icons.Default.TrendingUp
            "card_giftcard" -> Icons.Default.CardGiftcard
            "swap_horiz" -> Icons.Default.SwapHoriz
            "add_circle" -> Icons.Default.AddCircle
            "chat" -> Icons.Default.Chat
            "account_balance" -> Icons.Default.AccountBalance
            "credit_card" -> Icons.Default.CreditCard
            else -> Icons.Default.Circle
        }
    }
}
