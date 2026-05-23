package com.example.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import java.util.concurrent.TimeUnit

interface SyncApi {
    // We target a highly unique key in kvdb.io public sandbox bucket so it behaves as our isolated shared cloud database
    @GET("sandbox/sandhani_donors_secret_v2_918231")
    suspend fun getDonors(): List<Donor>

    @PUT("sandbox/sandhani_donors_secret_v2_918231")
    suspend fun saveDonors(@Body donors: List<Donor>): String

    companion object {
        private val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()

        private val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()

        val instance: SyncApi by lazy {
            Retrofit.Builder()
                .baseUrl("https://kvdb.io/")
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(SyncApi::class.java)
        }
    }
}
