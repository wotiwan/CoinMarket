package com.example.coinmarket

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RateCheckInteractor {
    val networkClient = NetworkClient()

    suspend fun requestRate(): String {
        Log.d("RateCheckInteractor", "requestRate() вызван")
        return withContext(Dispatchers.IO) {
            val result = networkClient.request(MainViewModel.USD_RATE_URL)
            Log.d("RateCheckInteractor", "Ответ от API: $result")

            if (!result.isNullOrEmpty()) {
                val parsedRate = parseRate(result)
                Log.d("RateCheckInteractor", "После парсинга: $parsedRate")
                parsedRate
            } else {
                Log.e("RateCheckInteractor", "Ответ пустой или null")
                ""
            }
        }
    }


    private fun parseRate(jsonString: String): String {
        return try {
            val jsonObject = JSONObject(jsonString)
            val usdRate = jsonObject.optString("USD", "Курс USD не найден")

            "$usdRate"
        } catch (e: Exception) {
            Log.e("RateCheckInteractor", "Ошибка парсинга JSON", e)
            "Ошибка парсинга"
        }
    }
}
