package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(private val appDao: AppDao) {

    // --- Services ---
    val allServices: Flow<List<ServiceEntity>> = appDao.getAllServicesFlow()
    val activeServices: Flow<List<ServiceEntity>> = appDao.getActiveServicesFlow()

    suspend fun saveService(service: ServiceEntity) = appDao.insertService(service)
    suspend fun deleteService(id: Int) = appDao.deleteService(id)

    // --- Posts (Bharti) ---
    val allPosts: Flow<List<AppPostEntity>> = appDao.getAllPostsFlow()
    val activePosts: Flow<List<AppPostEntity>> = appDao.getActivePostsFlow()

    suspend fun savePost(post: AppPostEntity) = appDao.insertPost(post)
    suspend fun deletePost(id: Int) = appDao.deletePost(id)

    // --- Applications ---
    val allApplications: Flow<List<ApplicationEntity>> = appDao.getAllApplicationsFlow()

    suspend fun getApplicationById(id: String): ApplicationEntity? = appDao.getApplicationById(id)

    suspend fun trackApplication(query: String): List<ApplicationEntity> {
        return appDao.trackApplication(query, query)
    }

    suspend fun submitApplication(app: ApplicationEntity) = appDao.insertApplication(app)
    suspend fun deleteApplication(id: String) = appDao.deleteApplication(id)

    suspend fun generateNextApplicationId(): String {
        val count = appDao.getApplicationsCount()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        return "SSE-$year-${String.format("%05d", count + 1)}"
    }

    // --- Notifications ---
    val allNotifications: Flow<List<NotificationEntity>> = appDao.getAllNotificationsFlow()

    suspend fun saveNotification(notif: NotificationEntity) = appDao.insertNotification(notif)
    suspend fun deleteNotification(id: String) = appDao.deleteNotification(id)
    suspend fun clearAllNotifications() = appDao.clearAllNotifications()

    // --- Config / Settings ---
    val appConfig: Flow<AppConfigEntity?> = appDao.getConfigFlow()

    suspend fun getConfigDirect(): AppConfigEntity? = appDao.getConfigDirect()
    suspend fun saveConfig(config: AppConfigEntity) = appDao.insertConfig(config)

    // --- Seed Database ---
    suspend fun checkAndSeedDatabase() {
        // Initialize Config if empty
        if (appDao.getConfigDirect() == null) {
            appDao.insertConfig(SeedData.defaultConfig)
        }
        // Initialize services if empty
        if (appDao.getServicesCount() == 0) {
            appDao.insertServices(SeedData.defaultServices)
        }
        // Initialize posts if empty
        if (appDao.getPostsCount() == 0) {
            appDao.insertPosts(SeedData.defaultPosts)
        }
        // Initialize announcements if empty
        val notifCount = appDao.getAllNotificationsFlow().firstOrNull()?.size ?: 0
        if (notifCount == 0) {
            for (notif in SeedData.defaultNotifications) {
                appDao.insertNotification(notif)
            }
        }
        // Initialize applications if empty
        if (appDao.getApplicationsCount() == 0) {
            for (app in SeedData.defaultApplications) {
                appDao.insertApplication(app)
            }
        }
    }
}
