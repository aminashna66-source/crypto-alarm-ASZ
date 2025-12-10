package com.example.cryptoalarm

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createChannel(this)
        setContent { AppContent(this) }
        scheduleWorker(this)
    }
}

@Composable
fun AppContent(context: Context) {
    val prefs = context.getSharedPreferences("watchlist", Context.MODE_PRIVATE)
    var selected by remember { mutableStateOf("") }
    var sensitivity by remember { mutableStateOf("1.0") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Crypto Alarm - Nobitex (Advanced)", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Select from Nobitex markets (e.g., XRPIRT, BTCIRT)") 
            OutlinedTextField(value = selected, onValueChange = { selected = it }, label = { Text("Market e.g. XRPIRT") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = sensitivity, onValueChange = { sensitivity = it }, label = { Text("Sensitivity multiplier (default 1.0)") })
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                val entry = "${selected.trim()}|${sensitivity.toFloatOrNull()?:1.0f}"
                val list = prefs.getString("coins", "") ?: ""
                prefs.edit().putString("coins", if (list.isEmpty()) entry else list + "\n" + entry).apply()
            }) {
                Text("Add to Nobitex watchlist & start")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Watchlist:")
            val coins = prefs.getString("coins", "") ?: ""
            coins.split('\n').forEach { Text(it) }
        }
    }
}

fun scheduleWorker(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val request = PeriodicWorkRequestBuilder<AdvancedCryptoWorker>(15, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork("crypto_watch_worker", ExistingPeriodicWorkPolicy.REPLACE, request)
}
