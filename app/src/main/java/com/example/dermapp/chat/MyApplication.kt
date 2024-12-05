package com.example.dermapp

import android.app.Application
import android.content.Context

/**
 * Custom Application class for global access to the application context.
 * Stores the application context in a companion object for easy access throughout the app.
 */
class MyApplication : Application() {

    companion object {
        /**
         * Holds the global application context.
         * This property is initialized in the [onCreate] method.
         */
        lateinit var appContext: Context
            private set
    }

    /**
     * Initializes the application and sets the global context.
     */
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
