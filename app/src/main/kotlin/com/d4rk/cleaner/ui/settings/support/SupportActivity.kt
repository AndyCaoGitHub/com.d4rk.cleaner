@file:Suppress("DEPRECATION")
package com.d4rk.cleaner.ui.settings.support
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.d4rk.cleaner.databinding.ActivitySupportBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import me.zhanghai.android.fastscroll.FastScrollerBuilder
class SupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySupportBinding
    private lateinit var billingClient: BillingClient
    private val skuDetailsMap = mutableMapOf<String, SkuDetails>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MobileAds.initialize(this)
        binding.adView.loadAd(AdRequest.Builder().build())
        FastScrollerBuilder(binding.scrollView).useMd2Style().build()
        binding.buttonWebAd.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://bit.ly/3p8bpjj")))
        }
        MobileAds.initialize(this)
        billingClient = BillingClient.newBuilder(this)
            .setListener { _, _ ->
            }
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    querySkuDetails()
                }
            }
            override fun onBillingServiceDisconnected() {
            }
        })
        binding.buttonLowDonation.setOnClickListener { initiatePurchase("low_donation") }
        binding.buttonNormalDonation.setOnClickListener { initiatePurchase("normal_donation") }
        binding.buttonHighDonation.setOnClickListener { initiatePurchase("high_donation") }
        binding.buttonExtremeDonation.setOnClickListener { initiatePurchase("extreme_donation") }
    }
    private fun querySkuDetails() {
        val skuList = listOf("low_donation", "normal_donation", "high_donation", "extreme_donation")
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                for (skuDetails in skuDetailsList) {
                    skuDetailsMap[skuDetails.sku] = skuDetails
                    when (skuDetails.sku) {
                        "low_donation" -> binding.buttonLowDonation.text = skuDetails.price
                        "normal_donation" -> binding.buttonNormalDonation.text = skuDetails.price
                        "high_donation" -> binding.buttonHighDonation.text = skuDetails.price
                        "extreme_donation" -> binding.buttonExtremeDonation.text = skuDetails.price
                    }
                }
            }
        }
    }
    private fun initiatePurchase(sku: String) {
        val skuDetails = skuDetailsMap[sku]
        if (skuDetails != null) {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build()
            billingClient.launchBillingFlow(this, flowParams)
        }
    }
}