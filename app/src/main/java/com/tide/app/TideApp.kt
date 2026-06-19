package com.tide.app

import android.app.Application
import android.content.Context
import com.tide.app.data.AppCatalog
import com.tide.app.data.BlockRepository
import com.tide.app.data.FocusRepository
import com.tide.app.data.UsageStatsRepository
import com.tide.app.data.db.TideDatabase
import com.tide.app.data.prefs.SettingsRepository

/**
 * Hand-rolled dependency container. The app is small enough that a DI framework would add
 * more ceremony than value; everything here is a cheap lazy singleton.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: TideDatabase by lazy { TideDatabase.get(appContext) }
    val settings: SettingsRepository by lazy { SettingsRepository(appContext) }
    val appCatalog: AppCatalog by lazy { AppCatalog(appContext) }
    val usageStats: UsageStatsRepository by lazy {
        UsageStatsRepository(appContext, database.dailyUsageDao())
    }
    val blocks: BlockRepository by lazy {
        BlockRepository(database.shieldDao(), database.scheduleDao())
    }
    val focus: FocusRepository by lazy {
        FocusRepository(appContext, settings, database.focusSessionDao())
    }
}

class TideApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

val Context.appContainer: AppContainer
    get() = (applicationContext as TideApp).container
