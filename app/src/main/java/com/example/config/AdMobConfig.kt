package com.example.config

/**
 * =========================================================================
 *                   ADMOB BACKEND CONFIGURATION
 * =========================================================================
 *
 * LOCATION TO ADD YOUR PUB KEY / ADMOB CREDENTIALS:
 * File: app/src/main/java/com/example/config/AdMobConfig.kt
 *
 * INSTRUCTIONS:
 * 1. Put your AdMob Publisher Key in [ADMOB_PUBLISHER_ID]
 *    Example: "pub-1234567890123456"
 *
 * 2. Put your AdMob Application ID in [ADMOB_APP_ID]
 *    Example: "ca-app-pub-1234567890123456~9876543210"
 *    NOTE: Also update the <meta-data> in app/src/main/AndroidManifest.xml line 32!
 *
 * 3. Put your Rewarded Ad Unit ID in [ADMOB_REWARDED_AD_UNIT_ID]
 *    Example: "ca-app-pub-1234567890123456/1234567890"
 * =========================================================================
 */
object AdMobConfig {

    /**
     * YOUR ADMOB PUBLISHER ID (w pub key):
     * Replace with your publisher ID: pub-XXXXXXXXXXXXXXXX
     */
    const val ADMOB_PUBLISHER_ID: String = "pub-6069047874277520"

    /**
     * YOUR ADMOB APPLICATION ID:
     * Replace with your AdMob App ID: ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
     */
    const val ADMOB_APP_ID: String = "ca-app-pub-3940256099942544~3347511713"

    /**
     * YOUR REWARDED AD UNIT ID (Used for reviving after crash):
     * Replace with your Rewarded Ad Unit ID: ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
     */
    const val ADMOB_REWARDED_AD_UNIT_ID: String = "ca-app-pub-6069047874277520/8238945998"

    /**
     * Google's official sample rewarded ad unit for safe testing and fallback
     */
    const val GOOGLE_TEST_REWARDED_AD_UNIT_ID: String = "ca-app-pub-3940256099942544/5224354917"

    /**
     * Automatic Fallback Option:
     * When set to true, if your real AdMob Ad Unit returns ERROR_CODE_NO_FILL (Code 3)
     * because your account/ad-unit is new (takes 24-48 hours for Google to serve live inventory),
     * the game automatically falls back to Google's test ad so you can always see and test the ad!
     */
    var FALLBACK_TO_TEST_AD_ON_NO_FILL: Boolean = true

    /**
     * Test Device IDs (for safe testing in emulator/development)
     */
    val TEST_DEVICE_IDS: List<String> = listOf(
        "EMULATOR"
    )
}
