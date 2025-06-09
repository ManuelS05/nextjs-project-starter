package com.example.taskmaster.di

import android.content.Context
import com.example.taskmaster.data.AppDatabase
import com.example.taskmaster.data.dao.ProjectDao
import com.example.taskmaster.data.dao.TaskDao
import com.example.taskmaster.data.dao.UserDao
import com.example.taskmaster.data.repository.ProjectRepository
import com.example.taskmaster.data.repository.TaskRepository
import com.example.taskmaster.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = AppDatabase.getInstance(context)

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    @Singleton
    fun provideProjectDao(database: AppDatabase): ProjectDao = database.projectDao()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao
    ): TaskRepository = TaskRepository(taskDao)

    @Provides
    @Singleton
    fun provideProjectRepository(
        projectDao: ProjectDao
    ): ProjectRepository = ProjectRepository(projectDao)

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        firebaseAuth: FirebaseAuth
    ): UserRepository = UserRepository(userDao, firebaseAuth)
}
