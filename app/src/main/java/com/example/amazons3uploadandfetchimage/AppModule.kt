package com.example.amazons3uploadandfetchimage


import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

//    @Singleton
//    @Provides
//    fun provideS3Helper(context: Context): S3Helper {
//        return S3Helper(context)
//    }

    @Provides
    fun bindContext(application: MyApp): Context = application.applicationContext
}
