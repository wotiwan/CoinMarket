package com.example.coinmarket

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigDecimal

class RateCheckService : Service() {
    val handler = Handler(Looper.getMainLooper())
    var rateCheckAttempt = 0
    lateinit var startRate: BigDecimal
    lateinit var targetRate: BigDecimal
    val rateCheckInteractor = RateCheckInteractor()

    val rateCheckRunnable: Runnable = Runnable {
        // Write your code here. Check number of attempts and stop service if needed
        requestAndCheckRate()
    }

    private fun requestAndCheckRate() {
        GlobalScope.launch(Dispatchers.IO) {
            val rateString = rateCheckInteractor.requestRate()
            if (rateString.isNotEmpty()) {
                try {
                    val rate = BigDecimal(rateString)
                    Log.d(TAG, "Полученная котировка: $rate")
                    // Проверяем, достигнута ли целевая котировка
                    if (rate >= targetRate) {
                        sendNotification("Целевая котировка достигнута! Курс: $rate", R.drawable.up)
                        Log.d(TAG, "Целевая котировка достигнута! Курс: $rate")
                        stopService() // Останавливаем сервис, так как цель достигнута
                    } else {
                        Log.d(TAG, "Курс не достиг целевой. Проверим снова через ${RATE_CHECK_INTERVAL / 1000} секунд. Курс: ${rate}")
                        // Повторный запуск через интервал
                        handler.postDelayed(rateCheckRunnable, RATE_CHECK_INTERVAL)
                    }
                } catch (e: NumberFormatException) {
                    Log.e(TAG, "Ошибка преобразования котировки в BigDecimal", e)
                }
            } else {
                Log.e(TAG, "Ответ от API пустой или недоступен.")
            }
        }
    }

    private fun sendNotification(message: String, icon: Int) {
        // Создаем Notification Manager
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Для Android 8.0 и выше нужно создать канал уведомлений
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "rate_notification_channel",
                "Rate Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Создаем уведомление
        val notification: Notification = NotificationCompat.Builder(this, "rate_notification_channel")
            .setContentTitle("Изменение котировки")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_background)  // Иконка уведомления (по умолчанию)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, icon)) // Стрелка вверх или вниз
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Отправляем уведомление
        notificationManager.notify(1, notification)
    }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRate = BigDecimal(intent?.getStringExtra(ARG_START_RATE))
        targetRate = BigDecimal(intent?.getStringExtra(ARG_TARGET_RATE))

        Log.d(TAG, "onStartCommand startRate = $startRate targetRate = $targetRate")

        // Запуск проверки котировки
        handler.post(rateCheckRunnable)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(rateCheckRunnable)  // Останавливаем проверку котировки
        Log.d(TAG, "Сервис остановлен")
    }

    private fun stopService() {
        stopSelf()  // Останавливаем сервис
        Log.d(TAG, "Сервис остановлен. Цель достигнута.")
        handler.post {
            Toast.makeText(applicationContext, "Целевая котировка достигнута!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val TAG = "RateCheckService"
        const val RATE_CHECK_INTERVAL = 5000L  // Интервал проверки котировки (5 секунд)
        const val RATE_CHECK_ATTEMPTS_MAX = 100  // Максимальное количество попыток

        const val ARG_START_RATE = "ARG_START_RATE"
        const val ARG_TARGET_RATE = "ARG_TARGET_RATE"

        fun startService(context: Context, startRate: String, targetRate: String) {
            context.startService(Intent(context, RateCheckService::class.java).apply {
                putExtra(ARG_START_RATE, startRate)
                putExtra(ARG_TARGET_RATE, targetRate)
            })
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, RateCheckService::class.java))
        }
    }
}
