package app.ilecto.agenda.core.network.di

import app.ilecto.agenda.core.network.google.GoogleCalendarApi
import app.ilecto.agenda.core.network.outlook.OutlookGraphApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GoogleRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OutlookRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        // No auth interceptor here on purpose: Google/Outlook access tokens are short-lived and
        // fetched fresh per-request by GoogleTokenProvider/OutlookTokenProvider, then passed
        // explicitly as an Authorization header argument rather than attached globally.
        .build()

    @Provides
    @Singleton
    @GoogleRetrofit
    fun provideGoogleRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    @OutlookRetrofit
    fun provideOutlookRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = Retrofit.Builder()
        .baseUrl("https://graph.microsoft.com/")
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides
    @Singleton
    fun provideGoogleCalendarApi(@GoogleRetrofit retrofit: Retrofit): GoogleCalendarApi = retrofit.create()

    @Provides
    @Singleton
    fun provideOutlookGraphApi(@OutlookRetrofit retrofit: Retrofit): OutlookGraphApi = retrofit.create()
}
