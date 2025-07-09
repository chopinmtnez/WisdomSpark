package com.albertowisdom.wisdomspark.di

import com.albertowisdom.wisdomspark.data.remote.GoogleSheetsApi
import com.albertowisdom.wisdomspark.data.remote.RemoteConfig
import com.albertowisdom.wisdomspark.data.remote.LogConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo de Hilt para dependencias de red y Google Sheets
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (LogConfig.ENABLE_NETWORK_LOGGING) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(RemoteConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(RemoteConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(RemoteConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("User-Agent", "WisdomSpark-Android/1.0")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GoogleSheetsApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideGoogleSheetsApi(retrofit: Retrofit): GoogleSheetsApi {
        return retrofit.create(GoogleSheetsApi::class.java)
    }
}
