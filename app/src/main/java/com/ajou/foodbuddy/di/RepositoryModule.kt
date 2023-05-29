package com.ajou.foodbuddy.di

import com.ajou.foodbuddy.data.repository.ChatRepository
import com.ajou.foodbuddy.data.repository.ChatRepositoryImpl
import com.ajou.foodbuddy.data.repository.UserRepository
import com.ajou.foodbuddy.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@InstallIn(ViewModelComponent::class)
@Module
abstract class ChatRepositoryModule {

    @ViewModelScoped
    @Binds
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository
}

@InstallIn(ViewModelComponent::class)
@Module
abstract class UserRepositoryModule {

    @ViewModelScoped
    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}