package com.example.naivety.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import android.os.Handler
import android.os.Looper

object AdManager {
    private const val TAG = "AdManager"
    private const val REWARDED_AD_ID = "ca-app-pub-1590434069699907/2840514235"
    private const val NAVIGATION_DELAY = 150L
    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun initialize(context: Context) {
        MobileAds.initialize(context) { status ->
            Log.d(TAG, "MobileAds initialization status: $status")
            preloadRewardedAd(context)
        }
    }

    fun preloadRewardedAd(context: Context) {
        if (rewardedAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(context, REWARDED_AD_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Rewarded ad loaded successfully")
                rewardedAd = ad
                isAdLoading = false
                setupFullScreenCallbacks()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                rewardedAd = null
                isAdLoading = false
            }
        })
    }

    private fun setupFullScreenCallbacks() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed")
                rewardedAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show: ${adError.message}")
                rewardedAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content")
            }
        }
    }

    fun showRewardedAd(activity: Activity, onAdClosed: () -> Unit, onAdFailedToShow: () -> Unit) {
        if (rewardedAd != null) {
            // Set up a callback before showing the ad
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed")
                    rewardedAd = null

                    // Add a small delay before navigation to allow UI to settle
                    Handler(Looper.getMainLooper()).postDelayed({
                        onAdClosed()
                    }, NAVIGATION_DELAY)

                    // Preload the next ad
                    preloadRewardedAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    Handler(Looper.getMainLooper()).postDelayed({
                        onAdFailedToShow()
                    }, NAVIGATION_DELAY)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content")
                }
            }

            // Show the ad
            rewardedAd?.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                // Note: We handle navigation in onAdDismissedFullScreenContent instead
            }
        } else {
            Log.d(TAG, "Rewarded ad not ready yet")
            onAdFailedToShow()
            preloadRewardedAd(activity)
        }
    }
}