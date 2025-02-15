package com.tolstoy.zurichat.di

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.tolstoy.zurichat.ui.organizations.utils.TOKEN_NAME
import com.tolstoy.zurichat.data.remoteSource.ChatsService
import com.tolstoy.zurichat.data.remoteSource.FilesService
import com.tolstoy.zurichat.data.remoteSource.Retrofit as RetrofitBuilder
import com.tolstoy.zurichat.data.remoteSource.UsersService
import com.tolstoy.zurichat.data.remoteSource.RoomService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    @Provides
    fun provideGson(): Gson {
        return Gson().newBuilder().setLenient().create()
    }

    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().also {
            it.level = HttpLoggingInterceptor.Level.BODY
        }
    }



    @Provides
    fun provideClient(interceptor: HttpLoggingInterceptor, sharedPreferences : SharedPreferences): OkHttpClient {
        // Add authorization token to the header interceptor
        val headerAuthorization = Interceptor { chain ->
            val request = chain.request().newBuilder()
            sharedPreferences.getString(TOKEN_NAME, null)?.let {
                request.addHeader("Authorization", "Bearer $it")
            }
            chain.proceed(request.build())
        }
        return OkHttpClient.Builder().
        addInterceptor(headerAuthorization).
        addInterceptor(interceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.zuri.chat/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    fun provideUserService(retrofit: Retrofit): UsersService =
        retrofit.create(UsersService::class.java)

    @Provides
    fun provideChatService() =
        RetrofitBuilder.retrofit(ChatsService.BASE_URL).create(ChatsService::class.java)

    @Provides
    fun provideRoomService() =
        RetrofitBuilder.retrofit(RoomService.BASE_URL).create(RoomService::class.java)

    @Provides
    fun provideFileService() =
        RetrofitBuilder.retrofit(FilesService.BASE_URL).create(FilesService::class.java)
}