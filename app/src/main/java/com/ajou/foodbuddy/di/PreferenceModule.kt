package com.ajou.foodbuddy.di

import android.content.Context
import android.content.SharedPreferences
import com.ajou.foodbuddy.R
import com.ajou.foodbuddy.data.db.preference.PreferenceManager
import com.ajou.foodbuddy.data.db.preference.SharedPreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SharedPreferenceManagerModule {

    @Singleton
    @Binds
    abstract fun bindPreferenceManager(impl: SharedPreferenceManager): PreferenceManager
}

@InstallIn(SingletonComponent::class)
@Module
object SharedPreferenceModule {

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(R.string.shared_preferences.toString(), Context.MODE_PRIVATE)
    }
}