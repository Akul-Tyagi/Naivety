package com.abundance.naivety.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AdManager {
    private const val TAG = "AdManager"
    private const val INTERSTITIAL_AD_ID = "ca-app-pub-1590434069699907/2680152540"
    private const val NAVIGATION_DELAY = 50L

    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private val mainScope = CoroutineScope(Dispatchers.Main)

    fun initialize(context: Context) {
        MobileAds.initialize(context) { status ->
            Log.d(TAG, "MobileAds initialization status: $status")
            preloadInterstitialAd(context)
        }
    }

    fun preloadInterstitialAd(context: Context) {
        // Don't load if ad already exists or is currently loading
        if (interstitialAd != null || isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isAdLoading = false
                    setupFullScreenCallbacks(ad)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isAdLoading = false
                }
            }
        )
    }

    private fun setupFullScreenCallbacks(ad: InterstitialAd) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed")
                interstitialAd = null
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "Ad failed to show: ${adError.message}")
                interstitialAd = null
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content")
            }
        }
    }

    fun showInterstitialAd(
        activity: Activity,
        onAdClosed: () -> Unit,
        onAdFailedToShow: () -> Unit
    ) {
        val ad = interstitialAd

        if (ad != null) {
            // Set up a callback before showing the ad
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed")
                    interstitialAd = null

                    // Add a small delay before navigation to allow UI to settle
                    mainScope.launch {
                        delay(NAVIGATION_DELAY)
                        onAdClosed()
                    }

                    // Preload the next ad
                    preloadInterstitialAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    interstitialAd = null

                    mainScope.launch {
                        delay(NAVIGATION_DELAY)
                        onAdFailedToShow()
                    }

                    preloadInterstitialAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content")
                    interstitialAd = null
                }
            }

            // Show the ad
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready yet")
            onAdFailedToShow()
            preloadInterstitialAd(activity)
        }
    }
}