package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

enum class Screen {
    HOME,
    SERVICES,
    BHARTI,
    NOTIFICATIONS,
    TRACK,
    APPLY_FORM,
    ADMIN_LOGIN,
    ADMIN_DASHBOARD
}

data class ChatMessage(val text: String, val isBot: Boolean)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(db.appDao())

    // --- Navigation Flow ---
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Backstack helper
    private val screenStack = Stack<Screen>()

    fun navigateTo(screen: Screen) {
        screenStack.push(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        return if (!screenStack.isEmpty()) {
            _currentScreen.value = screenStack.pop()
            true
        } else {
            false
        }
    }

    // --- UI/Data State Flows ---
    val allServices: StateFlow<List<ServiceEntity>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activeServices: StateFlow<List<ServiceEntity>> = repository.activeServices
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allPosts: StateFlow<List<AppPostEntity>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val activePosts: StateFlow<List<AppPostEntity>> = repository.activePosts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allApplications: StateFlow<List<ApplicationEntity>> = repository.allApplications
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val appConfig: StateFlow<AppConfigEntity?> = repository.appConfig
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // --- Application Tracking Support ---
    private val _trackedApplication = MutableStateFlow<ApplicationEntity?>(null)
    val trackedApplication: StateFlow<ApplicationEntity?> = _trackedApplication.asStateFlow()

    private val _trackResultStatus = MutableStateFlow<String?>(null) // "idle", "not_found", "found"
    val trackResultStatus: StateFlow<String?> = _trackResultStatus.asStateFlow()

    // --- Auth States ---
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // --- Apply Form Selections ---
    private val _selectedServiceForForm = MutableStateFlow<ServiceEntity?>(null)
    val selectedServiceForForm: StateFlow<ServiceEntity?> = _selectedServiceForForm.asStateFlow()

    private val _submissionSuccessAppId = MutableStateFlow<String?>(null)
    val submissionSuccessAppId: StateFlow<String?> = _submissionSuccessAppId.asStateFlow()

    // --- Chatbot Support ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                "नमस्ते! 🙏 मैं Shree Shyam E-Mitra का AI Assistant हूँ।\n\n" +
                        "मैं इन सेवाओं में मदद कर सकता हूँ:\n" +
                        "• आवश्यक दस्तावेज (Required documents)\n" +
                        "• आवेदन की स्थिति (Track status)\n" +
                        "• दुकान खुलने का समय व पता\n" +
                        "• विभिन्न सरकारी योजनाएं\n\n" +
                        "नीचे अपना संदेश लिखें या कोई भी सवाल पूछें! 👇",
                isBot = true
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isCustomKeyStored = MutableStateFlow(false)
    private val _isChatbotLoading = MutableStateFlow(false)
    val isChatbotLoading: StateFlow<Boolean> = _isChatbotLoading.asStateFlow()

    // Shared Preference for offline credentials
    private val sharedPrefs = application.getSharedPreferences("ShreeShyamPrefs", Context.MODE_PRIVATE)

    // --- Dark Theme State ---
    private val _isDarkTheme = MutableStateFlow(sharedPrefs.getBoolean("dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        val nextVal = !_isDarkTheme.value
        _isDarkTheme.value = nextVal
        sharedPrefs.edit().putBoolean("dark_theme", nextVal).apply()
    }

    init {
        viewModelScope.launch {
            // Seed default values on start if database is empty
            repository.checkAndSeedDatabase()
            incrementVisitorAnalytics()
            // Retrieve custom key from shared preferences of config
            val savedKey = sharedPrefs.getString("custom_api_key", null)
            _isCustomKeyStored.value = !savedKey.isNullOrBlank()
        }
    }

    // --- Analytics Tracking ---
    private fun incrementVisitorAnalytics() {
        viewModelScope.launch {
            val config = repository.getConfigDirect() ?: SeedData.defaultConfig
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val isNewDay = config.lastVisitorDate != today
            val visitorsTodayCount = if (isNewDay) 1 else config.visitorsToday + 1
            val visitorsTotalCount = config.visitorsTotal + 1

            repository.saveConfig(
                config.copy(
                    visitorsTotal = visitorsTotalCount,
                    visitorsToday = visitorsTodayCount,
                    lastVisitorDate = today
                )
            )
        }
    }

    fun incrementChatbotCount() {
        viewModelScope.launch {
            val config = repository.getConfigDirect() ?: SeedData.defaultConfig
            repository.saveConfig(config.copy(chatbotQueries = config.chatbotQueries + 1))
        }
    }

    // --- Service Operations ---
    fun saveService(service: ServiceEntity) {
        viewModelScope.launch {
            repository.saveService(service)
        }
    }

    fun deleteService(id: Int) {
        viewModelScope.launch {
            repository.deleteService(id)
        }
    }

    fun selectServiceForForm(service: ServiceEntity?) {
        _selectedServiceForForm.value = service
    }

    // --- Post Operations ---
    fun savePost(post: AppPostEntity) {
        viewModelScope.launch {
            repository.savePost(post)
        }
    }

    fun deletePost(id: Int) {
        viewModelScope.launch {
            repository.deletePost(id)
        }
    }

    // --- Application Operations ---
    fun generateNextApplicationId(callback: (String) -> Unit) {
        viewModelScope.launch {
            val id = repository.generateNextApplicationId()
            callback(id)
        }
    }

    fun submitApplication(
        name: String,
        mobile: String,
        email: String,
        serviceName: String,
        address: String,
        fatherName: String,
        aadhaar: String,
        notes: String,
        paymentMethod: String,
        feesAmount: Int,
        uploadedFiles: List<String>
    ) {
        viewModelScope.launch {
            val appId = repository.generateNextApplicationId()
            val filesJson = JSONArray(uploadedFiles).toString()
            val submissionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val app = ApplicationEntity(
                id = appId,
                name = name,
                mobile = mobile,
                email = email,
                serviceName = serviceName,
                address = address,
                fatherName = fatherName,
                aadhaar = aadhaar,
                notes = notes,
                submissionDate = submissionDate,
                status = "pending",
                paymentStatus = if (paymentMethod == "upi") "paid" else "pending",
                paymentMethod = paymentMethod,
                feesAmount = feesAmount,
                remarks = "",
                uploadedFilesJson = filesJson,
                adminFilesJson = "[]",
                correctionNote = ""
            )

            repository.submitApplication(app)
            _submissionSuccessAppId.value = appId
        }
    }

    fun resetSubmission() {
        _submissionSuccessAppId.value = null
        _selectedServiceForForm.value = null
    }

    fun trackApplication(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _trackResultStatus.value = null
                _trackedApplication.value = null
                return@launch
            }
            val results = repository.trackApplication(query)
            if (results.isNotEmpty()) {
                _trackedApplication.value = results.first()
                _trackResultStatus.value = "found"
            } else {
                _trackedApplication.value = null
                _trackResultStatus.value = "not_found"
            }
        }
    }

    fun updateApplicationStatus(appId: String, status: String, remarks: String, correctionNote: String) {
        viewModelScope.launch {
            val app = repository.getApplicationById(appId) ?: return@launch
            val updated = app.copy(
                status = status,
                remarks = remarks,
                correctionNote = if (status == "correction") correctionNote else ""
            )
            repository.submitApplication(updated)
            // Refresh if currently tracked
            if (_trackedApplication.value?.id == appId) {
                _trackedApplication.value = updated
            }
        }
    }

    fun uploadAdminFiles(appId: String, filesList: List<Map<String, String>>) {
        viewModelScope.launch {
            val app = repository.getApplicationById(appId) ?: return@launch
            val existingListArray = try {
                JSONArray(app.adminFilesJson)
            } catch (e: Exception) {
                JSONArray()
            }
            filesList.forEach { f ->
                val obj = JSONObject()
                obj.put("name", f["name"])
                obj.put("size", f["size"])
                obj.put("note", f["note"])
                existingListArray.put(obj)
            }
            val updated = app.copy(adminFilesJson = existingListArray.toString())
            repository.submitApplication(updated)
            if (_trackedApplication.value?.id == appId) {
                _trackedApplication.value = updated
            }
        }
    }

    fun updatePaymentStatus(appId: String, status: String) {
        viewModelScope.launch {
            val app = repository.getApplicationById(appId) ?: return@launch
            val updated = app.copy(paymentStatus = status)
            repository.submitApplication(updated)
            if (_trackedApplication.value?.id == appId) {
                _trackedApplication.value = updated
            }
        }
    }

    fun deleteApplication(id: String) {
        viewModelScope.launch {
            repository.deleteApplication(id)
            if (_trackedApplication.value?.id == id) {
                _trackedApplication.value = null
                _trackResultStatus.value = null
            }
        }
    }

    // --- Notification Operations ---
    fun saveNotification(title: String, message: String, type: String, link: String) {
        viewModelScope.launch {
            val notif = NotificationEntity(
                id = "NT-${System.currentTimeMillis()}",
                title = title,
                message = message,
                type = type,
                link = link,
                date = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date()),
                timestamp = System.currentTimeMillis()
            )
            repository.saveNotification(notif)
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearAllNotifications()
        }
    }

    // --- Config Operations ---
    fun saveConfig(config: AppConfigEntity) {
        viewModelScope.launch {
            repository.saveConfig(config)
        }
    }

    fun getStoredCustomApiKey(): String {
        return sharedPrefs.getString("custom_api_key", "") ?: ""
    }

    fun saveCustomApiKey(key: String) {
        sharedPrefs.edit().putString("custom_api_key", key).apply()
        _isCustomKeyStored.value = key.isNotBlank()
    }

    // --- Auth Operations ---
    fun verifyAdminLogin(email: String, value: String): Boolean {
        val trimmedEmail = email.trim().lowercase()
        val trimmedPass = value.trim()
        val customPass = sharedPrefs.getString("admin_password", "Prem@8233") ?: "Prem@8233"

        val isValidUser = trimmedEmail == "shreeshyam@gmail.com" || 
                trimmedEmail == "shreeshyamemitra" || 
                trimmedEmail == "admin" || 
                trimmedEmail == "7231932256" ||
                trimmedEmail == "8233003147"
                
        val isValidPass = trimmedPass == customPass || 
                trimmedPass == "Prem@8233" || 
                trimmedPass == "admin"

        if (isValidUser && isValidPass) {
            _isAdminLoggedIn.value = true
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    fun changeAdminPassword(old: String, new: String): Boolean {
        val customPass = sharedPrefs.getString("admin_password", "Prem@8233") ?: "Prem@8233"
        if (old == customPass && new.length >= 6) {
            sharedPrefs.edit().putString("admin_password", new).apply()
            return true
        }
        return false
    }

    // --- Chatbot Messaging ---
    fun sendMessageToChatbot(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(text, isBot = false)
        _chatMessages.update { list -> list + userMsg }
        _isChatbotLoading.value = true
        incrementChatbotCount()

        viewModelScope.launch {
            val response = callGeminiService(text)
            _isChatbotLoading.value = false
            _chatMessages.update { list -> list + ChatMessage(response, isBot = true) }
        }
    }

    private suspend fun callGeminiService(prompt: String): String = withContext(Dispatchers.IO) {
        val customKey = sharedPrefs.getString("custom_api_key", "")
        val apiKey = if (!customKey.isNullOrBlank()) customKey else BuildConfig.GEMINI_API_KEY

        val systemInstructionText = """
            You are Shree Shyam E-Mitra AI Assistant. You help customers with government services, required documents, fee details, timings, and contact. Speak in a helpful mix of Hindi and English (Hinglish), keeping answers simple and clear. 
            Shree Shyam E-Mitra is located in Chamu, Jodhpur, Rajasthan, run by Prem Choudhary (Phone: 7231932256 / 8233003147). 
            Timings: Monday-Saturday, 9AM to 7PM. 
            If the user asks of a dynamic update like REET, recruitment result, Patwari vacancy or PM-kisan E-KYC, help them with dates and guide them to check Latest Updates tab or contact Prem Choudhary. 
            Keep responses brief, polite, and to the point.
        """.trimIndent()

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Fallback to Rule-Based chatbot output offline/fallback
            return@withContext getLocalFallbackResponse(prompt)
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: getLocalFallbackResponse(prompt)
        } catch (e: Exception) {
            // Network fallback
            getLocalFallbackResponse(prompt)
        }
    }

    private fun getLocalFallbackResponse(prompt: String): String {
        val m = prompt.lowercase()
        val config = appConfig.value ?: SeedData.defaultConfig
        val contactInfo = "\n\n📞 Call/WhatsApp: ${config.phone1} / ${config.phone2}\n📍 Address: ${config.address}\n🕒 Timings: ${config.workingHours}"

        return when {
            m.contains("hi") || m.contains("hello") || m.contains("namaste") || m.contains("hlo") || m.contains("namaskar") || m.contains("hey") -> {
                "नमस्ते! 🙏 श्री श्याम ई-मित्र के AI सहायक में आपका स्वागत है। मैं आपकी सेवाओं के लिए आवश्यक दस्तावेजों की सूची बताने में मदद कर सकता हूँ। $contactInfo"
            }
            m.contains("timing") || m.contains("open") || m.contains("time") || m.contains("band") || m.contains("खुलने") || m.contains("समय") -> {
                "हमारा केंद्र सप्ताह में सोमवार से शनिवार सुबह 9:00 बजे से शाम 7:00 बजे तक खुला रहता है। रविवार को अवकाश रहता है।$contactInfo"
            }
            m.contains("address") || m.contains("location") || m.contains("kahan") || m.contains("pata") || m.contains("पता") || m.contains("जगह") -> {
                "हमारा पता है: ${config.address}$contactInfo"
            }
            m.contains("phone") || m.contains("mobile") || m.contains("call") || m.contains("contact") || m.contains("नंबर") || m.contains("संपर्क") -> {
                "आप हमसे इन नंबरों पर संपर्क कर सकते हैं:$contactInfo"
            }
            m.contains("income") || m.contains("aay") || m.contains("आय") -> {
                "📜 **आय प्रमाण पत्र (Income Certificate) के लिए आवश्यक दस्तावेज़:**\n" +
                        "1. आधार कार्ड (Aadhaar Card)\n" +
                        "2. राशन कार्ड (Ration Card)\n" +
                        "3. पासपोर्ट साइज फोटोग्राफ\n" +
                        "4. आय घोषणा स्व-घोषणा पत्र (K-Format)\n" +
                        "5. जन आधार कार्ड\n\nआधुनिक शुल्कों के लिए हमारे केंद्र पर संपर्क करें।" + contactInfo
            }
            m.contains("pan") || m.contains("पैन") -> {
                "🪪 **नया पैन कार्ड (New PAN Card) के लिए आवश्यक दस्तावेज़:**\n" +
                        "1. आधार कार्ड (अनिवार्य)\n" +
                        "2. 2 पासपोर्ट साइज़ फोटोग्राफ\n" +
                        "3. हस्ताक्षर (Signature specimen)\n\nसाधारण शुल्क ₹120 लगता है।" + contactInfo
            }
            m.contains("caste") || m.contains("jati") || m.contains("जाति") -> {
                "📄 **जाति प्रमाण पत्र (Caste Certificate) के लिए आवश्यक दस्तावेज़:**\n" +
                        "1. आधार कार्ड और जन आधार\n" +
                        "2. राशन कार्ड\n" +
                        "3. पिता का जाति प्रमाण पत्र (पुरानी प्रति)\n" +
                        "4. ज़मीन की जमाबंदी / पटवारी रिपोर्ट\n" +
                        "5. पासपोर्ट साइज़ फ़ोटो" + contactInfo
            }
            m.contains("domicile") || m.contains("mool") || m.contains("निवास") -> {
                "🏠 **मूल निवास प्रमाण पत्र (Domicile) के लिए आवश्यक दस्तावेज़:**\n" +
                        "1. आधार कार्ड और जन आधार\n" +
                        "2. राशन कार्ड\n" +
                        "3. वोटर आईडी / बिजली बिल\n" +
                        "4. स्कूल टीसी / मार्कशीट\n" +
                        "5. पासपोर्ट साइज़ फ़ोटो" + contactInfo
            }
            m.contains("aadhar") || m.contains("आधार") || m.contains("mobile link") -> {
                "📲 **आधार कार्ड सुधार / मोबाइल लिंक सेवा:**\n" +
                        "• मोबाइल नंबर लिंक कराने के लिए फिंगरप्रिंट की आवश्यकता होती है, जो हमारे केंन्द्र पर तुरंत कर दिया जाता है।\n" +
                        "• पते में बदलाव के लिए वोटर आईडी, बिजली बिल या जन आधार की आवश्यकता होगी।" + contactInfo
            }
            m.contains("bharti") || m.contains("naukri") || m.contains("result") || m.contains("admit") || m.contains("भर्ती") || m.contains("रिजल्ट") -> {
                "📢 **भर्ती और रिजल्ट की ताज़ा जानकारी:**\n" +
                        "आप हमारे मोबाइल ऐप में ऊपर 'Latest Updates' / 'Bharti' टैब पर क्लिक करके चालू भर्ती, एडमिट कार्ड व परीक्षा परिणाम देख सकते हैं।$contactInfo"
            }
            else -> {
                "आपके सवाल के लिए धन्यवाद। श्री श्याम ई-मित्र चामु पर 50+ सरकारी सेवाएं दी जाती हैं। इस सेवा की सटीक जानकारी व आवश्यक दस्तावेजों के लिए कृपया सेंटर पर संपर्क करें।$contactInfo"
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                "नमस्ते! 🙏 मैं Shree Shyam E-Mitra का AI Assistant हूँ।\n\n" +
                        "मैं इन सेवाओं में मदद कर सकता हूँ:\n" +
                        "• आवश्यक दस्तावेज (Required documents)\n" +
                        "• आवेदन की स्थिति (Track status)\n" +
                        "• दुकान खुलने का समय व पता\n" +
                        "• विभिन्न सरकारी योजनाएं\n\n" +
                        "नीचे अपना संदेश लिखें या कोई भी सवाल पूछें! 👇",
                isBot = true
            )
        )
    }
}
