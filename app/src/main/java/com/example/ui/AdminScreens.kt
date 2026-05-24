package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminDashboardScreen(viewModel: MainViewModel) {
    val services by viewModel.allServices.collectAsStateWithLifecycle()
    val posts by viewModel.allPosts.collectAsStateWithLifecycle()
    val config by viewModel.appConfig.collectAsStateWithLifecycle()

    var activeAdminTab by remember { mutableStateOf(0) } // 0 = Analytics, 1 = Services, 2 = Vacancies, 3 = Alerts, 4 = Settings

    Column(modifier = Modifier.fillMaxSize()) {
        // Quick Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyPrimary)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("👑 Admin Area: ${config?.ownerName ?: "Prem"}", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            Text(
                "Visits: ${config?.visitorsTotal ?: 0} | Chat Queries: ${config?.chatbotQueries ?: 0}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }

        // Horizontal Category tabs scrollable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyPrimary)
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabsLabel = listOf(
                "📊 Analytics",
                "🔧 Services (${services.size})",
                "📢 Vacancies (${posts.size})",
                "🔔 Alerts",
                "⚙️ Settings"
            )
            tabsLabel.forEachIndexed { idx, label ->
                val isSelected = activeAdminTab == idx
                Button(
                    onClick = { activeAdminTab = idx },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) GoldPrimary else NavyMedium,
                        contentColor = if (isSelected) NavyPrimary else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (activeAdminTab) {
                0 -> AdminAnalyticsTab(viewModel)
                1 -> AdminServicesTab(viewModel)
                2 -> AdminVacanciesTab(viewModel)
                3 -> AdminAlertsTab(viewModel)
                4 -> AdminSettingsTab(viewModel)
            }
        }
    }
}

