package com.example.cryptoalarm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AdvancedCryptoWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = applicationContext.getSharedPreferences("watchlist", Context.MODE_PRIVATE)
            val coinsRaw = prefs.getString("coins", "") ?: ""
            if (coinsRaw.isBlank()) return@withContext Result.success()

            val lines = coinsRaw.split('\n')
            for (line in lines) {
                val parts = line.split('|')
                val pair = parts.getOrNull(0) ?: continue
                val sensitivity = parts.getOrNull(1)?.toDoubleOrNull() ?: 1.0

                val summary = NobitexClient.api.getMarketSummary(pair)
                val change24 = summary.change24 ?: 0.0
                val last = summary.last ?: 0.0
                val volume24 = summary.volume24 ?: 0.0

                val recentPrices = List(24) { idx -> last * (1.0 + (kotlin.math.sin(idx.toDouble())*0.002)) }

                val res = AnalysisEngine.analyze(pair, last, change24, volume24, recentPrices, sensitivity)

                val message = when(res.action) {
                    "BUY" -> "امین، ارز $pair الآن بخر — دلیل: ${res.reason}"
                    "SELL" -> "امین، اکنون $pair را بفروش — دلیل: ${res.reason}"
                    else -> "امین، $pair در محدوده تماشاست — دلیل: ${res.reason}"
                }

                NotificationHelper.postNotification(applicationContext, pair.hashCode(), pair, message)
            }
            return@withContext Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext Result.retry()
        }
    }
}
