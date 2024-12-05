package com.example.dermapp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.firestore.DocumentReference

/**
 * Observer to manage the user's online status in Firestore based on app lifecycle events.
 *
 * @param userRef A Firestore DocumentReference pointing to the user's document.
 */
class AppLifecycleObserver(private val userRef: DocumentReference) : LifecycleObserver {

    /**
     * Sets the user's online status to `true` when the app enters the foreground.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onEnterForeground() {
        userRef.update("isOnline", true)
    }

    /**
     * Sets the user's online status to `false` when the app enters the background.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onEnterBackground() {
        userRef.update("isOnline", false)
    }
}
