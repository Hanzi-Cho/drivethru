package com.hanzi.drivethru.di

import android.content.Context

object DriveThruRuntime {
    @Volatile
    private var appContainer: AppContainer? = null

    fun get(context: Context): AppContainer {
        return appContainer ?: synchronized(this) {
            appContainer ?: AppContainer(context.applicationContext).also { created ->
                appContainer = created
            }
        }
    }
}
