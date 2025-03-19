package com.example.coinmarket

import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val usdRate = MutableLiveData<String>()
    val rateCheckInteractor = RateCheckInteractor()

    fun onCreate() {
        refreshRate()
    }

    fun onRefreshClicked() {
        refreshRate()
    }

    private fun refreshRate() {
        Log.d(TAG, "refreshRate() вызван")
        GlobalScope.launch(Dispatchers.Main) {
            Log.d(TAG, "Корутина стартовала")
            val rate = rateCheckInteractor.requestRate()
            Log.d(TAG, "usdRate = $rate")
            usdRate.value = rate
        }
    }

    companion object {
        const val TAG = "MainViewModel"
        const val USD_RATE_URL = "https://min-api.cryptocompare.com/data/price?fsym=ETH&tsyms=USD&api_key=5208bf63c63ba915540200e8d9b5a2610a28bbd07f07cd2940878a4e7c8a25a7"
    }
}