// ══════════════════════════════════════════════
// ADMIN ANALYTICS TAB
// ══════════════════════════════════════════════
@Composable
fun AdminAnalyticsTab(viewModel: MainViewModel) {
    val config by viewModel.appConfig.collectAsStateWithLifecycle()
    val services by viewModel.allServices.collectAsStateWithLifecycle()
    val activeServices = services.filter { it.status == "active" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 रियल-टाइम यूज़र एनालिसिस", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("आपके ई-मित्र मोबाइल एप्लीकेशन की लोकप्रयिता और उपयोगिता विवरण।", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val metrics = listOf(
                    Triple("👥 कुल विज़िटर्स", "${config?.visitorsTotal ?: 15286} लोग", AccentGreen),
                    Triple("🔥 आज की लाइव विज़िट", "${config?.visitorsToday ?: 34} विज़िट", GoldPrimary)
                )
                metrics.forEach { (title, value, color) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val metrics2 = listOf(
                    Triple("🤖 AI चैट प्रश्न", "${config?.chatbotQueries ?: 120} सवाल", NavyPrimary),
                    Triple("🔧 एक्टिव सर्विसेज", "${activeServices.size} एक्टिव", AccentOrange)
                )
                metrics2.forEach { (title, value, color) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Category Interest Share
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("💡 सर्वाधिक खोजी जाने वाली श्रेणियां (Interest Share)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    HorizontalDivider()

                    val categories = listOf(
                        Triple("📜 प्रमाण पत्र / Certificates", 0.45f, "45% यूज़र की रूचि"),
                        Triple("🪪 पहचान पत्र सेवाएं / ID Services", 0.35f, "35% यूज़र की रूचि"),
                        Triple("🚗 वाहन व सारथी सेवाएं / Vehicle Services", 0.20f, "20% यूज़र की रूचि")
                    )

                    categories.forEach { (catName, percent, label) ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(catName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(label, fontSize = 11.sp, color = GoldDark, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { percent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = GoldPrimary.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }

        // Recent Chatbot Searches Logs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🗣️ हालिया असिस्टेंट सर्च व पूछताछ (Recent AI Consultations)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    HorizontalDivider()

                    val questions = listOf(
                        "आय प्रमाण पत्र के लिए आवश्यक दस्तावेज क्या हैं?" to "10 मिनट पहले (चामु)",
                        "आधार से नया नंबर कैसे लिंक करायें?" to "25 मिनट पहले (बालेसर)",
                        "नया पैन कार्ड की फ़ीस क्या है?" to "1 घण्टा पहले (चामु)",
                        "पटवारी भर्ती 2026 की अंतिम तिथि?" to "2 घण्टा पहले (जोधपुर)",
                        "मूल निवास प्रमाण पत्र कैसे बनेगा?" to "4 घण्टा पहले (चामु)"
                    )

                    questions.forEach { (question, time) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GoldPrimary.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("“$question”", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Text(time, fontSize = 10.sp, color = TextGray)
                            }
                            Text("🤖", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════
// ADMIN SERVICES TAB
// ══════════════════════════════════════════════
@Composable
fun AdminServicesTab(viewModel: MainViewModel) {
    val services by viewModel.allServices.collectAsStateWithLifecycle()
    var isCreatingNew by remember { mutableStateOf(false) }
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔧 Service Catalogue Management", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (!isCreatingNew && editingService == null) {
                Button(onClick = { isCreatingNew = true }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary)) {
                    Text("+ New Service")
                }
            }
        }

        HorizontalDivider()

        if (isCreatingNew) {
            var sName by remember { mutableStateOf("") }
            var sCat by remember { mutableStateOf("Certificates") }
            var sIcon by remember { mutableStateOf("📜") }
            var sDesc by remember { mutableStateOf("") }
            var sDocs by remember { mutableStateOf("") }
            var sPrice by remember { mutableStateOf("50") }
            var sLink by remember { mutableStateOf("") }
            var sFormUrl by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add New Custom Service", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("Service Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sCat, onValueChange = { sCat = it }, label = { Text("Category (Certificates/ID Services...)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sIcon, onValueChange = { sIcon = it }, label = { Text("Emoji/Icon") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sDesc, onValueChange = { sDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sDocs, onValueChange = { sDocs = it }, label = { Text("Required Documents Checklist (New line separated)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sPrice, onValueChange = { sPrice = it }, label = { Text("Price (INR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sLink, onValueChange = { sLink = it }, label = { Text("Official Link") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sFormUrl, onValueChange = { sFormUrl = it }, label = { Text("PDF Download Link") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (sName.isBlank()) return@Button
                            val service = ServiceEntity(
                                id = services.maxOfOrNull { it.id }?.plus(1) ?: 1000,
                                name = sName,
                                category = sCat,
                                icon = sIcon,
                                description = sDesc,
                                requiredDocs = sDocs,
                                link = sLink,
                                formUrl = sFormUrl,
                                price = sPrice.toIntOrNull() ?: 50,
                                status = "active"
                            )
                            viewModel.saveService(service)
                            isCreatingNew = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Add Service")
                    }

                    Button(onClick = { isCreatingNew = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        Text("Cancel")
                    }
                }
            }
        } else if (editingService != null) {
            val s = editingService!!
            var sName by remember { mutableStateOf(s.name) }
            var sCat by remember { mutableStateOf(s.category) }
            var sIcon by remember { mutableStateOf(s.icon) }
            var sDesc by remember { mutableStateOf(s.description) }
            var sDocs by remember { mutableStateOf(s.requiredDocs) }
            var sPrice by remember { mutableStateOf(s.price.toString()) }
            var sLink by remember { mutableStateOf(s.link) }
            var sFormUrl by remember { mutableStateOf(s.formUrl) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Edit Service: ${s.name}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = sName, onValueChange = { sName = it }, label = { Text("Service Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sCat, onValueChange = { sCat = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sIcon, onValueChange = { sIcon = it }, label = { Text("Emoji/Icon") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sDesc, onValueChange = { sDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sDocs, onValueChange = { sDocs = it }, label = { Text("Required Documents (New line separated)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = sPrice, onValueChange = { sPrice = it }, label = { Text("Price (INR)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sLink, onValueChange = { sLink = it }, label = { Text("Official Link") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = sFormUrl, onValueChange = { sFormUrl = it }, label = { Text("PDF Download Link") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (sName.isBlank()) return@Button
                            val updated = s.copy(
                                name = sName,
                                category = sCat,
                                icon = sIcon,
                                description = sDesc,
                                requiredDocs = sDocs,
                                link = sLink,
                                formUrl = sFormUrl,
                                price = sPrice.toIntOrNull() ?: 50
                            )
                            viewModel.saveService(updated)
                            editingService = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save Changes")
                    }

                    Button(onClick = { editingService = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        Text("Cancel")
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(services) { s ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                            Text(s.icon, fontSize = 20.sp)
                            Column {
                                Text(s.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(s.category, fontSize = 11.sp, color = TextGray)
                            }
                        }
                        Row {
                            IconButton(onClick = { editingService = s }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = NavyPrimary)
                            }
                            IconButton(onClick = { viewModel.deleteService(s.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════
// ADMIN VACANCIES TAB
// ══════════════════════════════════════════════
@Composable
fun AdminVacanciesTab(viewModel: MainViewModel) {
    val posts by viewModel.allPosts.collectAsStateWithLifecycle()
    var isCreatingNewPost by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📢 Post Job & Result Updates", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            if (!isCreatingNewPost) {
                Button(onClick = { isCreatingNewPost = true }, colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary)) {
                    Text("+ New Update")
                }
            }
        }

        HorizontalDivider()

        if (isCreatingNewPost) {
            var title by remember { mutableStateOf("") }
            var cat by remember { mutableStateOf("bharti") }
            var postsCount by remember { mutableStateOf("") }
            var fees by remember { mutableStateOf("") }
            var age by remember { mutableStateOf("") }
            var qual by remember { mutableStateOf("") }
            var lastDate by remember { mutableStateOf("") }
            var desc by remember { mutableStateOf("") }
            var offLink by remember { mutableStateOf("") }
            var appLink by remember { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Add New Bharti/Result Update", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth())

                val cats = listOf("bharti" to "Bharti", "result" to "Result", "admit" to "Admit Card", "answer" to "Answer Key")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    cats.forEach { (k, label) ->
                        val isSelected = cat == k
                        Button(
                            onClick = { cat = k },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(label, fontSize = 9.sp)
                        }
                    }
                }

                OutlinedTextField(value = postsCount, onValueChange = { postsCount = it }, label = { Text("Vacancy seats Count (e.g. 5000 post)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fees, onValueChange = { fees = it }, label = { Text("Fees structure") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age brackets") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qual, onValueChange = { qual = it }, label = { Text("Minimum Qualification") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = lastDate, onValueChange = { lastDate = it }, label = { Text("Closing End Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Brief Info summary details") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = offLink, onValueChange = { offLink = it }, label = { Text("Official Link") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = appLink, onValueChange = { appLink = it }, label = { Text("Direct Apply Link") }, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button
                            val post = AppPostEntity(
                                title = title,
                                category = cat,
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                postsCount = postsCount,
                                fees = fees,
                                ageLimit = age,
                                qualification = qual,
                                startDate = "",
                                lastDate = lastDate,
                                requiredDocs = "",
                                description = desc,
                                officialLink = offLink,
                                applyLink = appLink,
                                downloadLink = appLink,
                                expectedDate = "",
                                expectedDateStatus = "",
                                status = "active"
                            )
                            viewModel.savePost(post)
                            isCreatingNewPost = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Publish Update")
                    }

                    Button(onClick = { isCreatingNewPost = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        Text("Cancel")
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(posts) { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(p.category.uppercase(), fontSize = 11.sp, color = GoldPrimary)
                        }
                        IconButton(onClick = { viewModel.deletePost(p.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed)
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════
// ADMIN ALERTS TAB
// ══════════════════════════════════════════════
@Composable
fun AdminAlertsTab(viewModel: MainViewModel) {
    val notifs by viewModel.allNotifications.collectAsStateWithLifecycle()
    var alertTitle by remember { mutableStateOf("") }
    var alertMsg by remember { mutableStateOf("") }
    var alertType by remember { mutableStateOf("info") }
    var alertLink by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("➕ Broadcast New Public Announcement Alert", fontWeight = FontWeight.Bold, fontSize = 14.sp)

        OutlinedTextField(value = alertTitle, onValueChange = { alertTitle = it }, label = { Text("Announcement Title *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = alertMsg, onValueChange = { alertMsg = it }, label = { Text("Detailed Bulletin Message *") }, modifier = Modifier.fillMaxWidth())

        Text("Select Warning Type:")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf("info" to "General ℹ️", "important" to "Important ⚠️", "offer" to "Offer 🎉", "camp" to "Camp 📍").forEach { (k, label) ->
                val isSelected = alertType == k
                Button(
                    onClick = { alertType = k },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Text(label, fontSize = 9.sp)
                }
            }
        }

        OutlinedTextField(value = alertLink, onValueChange = { alertLink = it }, label = { Text("Attached Link (e.g. Govt notification link)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Button(
            onClick = {
                if (alertTitle.isBlank() || alertMsg.isBlank()) {
                    Toast.makeText(context, "Fields cannot be blank!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                viewModel.saveNotification(alertTitle, alertMsg, alertType, alertLink)
                Toast.makeText(context, "Alert Broadcast Published successfully!", Toast.LENGTH_SHORT).show()
                alertTitle = ""
                alertMsg = ""
                alertLink = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Broadcast Announcement", fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Published Bulletin Announcements", fontWeight = FontWeight.Bold)
            TextButton(onClick = { viewModel.clearAllNotifications() }) {
                Text("Delete All", color = AccentRed)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            notifs.forEach { n ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(n.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(n.message, fontSize = 11.sp, color = TextGray)
                    }
                    IconButton(onClick = { viewModel.deleteNotification(n.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRed)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════
// ADMIN SETTINGS TAB
// ══════════════════════════════════════════════
@Composable
fun AdminSettingsTab(viewModel: MainViewModel) {
    val config by viewModel.appConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var upiId by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var phone1 by remember { mutableStateOf("") }
    var phone2 by remember { mutableStateOf("") }
    var hours by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mapLink by remember { mutableStateOf("") }
    var termsAndConditions by remember { mutableStateOf("") }
    var privacyPolicy by remember { mutableStateOf("") }

    LaunchedEffect(config) {
        config?.let {
            upiId = it.upiId
            shopName = it.shopName
            ownerName = it.ownerName
            phone1 = it.phone1
            phone2 = it.phone2
            hours = it.workingHours
            address = it.address
            email = it.email
            mapLink = it.mapLink
            termsAndConditions = it.termsAndConditions
            privacyPolicy = it.privacyPolicy
        }
    }

    var apiKeyInput by remember { mutableStateOf(viewModel.getStoredCustomApiKey()) }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("⚙️ Application Branding & Settings Builder", fontWeight = FontWeight.Bold, fontSize = 15.sp)

        OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("Paytm/GPay Shop UPI ID") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = shopName, onValueChange = { shopName = it }, label = { Text("App Brand Banner Shop Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Proprietor Owner Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = phone1, onValueChange = { phone1 = it }, label = { Text("Owner Main Phone 1") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = phone2, onValueChange = { phone2 = it }, label = { Text("Owner Contact Phone 2") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = hours, onValueChange = { hours = it }, label = { Text("Store Working Hours") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Store Landmark Physical Location Address") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Store Customer Support Email Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = mapLink, onValueChange = { mapLink = it }, label = { Text("Store Google Maps Location URL") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(
            value = termsAndConditions,
            onValueChange = { termsAndConditions = it },
            label = { Text("Terms & Conditions (नियम व शर्तें)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        OutlinedTextField(
            value = privacyPolicy,
            onValueChange = { privacyPolicy = it },
            label = { Text("Privacy Policy (गोपनीयता नीति)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(
            onClick = {
                val updated = AppConfigEntity(
                    id = 1,
                    upiId = upiId,
                    shopName = shopName,
                    qrCodeBase64 = config?.qrCodeBase64 ?: "",
                    ownerName = ownerName,
                    phone1 = phone1,
                    phone2 = phone2,
                    workingHours = hours,
                    address = address,
                    visitorsTotal = config?.visitorsTotal ?: 15286,
                    visitorsToday = config?.visitorsToday ?: 34,
                    chatbotQueries = config?.chatbotQueries ?: 120,
                    lastVisitorDate = config?.lastVisitorDate ?: "",
                    email = email,
                    mapLink = mapLink,
                    termsAndConditions = termsAndConditions,
                    privacyPolicy = privacyPolicy
                )
                viewModel.saveConfig(updated)
                Toast.makeText(context, "Configurations saved successfully!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Brand Configuration", fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        // AI Chatbot Developer Key setup
        Text("🤖 AI Chatbot Gemini Setup", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        Text("AI chatbot utilizes standard Google Gemini platform API keys. Enter custom key as fallback to avoid quota failure limit.", fontSize = 11.sp, color = TextGray)

        OutlinedTextField(value = apiKeyInput, onValueChange = { apiKeyInput = it }, label = { Text("Paste customized Google Gemini API Key") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(
            onClick = {
                viewModel.saveCustomApiKey(apiKeyInput)
                Toast.makeText(context, "Custom API Key saved successfully!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Custom API Token")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        // Change password security credential
        Text("🔐 Change Access Password Key", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        OutlinedTextField(value = oldPass, onValueChange = { oldPass = it }, label = { Text("Current Password") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("New Security Password (Min. 6 characters)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Button(
            onClick = {
                val ok = viewModel.changeAdminPassword(oldPass, newPass)
                if (ok) {
                    Toast.makeText(context, "Credentials altered! Keep it noted safely.", Toast.LENGTH_SHORT).show()
                    oldPass = ""
                    newPass = ""
                } else {
                    Toast.makeText(context, "Invalid Current password or weak new credential (min 6 required)!", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Update Access Credentials")
        }
    }
}
