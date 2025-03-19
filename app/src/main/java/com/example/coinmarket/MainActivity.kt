package com.example.coinmarket

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel
    lateinit var textRate: TextView
    lateinit var textTargetRate: EditText
    lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViewModel()
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.usdRate.observe(this, {
            textRate.text = "$it USD"
        })

        // Загружаем начальную котировку
        viewModel.onCreate()
    }

    private fun initView() {
        textRate = findViewById(R.id.textUsdRubRate)
        textTargetRate = findViewById(R.id.textTargetRate)
        rootView = findViewById(R.id.rootView)

        // Обработчик нажатия на кнопку "Обновить"
        findViewById<Button>(R.id.btnRefresh).setOnClickListener {
            viewModel.onRefreshClicked()
        }

        // Обработчик нажатия на кнопку "Подписаться на котировку"
        findViewById<Button>(R.id.btnSubscribeToRate).setOnClickListener {
            val targetRateText = textTargetRate.text.toString()
            val startRateText = viewModel.usdRate.value

            if (targetRateText.isNotEmpty() && !startRateText.isNullOrEmpty()) {
                try {
                    // Останавливаем сервис, если он уже запущен
                    RateCheckService.stopService(this)

                    // Запускаем сервис с текущей и целевой котировкой
                    RateCheckService.startService(this, startRateText, targetRateText)
                } catch (e: Exception) {
                    Snackbar.make(rootView, "Ошибка при запуске сервиса", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                // Если введена пустая котировка или текущий курс отсутствует
                if (targetRateText.isEmpty()) {
                    Snackbar.make(rootView, R.string.target_rate_empty, Snackbar.LENGTH_SHORT).show()
                } else if (startRateText.isNullOrEmpty()) {
                    Snackbar.make(rootView, R.string.current_rate_empty, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }
}
