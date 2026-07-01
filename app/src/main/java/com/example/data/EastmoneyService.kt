package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class EastmoneySectorItem(
    @Json(name = "f12") val code: String?,
    @Json(name = "f14") val name: String?,
    @Json(name = "f3") val changePercent: Double?,
    @Json(name = "f62") val netFlow: Double?, // in Yuan
    @Json(name = "f128") val leadingStockName: String?,
    @Json(name = "f136") val leadingStockChange: Double?,
    @Json(name = "f204") val inflowAmount: Double?,
    @Json(name = "f205") val outflowAmount: Double?
)

data class EastmoneyData(
    @Json(name = "total") val total: Int?,
    @Json(name = "diff") val diff: List<EastmoneySectorItem>?
)

data class EastmoneySectorResponse(
    @Json(name = "data") val data: EastmoneyData?
)

interface EastmoneyApiService {
    @GET("api/qt/clist/get")
    suspend fun getSectorFundFlow(
        @Query("pn") pageNumber: Int = 1,
        @Query("pz") pageSize: Int = 100,
        @Query("po") sortOrder: Int = 1,
        @Query("np") np: Int = 1,
        @Query("ut") token: String = "b2884a397a5973c537589753c24058d0",
        @Query("fltt") fltt: Int = 2,
        @Query("invt") invt: Int = 2,
        @Query("fid") fid: String = "f62",
        @Query("fs", encoded = true) filter: String = "m:90+t:2+f:!50",
        @Query("fields") fields: String = "f12,f14,f3,f62,f128,f136,f204,f205"
    ): EastmoneySectorResponse
}

object EastmoneyRetrofitClient {
    private const val BASE_URL = "https://push2.eastmoney.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val service: EastmoneyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(EastmoneyApiService::class.java)
    }
}
