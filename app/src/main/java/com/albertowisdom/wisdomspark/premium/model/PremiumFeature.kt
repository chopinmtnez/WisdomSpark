package com.albertowisdom.wisdomspark.premium.model

/**
 * Características Premium disponibles en WisdomSpark
 */
enum class PremiumFeature(
    val id: String,
    val nameKey: String,
    val descriptionKey: String,
    val icon: String
) {
    AD_FREE(
        id = "ad_free",
        nameKey = "premium_feature_ad_free",
        descriptionKey = "premium_feature_ad_free_desc",
        icon = "🚫"
    ),
    PREMIUM_THEMES(
        id = "premium_themes",
        nameKey = "premium_feature_themes",
        descriptionKey = "premium_feature_themes_desc",
        icon = "🎨"
    ),
    UNLIMITED_FAVORITES(
        id = "unlimited_favorites",
        nameKey = "premium_feature_unlimited_favorites",
        descriptionKey = "premium_feature_unlimited_favorites_desc",
        icon = "⭐"
    ),
    ADVANCED_CATEGORIES(
        id = "advanced_categories",
        nameKey = "premium_feature_advanced_categories",
        descriptionKey = "premium_feature_advanced_categories_desc",
        icon = "📚"
    ),
    QUOTE_SHARING_STYLES(
        id = "quote_sharing_styles",
        nameKey = "premium_feature_sharing_styles",
        descriptionKey = "premium_feature_sharing_styles_desc",
        icon = "📤"
    ),
    OFFLINE_MODE(
        id = "offline_mode",
        nameKey = "premium_feature_offline",
        descriptionKey = "premium_feature_offline_desc",
        icon = "📱"
    ),
    DAILY_QUOTES_CUSTOMIZATION(
        id = "daily_quotes_customization",
        nameKey = "premium_feature_daily_customization",
        descriptionKey = "premium_feature_daily_customization_desc",
        icon = "⏰"
    ),
    PRIORITY_SUPPORT(
        id = "priority_support",
        nameKey = "premium_feature_priority_support",
        descriptionKey = "premium_feature_priority_support_desc",
        icon = "🎧"
    )
}