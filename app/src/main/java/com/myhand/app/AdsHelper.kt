package com.myhand.app

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object AdsHelper {
    private var started = false

    fun start(activity: Activity, onReady: () -> Unit) {
        val ci = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()
        ci.requestConsentInfoUpdate(activity, params, {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                initIfAllowed(activity, ci, onReady)
            }
        }, { _ ->
            initIfAllowed(activity, ci, onReady)
        })
    }

    private fun initIfAllowed(a: Activity, ci: ConsentInformation, onReady: () -> Unit) {
        if (!ci.canRequestAds()) return
        if (started) {
            onReady()
            return
        }
        started = true
        Thread {
            MobileAds.initialize(a.applicationContext) { }
            a.runOnUiThread {
                onReady()
            }
        }.start()
    }

    fun privacyRequired(ctx: Context): Boolean =
        UserMessagingPlatform.getConsentInformation(ctx).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun showPrivacy(a: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(a) { _ -> }
    }

    fun loadBanner(ctx: Context, container: ViewGroup) {
        if (!started) return
        val v = AdView(ctx)
        v.setAdSize(AdSize.BANNER)
        v.adUnitId = ctx.getString(R.string.admob_banner)
        container.removeAllViews()
        container.addView(v)
        v.loadAd(AdRequest.Builder().build())
    }

    // Stubs to prevent build errors if other screens try to call them
    fun showInterstitial(a: Activity, onDone: () -> Unit) { onDone() }
    fun showRewarded(a: Activity, onReward: () -> Unit, onUnavailable: () -> Unit) { onUnavailable() }
}
