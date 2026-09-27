package xyz.retroforge.velora.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // TODO: move to BuildConfig / a settings screen if you ever point this
    // at a different host (staging, self-hosted instance, etc).
    private const val BASE_URL = "https://retroforge.xyz/api/"

    lateinit var cookieJar: PersistentCookieJar
        private set

    val service: ApiService by lazy { build() }

    fun init(context: Context) {
        if (::cookieJar.isInitialized) return
        cookieJar = PersistentCookieJar(context)
    }

    private fun build(): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
