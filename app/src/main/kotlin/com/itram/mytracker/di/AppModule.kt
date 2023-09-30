package com.itram.mytracker.di

import android.content.Context
import androidx.room.Room
import com.itram.mytracker.db.RunningDatabase
import com.itram.mytracker.other.Constants.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        context = app,
        klass = RunningDatabase::class.java,
        name = RUNNING_DATABASE_NAME
    ).build()


    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

}