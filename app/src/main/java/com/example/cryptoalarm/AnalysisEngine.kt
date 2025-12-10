package com.example.cryptoalarm

import kotlin.math.*
import android.util.Log

data class AnalysisResult(val action: String, val score: Double, val reason: String)

object AnalysisEngine {
    fun analyze(pair: String, lastPrice: Double, change24: Double, volume24: Double, recentPrices: List<Double>, sensitivity: Double = 1.0): AnalysisResult {
        val mean = if (recentPrices.isEmpty()) lastPrice else recentPrices.average()
        val variance = if (recentPrices.isEmpty()) 0.0 else recentPrices.map { (it - mean)*(it - mean) }.average()
        val volatility = sqrt(variance)
        val momentum = if (recentPrices.size >= 3) recentPrices.takeLast(3).average() - recentPrices.takeLast(min(10,recentPrices.size)).average() else 0.0

        val scoreChange = -change24 / 10.0
        val scoreMomentum = -momentum / (mean+1e-9)
        val scoreVolatility = -volatility / (mean+1e-9)
        val scoreVolume = (if (volume24>0) log10(volume24+1.0) else 0.0)/5.0

        val wChange = 0.5 * sensitivity
        val wMomentum = 0.2 * sensitivity
        val wVol = 0.2 * sensitivity
        val wVolu = 0.1 * sensitivity

        val score = wChange*scoreChange + wMomentum*scoreMomentum + wVol*scoreVolatility + wVolu*scoreVolume

        val action = when {
            score >= 0.5 -> "SELL"
            score <= -0.5 -> "BUY"
            else -> "HOLD"
        }

        val reason = "score=${String.format("%.2f",score)}, change24=${String.format("%.2f",change24)}, vol=${String.format("%.2f",volume24)}"
        Log.i("AnalysisEngine","$pair -> $reason -> $action")
        return AnalysisResult(action, score, reason)
    }
}
