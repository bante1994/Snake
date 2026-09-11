package com.example.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.example.config.AdMobConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AdLoadStatus {
    object Idle : AdLoadStatus()
    object Loading : AdLoadStatus()
    data class Ready(val isTestFallback: Boolean) : AdLoadStatus()
    data class Failed(val errorCode: Int, val message: String, val explanation: String) : AdLoadStatus()
    object Showing : AdLoadStatus()
}

/**
 * Production-ready Google AdMob Rewarded Ad Manager.
 *
 * Handles:
 * 1. SDK initialization and test device configuration.
 * 2. Manifest vs Config App ID validation.
 * 3. Loading with real Ad Unit ID.
 * 4. Graceful fallback to official Google Test Ad on Error 3 (No Fill / Pending Activation).
 * 5. Rewarded video presentation and reward callback.
 */
object AdMobManager {
    private const val TAG = "AdMobManager"

    private var isInitialized = false
    private var rewardedAd: RewardedAd? = null
    private var isCurrentlyLoading = false
    private var isTestFallbackActive = false

    private val _adStatus = MutableStateFlow<AdLoadStatus>(AdLoadStatus.Idle)
    val adStatus: StateFlow<AdLoadStatus> = _adStatus.asStateFlow()

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage.asStateFlow()

    /**
     * Initialize Google Mobile Ads SDK once per app lifecycle.
     */
    fun initialize(context: Context) {
        if (isInitialized) return

        try {
            // Verify manifest metadata matches config
            verifyManifestAppId(context)

            // Configure test device IDs (includes emulator by default)
            val testDeviceIds = mutableListOf<String>()
            testDeviceIds.add(AdRequest.DEVICE_ID_EMULATOR)
            testDeviceIds.addAll(AdMobConfig.TEST_DEVICE_IDS)

            val configuration = RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build()
            MobileAds.setRequestConfiguration(configuration)

            MobileAds.initialize(context) { initStatus ->
                Log.d(TAG, "Google Mobile Ads initialized: ${initStatus.adapterStatusMap}")
                isInitialized = true
                // Preload an ad ready for when the user dies
                preloadAd(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MobileAds: ${e.message}", e)
            _lastErrorMessage.value = "AdMob Init Error: ${e.message}"
        }
    }

    /**
     * Checks whether the application ID in AndroidManifest.xml matches AdMobConfig.
     */
    private fun verifyManifestAppId(context: Context) {
        try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            val manifestAppId = appInfo.metaData?.getString("com.google.android.gms.ads.APPLICATION_ID")
            val configAppId = AdMobConfig.ADMOB_APP_ID.trim()

            if (manifestAppId.isNullOrEmpty()) {
                Log.w(TAG, "⚠️ MISSING com.google.android.gms.ads.APPLICATION_ID in AndroidManifest.xml!")
            } else if (!manifestAppId.equals(configAppId, ignoreCase = true) &&
                !configAppId.contains("3940256099942544") // skip warning if using default sample
            ) {
                Log.w(
                    TAG,
                    "⚠️ WARNING: AndroidManifest.xml APPLICATION_ID ($manifestAppId) does not match AdMobConfig.ADMOB_APP_ID ($configAppId)! " +
                    "To show real ads, update line 32 of AndroidManifest.xml to match your real AdMob App ID."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not read manifest metadata: ${e.message}")
        }
    }

    /**
     * Preloads a rewarded ad using the configured ad unit ID.
     */
    fun preloadAd(context: Context) {
        if (isCurrentlyLoading || (rewardedAd != null && _adStatus.value is AdLoadStatus.Ready)) {
            return
        }

        val adUnitId = AdMobConfig.ADMOB_REWARDED_AD_UNIT_ID.trim()
        val isDefaultTestId = adUnitId.contains("3940256099942544")

        loadAdInternal(context, adUnitId, isFallback = false)
    }

    private fun loadAdInternal(context: Context, unitId: String, isFallback: Boolean) {
        isCurrentlyLoading = true
        _adStatus.value = AdLoadStatus.Loading

        val adRequest = AdRequest.Builder().build()
        Log.d(TAG, "Requesting Rewarded Ad from AdMob with AdUnitId: $unitId (isFallback=$isFallback)")

        RewardedAd.load(
            context,
            unitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.i(TAG, "✓ Rewarded ad loaded successfully! (Unit: $unitId, isFallback=$isFallback)")
                    rewardedAd = ad
                    isCurrentlyLoading = false
                    isTestFallbackActive = isFallback
                    _adStatus.value = AdLoadStatus.Ready(isTestFallback = isFallback)
                    _lastErrorMessage.value = null
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    val code = loadAdError.code
                    val msg = loadAdError.message
                    val explanation = getErrorExplanation(code)

                    Log.w(TAG, "Ad failed to load: Code $code ($msg). Explanation: $explanation")
                    rewardedAd = null
                    isCurrentlyLoading = false

                    // If user used their real ad unit ID and it failed with NO_FILL (code 3) or INVALID_REQUEST (code 1)
                    if (!isFallback && AdMobConfig.FALLBACK_TO_TEST_AD_ON_NO_FILL &&
                        unitId != AdMobConfig.GOOGLE_TEST_REWARDED_AD_UNIT_ID
                    ) {
                        Log.i(TAG, "Falling back to Google official Test Rewarded Ad Unit so ad can still be displayed...")
                        _lastErrorMessage.value = "Real Ad returned code $code ($msg). Falling back to Google test ad..."
                        loadAdInternal(context, AdMobConfig.GOOGLE_TEST_REWARDED_AD_UNIT_ID, isFallback = true)
                    } else {
                        _adStatus.value = AdLoadStatus.Failed(code, msg, explanation)
                        _lastErrorMessage.value = "AdMob Error $code: $msg - $explanation"
                    }
                }
            }
        )
    }

    /**
     * Explains why an AdMob error occurred, especially for real credentials.
     */
    private fun getErrorExplanation(code: Int): String {
        return when (code) {
            AdRequest.ERROR_CODE_NO_FILL ->
                "ERROR_CODE_NO_FILL (Code 3): Google has no ad inventory yet. New AdMob accounts and freshly created Ad Units take 24-48 hours to start serving live ads. Also check app-ads.txt and billing in your AdMob console."
            AdRequest.ERROR_CODE_INVALID_REQUEST ->
                "ERROR_CODE_INVALID_REQUEST (Code 1): Check if the Ad Unit ID is formatted correctly (ca-app-pub-XXX/YYY) and that APPLICATION_ID in AndroidManifest.xml matches your AdMob App ID."
            AdRequest.ERROR_CODE_NETWORK_ERROR ->
                "ERROR_CODE_NETWORK_ERROR (Code 2): Device cannot connect to Google AdMob servers. Check internet connection."
            AdRequest.ERROR_CODE_INTERNAL_ERROR ->
                "ERROR_CODE_INTERNAL_ERROR (Code 0): Google AdMob internal server error. Try again shortly."
            8 ->
                "ERROR_CODE_APP_ID_MISSING (Code 8): The Google Mobile Ads SDK application ID was not found in AndroidManifest.xml."
            else ->
                "AdMob error code $code. Check AdMob console and device logs."
        }
    }

    /**
     * Shows the rewarded ad if available, otherwise triggers fallback reward.
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdUnavailable: (String) -> Unit
    ) {
        val currentAd = rewardedAd

        if (currentAd == null) {
            val status = _adStatus.value
            val reason = when (status) {
                is AdLoadStatus.Failed -> status.explanation
                is AdLoadStatus.Loading -> "Ad is still downloading from Google servers..."
                else -> "No ad is currently ready. Preloading new ad..."
            }
            Log.w(TAG, "Rewarded ad not ready to show. Reason: $reason")
            preloadAd(activity)
            onAdUnavailable(reason)
            return
        }

        _adStatus.value = AdLoadStatus.Showing
        var userEarnedReward = false

        currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed full screen.")
                rewardedAd = null // Ad consumed
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed. User earned reward: $userEarnedReward")
                _adStatus.value = AdLoadStatus.Idle
                // Preload the next ad for future revives
                preloadAd(activity)

                if (userEarnedReward) {
                    onRewardEarned()
                }
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                rewardedAd = null
                _adStatus.value = AdLoadStatus.Failed(adError.code, adError.message, adError.message)
                preloadAd(activity)
                onAdUnavailable("Ad failed to show: ${adError.message}")
            }
        }

        currentAd.show(activity) { rewardItem ->
            Log.i(TAG, "User completed watching rewarded ad! Reward: ${rewardItem.amount} ${rewardItem.type}")
            userEarnedReward = true
        }
    }

    fun isAdReady(): Boolean = rewardedAd != null
}
