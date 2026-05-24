package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Services ---
    @Query("SELECT * FROM services ORDER BY id ASC")
    fun getAllServicesFlow(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE status = 'active' ORDER BY id ASC")
    fun getActiveServicesFlow(): Flow<List<ServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteService(id: Int)

    @Query("SELECT COUNT(*) FROM services")
    suspend fun getServicesCount(): Int


    // --- Exam/Bharti Posts ---
    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAllPostsFlow(): Flow<List<AppPostEntity>>

    @Query("SELECT * FROM posts WHERE status = 'active' ORDER BY id DESC")
    fun getActivePostsFlow(): Flow<List<AppPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: AppPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<AppPostEntity>)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePost(id: Int)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getPostsCount(): Int


    // --- Customer Applications ---
    @Query("SELECT * FROM applications ORDER BY submissionDate DESC")
    fun getAllApplicationsFlow(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE id = :id")
    suspend fun getApplicationById(id: String): ApplicationEntity?

    @Query("SELECT * FROM applications WHERE mobile = :mobile OR id = :id ORDER BY submissionDate DESC")
    suspend fun trackApplication(mobile: String, id: String): List<ApplicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity)

    @Query("DELETE FROM applications WHERE id = :id")
    suspend fun deleteApplication(id: String)

    @Query("SELECT COUNT(*) FROM applications")
    suspend fun getApplicationsCount(): Int


    // --- Public Broadcast Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAllNotifications()


    // --- App Settings Configuration ---
    @Query("SELECT * FROM app_config WHERE id = 1")
    fun getConfigFlow(): Flow<AppConfigEntity?>

    @Query("SELECT * FROM app_config WHERE id = 1")
    suspend fun getConfigDirect(): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfigEntity)
}
