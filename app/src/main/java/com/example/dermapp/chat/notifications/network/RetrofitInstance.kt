//package com.example.dermapp.chat.notifications.network
//
//import com.example.dermapp.MyApplication
//import com.example.dermapp.chat.notifications.AuthManager
//import okhttp3.OkHttpClient
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import okhttp3.Interceptor
//
//object RetrofitInstance {
//
//    private val authInterceptor = Interceptor { chain ->
//        val token = AuthManager.getBearerToken(MyApplication.appContext) // Pobierz token w kontekście aplikacji
//        val request = chain.request().newBuilder()
//            .addHeader("Authorization", "Bearer $token")
//            .build()
//        chain.proceed(request)
//    }
//
//    private val client = OkHttpClient.Builder()
//        .addInterceptor(authInterceptor)
//        .build()
//
//    private val retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl("https://fcm.googleapis.com/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(client)
//            .build()
//    }
//
//    val api: NotificationAPI by lazy {
//        retrofit.create(NotificationAPI::class.java)
//    }
//}
