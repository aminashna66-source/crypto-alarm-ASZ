package com.example.cryptoalarm

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

data class MarketSummary(val pair: String?, val last: Double?, val change24: Double?, val volume24: Double?)
interface NobitexApi {
    @GET("v2/market/marketsummary/{pair}")
    suspend fun getMarketSummary(@Path("pair") pair: String): MarketSummary
    @GET("v2/market/markets")
    suspend fun getMarkets(): List<Map<String,Any>>
}

object NobitexClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.nobitex.ir/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
    val api: NobitexApi = retrofit.create(NobitexApi::class.java)
}
