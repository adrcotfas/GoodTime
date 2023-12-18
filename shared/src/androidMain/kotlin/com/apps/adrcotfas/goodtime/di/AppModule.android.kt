package com.apps.adrcotfas.goodtime.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apps.adrcotfas.goodtime.data.local.DatabaseDriverFactory
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DatabaseDriverFactory> {
        DatabaseDriverFactory(get<Context>())
    }
    single<DataStore<Preferences>> {
        getDataStore(
            producePath = { get<Context>().filesDir.resolve(dataStoreFileName).absolutePath }
        )
    }
}