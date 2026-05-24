package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String,
    val icon: String,
    val description: String,
    val requiredDocs: String, // Split by newline
    val link: String,
    val formUrl: String,
    val price: Int,
    val status: String // "active" or "inactive"
)

@Entity(tableName = "posts")
data class AppPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "bharti", "result", "admit", "answer", "syllabus"
    val date: String,
    val postsCount: String,
    val fees: String,
    val ageLimit: String,
    val qualification: String,
    val startDate: String,
    val lastDate: String,
    val requiredDocs: String,
    val description: String,
    val officialLink: String,
    val applyLink: String,
    val downloadLink: String,
    val expectedDate: String,
    val expectedDateStatus: String, // "soon", "today", "released", "expected"
    val status: String // "active" or "inactive"
)

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey val id: String, // e.g. "SSE-2026-00001"
    val name: String,
    val mobile: String,
    val email: String,
    val serviceName: String,
    val address: String,
    val fatherName: String,
    val aadhaar: String,
    val notes: String,
    val submissionDate: String,
    val status: String, // "pending", "approved", "rejected", "correction"
    val paymentStatus: String, // "pending", "paid", "refunded"
    val paymentMethod: String, // "upi", "cash", "later"
    val feesAmount: Int,
    val remarks: String,
    val uploadedFilesJson: String, // Comma separated or simple serialization for demo files
    val adminFilesJson: String, // Simple list of files sent by admin
    val correctionNote: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String, // "info", "important", "offer", "camp"
    val link: String,
    val date: String,
    val timestamp: Long
)

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val upiId: String,
    val shopName: String,
    val qrCodeBase64: String, // Base64 of QR code image if custom uploaded
    val ownerName: String,
    val phone1: String,
    val phone2: String,
    val workingHours: String,
    val address: String,
    val visitorsTotal: Int = 0,
    val visitorsToday: Int = 0,
    val chatbotQueries: Int = 0,
    val lastVisitorDate: String = "",
    val email: String = "shreeemitra010@gmail.com",
    val mapLink: String = "https://maps.app.goo.gl/3XgDmsZq8P98v6qTA",
    val termsAndConditions: String = "1. यह ऐप राजस्थान सरकार की विभिन्न सेवाओं और भर्तियों की जानकारी प्रदान करता है।\n2. हम किसी भी सरकारी इकाई का प्रतिनिधित्व नहीं करते हैं और न ही किसी आधिकारिक क्षमता में काम करते हैं।\n3. ऐप पर साझा की गई जानकारी केवल आम नागरिक की मदद के लिए है। कृपया आधिकारिक वेबसाइट से भी जांच करें।\n4. किसी भी सेवा के आवश्यक दस्तावेजों को ऐप में देखकर आप सीधे हमसे संपर्क कर सेवा प्राप्त कर सकते हैं।",
    val privacyPolicy: String = "1. हम यूजर की निजता और गोपनीयता का पूरा सम्मान करते हैं।\n2. इस एप्लीकेशन में किसी भी प्रकार का कोई अनाधिकृत यूजर डेटा एकत्र या स्टोर नहीं किया जाता है।\n3. हम केवल एप्लीकेशन पर आने वाले विज़िटर्स (पार्सल संख्या) और चैट के सामान्य आँकड़े गिनते हैं ताकि सेवा को बेहतर किया जा सके।"
)
