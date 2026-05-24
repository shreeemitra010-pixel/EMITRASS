package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.Screen
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MainContent(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Chatbot Global State
    var showChatbot by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HeaderBar(
                currentScreen = currentScreen,
                isAdmin = isAdminLoggedIn,
                isDark = isDarkTheme,
                onToggleTheme = { viewModel.toggleTheme() },
                onNavigate = { viewModel.navigateTo(it) },
                onLogout = { viewModel.logoutAdmin() },
                onBack = { viewModel.navigateBack() }
            )
        },
        bottomBar = {
            if (currentScreen != Screen.ADMIN_DASHBOARD) {
                BottomNavBar(
                    currentScreen = currentScreen,
                    onNavigate = { viewModel.navigateTo(it) }
                )
            }
        },
        floatingActionButton = {
            if (currentScreen != Screen.ADMIN_DASHBOARD) {
                FloatingActionButton(
                    onClick = { showChatbot = true },
                    containerColor = GoldPrimary,
                    contentColor = NavyPrimary,
                    shape = CircleShape,
                ) {
                    Text("🤖", fontSize = 24.sp)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(viewModel)
                Screen.SERVICES -> ServicesScreen(viewModel)
                Screen.BHARTI -> BhartiScreen(viewModel)
                Screen.TRACK -> ContactScreen(viewModel)
                Screen.NOTIFICATIONS -> NotificationsScreen(viewModel)
                Screen.APPLY_FORM -> ContactScreen(viewModel)
                Screen.ADMIN_LOGIN -> AdminLoginScreen(viewModel)
                Screen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel)
            }

            if (showChatbot) {
                ChatbotDialog(viewModel = viewModel, onDismiss = { showChatbot = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar(
    currentScreen: Screen,
    isAdmin: Boolean,
    isDark: Boolean,
    onToggleTheme: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Boolean
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (isAdmin) onNavigate(Screen.ADMIN_DASHBOARD) else onNavigate(Screen.ADMIN_LOGIN)
                        }
                    )
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("श्री", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Shree Shyam E-Mitra",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Chamu, Jodhpur",
                        fontSize = 11.sp,
                        color = GoldPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        navigationIcon = {
            if (currentScreen != Screen.HOME) {
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
            }
        },
        actions = {
            IconButton(onClick = onToggleTheme) {
                Text(if (isDark) "☀️" else "🌙", fontSize = 20.sp)
            }

            if (isAdmin && currentScreen == Screen.ADMIN_DASHBOARD) {
                IconButton(onClick = { onLogout(); onNavigate(Screen.HOME) }) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = AccentRed)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NavyPrimary,
            titleContentColor = Color.White
        )
    )
}

@Composable
fun BottomNavBar(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    NavigationBar(
        containerColor = NavyPrimary,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple(Screen.HOME, Icons.Default.Home, "Home"),
            Triple(Screen.SERVICES, Icons.Default.List, "Services"),
            Triple(Screen.BHARTI, Icons.Default.Star, "Updates"),
            Triple(Screen.TRACK, Icons.Default.Phone, "Contact"),
            Triple(Screen.NOTIFICATIONS, Icons.Default.Notifications, "Alerts")
        )

        navItems.forEach { (screen, icon, label) ->
            val isSelected = currentScreen == screen || (screen == Screen.TRACK && currentScreen == Screen.APPLY_FORM)
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(screen) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NavyPrimary,
                    selectedTextColor = GoldPrimary,
                    indicatorColor = GoldPrimary,
                    unselectedIconColor = Color.White.copy(alpha = 0.5f),
                    unselectedTextColor = Color.White.copy(alpha = 0.5f)
                )
            )
        }
    }
}

// ══════════════════════════════════════════════
// 1. HOME SCREEN
// ══════════════════════════════════════════════
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val config by viewModel.appConfig.collectAsStateWithLifecycle()
    val activePosts by viewModel.activePosts.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val finalPhone = config?.phone1 ?: "7231932256"
    val addressText = config?.address ?: "UCO Bank के सामने, चामु, जोधपुर"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Hero Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "राजस्थान सरकार अधिकृत E-Mitra",
                        color = GoldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "आपकी हर सरकारी सेवा\nएक ही जगह!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 30.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "7+ सालों के अनुभव के साथ जोधपुर का सबसे विश्वसनीय ई-मित्र केंद्र।",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.TRACK) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary)
                        ) {
                            Text("📍 दुकान की लोकेशन", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$finalPhone?text=नमस्ते! मुझे Shree Shyam E-Mitra की सेवाएं चाहिए।"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = Color.White)
                        ) {
                            Text("💬 WhatsApp", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val stats = listOf(
                    "99999+" to "Customers",
                    "7+" to "Years Exp",
                    "99%" to "Success"
                )
                stats.forEach { (count, label) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(count, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            Text(label, fontSize = 11.sp, color = TextGray)
                        }
                    }
                }
            }
        }

        // Contact Information Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("📍 हमारा पता व संपर्क", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("👤", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("संचालक: ${config?.ownerName ?: "Prem Choudhary"}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🏢", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(addressText, fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📞", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Phone 1: $finalPhone", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$finalPhone")))
                            })
                            config?.phone2?.let {
                                if (it.isNotBlank()) {
                                    Text("Phone 2: $it", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable {
                                        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it")))
                                    })
                                }
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🕒", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("समय: ${config?.workingHours ?: "सोम-शनि: 9AM–7PM"}", fontSize = 13.sp)
                    }
                }
            }
        }

        // Latest Bharti Alerts Small Banner
        if (activePosts.isNotEmpty()) {
            item {
                Column {
                    Text(
                        "📢 ताज़ा भर्तियां (Latest Vacancies)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    activePosts.take(2).forEach { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.navigateTo(Screen.BHARTI) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(post.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(
                                        "Last Date: ${post.lastDate.ifBlank { "—" }} | ${post.postsCount}",
                                        fontSize = 11.sp,
                                        color = AccentOrange,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GoldPrimary.copy(alpha = 0.2f))
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                ) {
                                    Text("🔍 See", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Why Choose Us
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("⭐ हमारी विशेषताएं", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                val features = listOf(
                    "⚡" to "तेज़ सेवा" to "Same day processing with full proof accuracy.",
                    "🔒" to "100% सुरक्षित" to "Your Aadhaar and critical personal data are fully secured.",
                    "💰" to "किफायती शुल्क" to "Minimum e-mitra catalog charges, no hidden fees."
                )
                features.forEach { (pair, desc) ->
                    val (icon, title) = pair
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f)), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(icon, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text(desc, fontSize = 11.sp, color = TextGray)
                        }
                    }
                }
            }
        }

        // Testimonials Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("💬 हमारे ग्राहक क्या कहते हैं (Testimonials)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                val testimonials = listOf(
                    "Income certificate एक ही दिन में मिल गई। Prem जी बहुत helpful हैं।" to "Ramesh Kumar",
                    "Aadhaar Address Update एक ही घन्टे में अपडेट हो गया था। Prem जी बहुत helpful हैं।" to "Kavita Sharma",
                    "PAN card जल्दी हो गया। किफायती charges।" to "Suresh Solanki",
                    "PM-KISAN registration बिना परेशानी के हो गया।" to "Mahendra Choudhary"
                )
                testimonials.forEach { (review, author) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) {
                                    Text("⭐", fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "“$review”",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "— $author",
                                color = GoldDark,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════
// 2. SERVICES TAB
// ══════════════════════════════════════════════
@Composable
fun ServicesScreen(viewModel: MainViewModel) {
    val services by viewModel.activeServices.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val categories = listOf("All") + services.map { it.category }.distinct()

    val filteredServices = services.filter {
        val matchesCategory = selectedCategory == "All" || it.category == selectedCategory
        val matchesQuery = it.name.lowercase().contains(searchQuery.lowercase()) ||
                it.description.lowercase().contains(searchQuery.lowercase())
        matchesCategory && matchesQuery
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search 100+ services...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = GoldPrimary
            ),
            singleLine = true
        )

        // Categories Chips List Horizontally Scrollable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Services Listing
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (filteredServices.isEmpty()) {
                item {
                    Text(
                        "कोई सेवा नहीं मिली।",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        textAlign = TextAlign.Center,
                        color = TextGray
                    )
                }
            } else {
                items(filteredServices) { service ->
                    ServiceCard(
                        service = service,
                        onWhatsApp = {
                            val phone = viewModel.appConfig.value?.phone1 ?: "7231932256"
                            val text = "नमस्ते, मुझे '${service.name}' सेवा के बारे में जानकारी चाहिए और इसके लिए आवश्यक दस्तावेज भेजकर आवेदन करना है।"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$phone?text=${Uri.encode(text)}"))
                            context.startActivity(intent)
                        },
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: ServiceEntity, onWhatsApp: () -> Unit, context: android.content.Context) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(service.icon, fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(service.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Text(service.category, fontSize = 11.sp, color = GoldDark, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(service.description, fontSize = 12.sp, color = TextGray)

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider()

                    // Documents List
                    Text("📋 आवश्यक दस्तावेज (Req. Docs):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    val docs = service.requiredDocs.split("\n").filter { it.isNotBlank() }
                    if (docs.isEmpty()) {
                        Text("• संपर्क करें", fontSize = 12.sp, color = TextGray)
                    } else {
                        docs.forEach { doc ->
                            Text("• $doc", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (service.link.isNotBlank()) {
                            Button(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(service.link)))
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("🌐 Official Apply", fontSize = 11.sp)
                            }
                        }
                        if (service.formUrl.isNotBlank()) {
                            Button(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(service.formUrl)))
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("⬇️ Form PDF", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (expanded) "🔼 Hide details" else "🔽 Show documents & links",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDark
                    )
                }

                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("💬 WhatsApp संपर्क", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════
// 3. BHARTI TAB (LATEST UPDATES)
// ══════════════════════════════════════════════
@Composable
fun BhartiScreen(viewModel: MainViewModel) {
    val posts by viewModel.activePosts.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("all") }
    val context = LocalContext.current

    val categories = listOf(
        "all" to "📢 सभी",
        "bharti" to "💼 Bharti",
        "result" to "📊 Result",
        "admit" to "🎟️ Admit Card",
        "answer" to "📝 Answer Key",
        "syllabus" to "📚 Syllabus"
    )

    val filteredPosts = posts.filter {
        selectedCategory == "all" || it.category == selectedCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("📢 Latest Government Updates", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)

        // Category tabs scrollable horizontal row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { (key, label) ->
                val isSelected = selectedCategory == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = key },
                    label = { Text(label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Post list
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (filteredPosts.isEmpty()) {
                item {
                    Text(
                        "इस category में अभी कोई सूचना नहीं है।",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        textAlign = TextAlign.Center,
                        color = TextGray
                    )
                }
            } else {
                items(filteredPosts) { post ->
                    BhartiCard(post = post, context = context)
                }
            }
        }
    }
}

@Composable
fun BhartiCard(post: AppPostEntity, context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val badgeColor = when (post.category) {
                    "bharti" -> Color(0xFFE6F0FF)
                    "result" -> Color(0xFFE8F8ED)
                    "admit" -> Color(0xFFFFF3E0)
                    else -> Color(0xFFFCE8E8)
                }
                val badgeText = when (post.category) {
                    "bharti" -> "💼 Bharti"
                    "result" -> "📊 Result"
                    "admit" -> "🎟️ Admit Card"
                    "answer" -> "📝 Answer"
                    "syllabus" -> "📚 Syllabus"
                    else -> "📢 Update"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(badgeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                }

                Text("📅 ${post.date}", fontSize = 11.sp, color = TextGray)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(post.title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(post.description, fontSize = 12.sp, color = TextGray, lineHeight = 16.sp)

            // Bharti Details Block
            if (post.category == "bharti") {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (post.postsCount.isNotBlank()) Text("📌 **कुल पद (Total Posts):** ${post.postsCount}", fontSize = 11.sp)
                    if (post.fees.isNotBlank()) Text("💰 **शुल्क (Fees):** ${post.fees}", fontSize = 11.sp)
                    if (post.ageLimit.isNotBlank()) Text("🎂 **आयु (Age):** ${post.ageLimit}", fontSize = 11.sp)
                    if (post.qualification.isNotBlank()) Text("🎓 **योग्यता (Qual.):** ${post.qualification}", fontSize = 11.sp)
                    if (post.startDate.isNotBlank() || post.lastDate.isNotBlank()) {
                        Text(
                            "🟢 Start Date: ${post.startDate} | 🔴 Last Date: ${post.lastDate}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentRed
                        )
                    }
                }
            }

            // Status Badge for result/admit
            if (post.category != "bharti" && post.expectedDateStatus.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                val statusLabel = when (post.expectedDateStatus) {
                    "soon" -> "🔜 Coming Soon"
                    "today" -> "🔥 आज घोषित होगा!"
                    "released" -> "✅ जारी कर दिया गया है"
                    else -> "Expected"
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(statusLabel, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    if (post.expectedDate.isNotBlank()) {
                        Text(" (${post.expectedDate})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (post.officialLink.isNotBlank()) {
                    Button(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.officialLink)))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("🔔 Notification", fontSize = 11.sp)
                    }
                }

                val downloadUrl = if (post.category == "bharti") post.applyLink else post.downloadLink
                val mainLabel = if (post.category == "bharti") "Apply Online 📝" else "Download Link ⬇️"

                if (downloadUrl.isNotBlank()) {
                    Button(
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(mainLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════
// 4. APPLY & TRACK APPLICATION FLOWS
// ══════════════════════════════════════════════
@Composable
fun ContactScreen(viewModel: MainViewModel) {
    val config by viewModel.appConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Local expandable state
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }

    val primaryPhone = config?.phone1 ?: "7231932256"
    val secondPhone = config?.phone2 ?: "8233003147"
    val emailAdd = config?.email ?: "shreeemitra010@gmail.com"
    val mapsLink = config?.mapLink ?: "https://maps.app.goo.gl/3XgDmsZq8P98v6qTA"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Main Branding banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        config?.shopName ?: "Shree Shyam E-Mitra",
                        color = GoldPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        "संचालक: ${config?.ownerName ?: "Prem Choudhary"}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        "⏰ ${config?.workingHours ?: "Mon-Sat: 9AM–7PM"}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Contact details info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("📞 संपर्क सूत्र (Call & WhatsApp Support)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    HorizontalDivider()

                    // Phone 1 Action Card
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("👤 मुख्य संपर्क:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Text("+91 $primaryPhone", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$primaryPhone"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text("📞 कॉल करें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val text = "नमस्ते, मुझे आपके ई-मित्र की सेवाओं के संदर्भ में सहायता चाहिए।"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91$primaryPhone?text=${Uri.encode(text)}"))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text("💬 WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider()

                    // Phone 2 Action Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("📞 वैकल्पिक संपर्क:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("+91 $secondPhone", fontSize = 11.sp, color = TextGray)
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$secondPhone"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyMedium),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("📞 कॉल करें", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Location & Address Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("📍 दुकान का पता (Shop Location)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                    HorizontalDivider()
                    Text(
                        config?.address ?: "UCO Bank के सामने, चामु, जोधपुर, राजस्थान",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsLink))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🗺️ गूगल मैप्स पर लोकेशन देखें", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Email address card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("✉️ आधिकारिक ईमेल", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        Text(emailAdd, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$emailAdd")
                                putExtra(Intent.EXTRA_SUBJECT, "Shree Shyam E-Mitra App Query")
                            }
                            try {
                                context.startActivity(intent)
                            } catch(e: Exception) {
                                Toast.makeText(context, "No email client found!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyMedium),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("✉️ Email", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Expandable Terms & Conditions
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTerms = !showTerms },
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📄 नियम व शर्तें (Terms & Conditions)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text(if (showTerms) "🔼" else "🔽", fontSize = 12.sp, color = GoldDark)
                    }

                    AnimatedVisibility(visible = showTerms) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                config?.termsAndConditions ?: "नियम व शर्तें जानकारी जल्द अपडेट होगी।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Expandable Privacy Policy
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPrivacy = !showPrivacy },
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒 गोपनीयता नीति (Privacy Policy)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        Text(if (showPrivacy) "🔼" else "🔽", fontSize = 12.sp, color = GoldDark)
                    }

                    AnimatedVisibility(visible = showPrivacy) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                config?.privacyPolicy ?: "गोपनीयता नीति जानकारी जल्द अपडेट होगी।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackStatusScreen(viewModel: MainViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val trackedApp by viewModel.trackedApplication.collectAsStateWithLifecycle()
    val statusText by viewModel.trackResultStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "लिखें Application ID (जैसे: SSE-2026-00001) या अपना registered मोबाइल नंबर स्थिति जांचने के लिए। ",
            fontSize = 12.sp,
            color = TextGray
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("App ID / Mobile No.") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = GoldPrimary
                ),
                singleLine = true
            )

            Button(
                onClick = { viewModel.trackApplication(searchQuery) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Search")
            }
        }

        HorizontalDivider()

        when (statusText) {
            "not_found" -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "⚠️ क्षमा करें, कोई आवेदन दर्ज नहीं मिला। कृपया सही ID/Mobile दर्ज़ करें।",
                        color = AccentRed,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
            "found" -> {
                trackedApp?.let { app ->
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header info
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(app.id, color = GoldPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                        Text(app.serviceName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    // Display status badge
                                    val (badgeBg, badgeText, badgeColor) = when (app.status) {
                                        "approved" -> Triple(Color(0xFFE8F8ED), "Approved ✅", AccentGreen)
                                        "rejected" -> Triple(Color(0xFFFCE8E8), "Rejected ❌", AccentRed)
                                        "correction" -> Triple(Color(0xFFFFF3E0), "Correction ✏️", AccentOrange)
                                        else -> Triple(Color(0xFFE6F0FF), "Pending ⏳", NavyPrimary)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(badgeText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                                    }
                                }
                            }
                        }

                        // Correction Note box
                        if (app.status == "correction" && app.correctionNote.isNotBlank()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) Color(0xFF2C1E0A) else Color(0xFFFFF9E6)),
                                    border = BorderStroke(1.dp, AccentOrange)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("✏️ संचालक सुधार नोट (Correction Note):", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(app.correctionNote, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                val sObj = viewModel.allServices.value.find { it.name == app.serviceName }
                                                viewModel.selectServiceForForm(sObj)
                                                viewModel.navigateTo(Screen.APPLY_FORM)
                                                Toast.makeText(context, "Correction notes Loaded in Form", Toast.LENGTH_LONG).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("सुधार कर पुनः भेजें (Re-Apply)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Steps Progress Indicator
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("📈 प्रसंस्करण स्थिति (Processing Timeline):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(10.dp))

                                    val isSubmitted = true
                                    val isVerified = app.status == "approved" || app.status == "correction" || app.status == "rejected"
                                    val isProcessing = app.status == "approved" || app.status == "rejected"
                                    val isCompleted = app.status == "approved"

                                    TimelineStep("1. आवेदन जमा किया (Submitted)", app.submissionDate, isSubmitted)
                                    TimelineStep("2. संचालक जांच (Document Verification)", if (isVerified) "Complete" else "In Progress", isVerified)
                                    TimelineStep("3. प्रोग्रेसिंग (Processing)", if (isProcessing) "Processed" else "Waiting", isProcessing)
                                    TimelineStep(
                                        "4. अंतिम परिणाम (Completed / Rejected)",
                                        if (app.status == "rejected") "Rejected ❌" else if (app.status == "approved") "Success ✅" else "Pending",
                                        isCompleted
                                    )
                                }
                            }
                        }

                        // Customer admin files ready for download
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("📥 तैयार सरकारी दस्तावेज व रसीदें (Issued Docs):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    val listArray = try {
                                        JSONArray(app.adminFilesJson)
                                    } catch (e: Exception) {
                                        JSONArray()
                                    }

                                    if (listArray.length() == 0) {
                                        Text("जांच के बाद जारी दस्तावेज यहाँ उपलब्ध होंगे।", color = TextGray, fontSize = 12.sp)
                                    } else {
                                        for (i in 0 until listArray.length()) {
                                            val obj = listArray.getJSONObject(i)
                                            val fName = obj.optString("name", "Document.pdf")
                                            val fSize = obj.optString("size", "0 KB")
                                            val fNote = obj.optString("note", "")

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                                    .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f)), RoundedCornerShape(8.dp))
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(fName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text("$fSize ${if (fNote.isNotBlank()) "— $fNote" else ""}", fontSize = 11.sp, color = TextGray)
                                                }
                                                Button(
                                                    onClick = {
                                                        Toast.makeText(context, "Downloading $fName...", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Download", fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Details checklist
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("📋 आवेदन का विवरण (Details):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    DetailRow("नाम", app.name)
                                    DetailRow("पिता का नाम", app.fatherName)
                                    DetailRow("मोबाइल नंबर", app.mobile)
                                    DetailRow("Aadhaar Card", app.aadhaar)
                                    DetailRow("Service", app.serviceName)
                                    DetailRow("Fees Amount", "₹${app.feesAmount}")
                                    DetailRow("Payment Method", app.paymentMethod.uppercase())
                                    DetailRow("Payment Status", app.paymentStatus.uppercase())
                                    DetailRow("Address", app.address)
                                    DetailRow("Notes", app.notes)
                                    DetailRow("Remarks", app.remarks.ifBlank { "कोई备注नहीं।" })
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, "Shree Shyam E-Mitra Jodhpur\nApplication ID: ${app.id}\nStatus: ${app.status.uppercase()}\nService: ${app.serviceName}\nThank you!")
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Receipt Via"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Share Status Receipt")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineStep(label: String, valText: String, isFinished: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isFinished) AccentGreen else Color.Gray.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, fontSize = 12.sp, fontWeight = if (isFinished) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        Text(valText, fontSize = 11.sp, color = TextGray)
    }
}

@Composable
fun DetailRow(label: String, text: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextGray, fontSize = 12.sp)
            Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp), color = Color.LightGray.copy(alpha = 0.3f))
    }
}

@Composable
fun QuickApplyIntroScreen(viewModel: MainViewModel) {
    val services by viewModel.activeServices.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "ई-मित्र केंद्र आएं या सीधे यहीं से ऑनलाइन फॉर्म जमा करें। ",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            fontSize = 14.sp
        )
        Text(
            "नीचे दी गई सूची से आवश्यक सेवा चुनें, नियम व आवश्यक दस्तावेज पढ़ें और ऑनलाइन आवेदन दर्ज करें।",
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { viewModel.navigateTo(Screen.SERVICES) },
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("✅ सभी 100+ योग्य सेवाएं देखें", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text("🔥 मुख्य लोकप्रिय सेवाएं:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Start))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            val popList = services.take(5)
            items(popList) { service ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                        .border(BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.1f)), RoundedCornerShape(10.dp))
                        .clickable {
                            viewModel.selectServiceForForm(service)
                            viewModel.navigateTo(Screen.APPLY_FORM)
                        }
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(service.icon, fontSize = 20.sp)
                        Text(service.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text("Apply >>", fontSize = 11.sp, color = GoldDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════
// 5. APPLY FORM SCREEN
// ══════════════════════════════════════════════
@Composable
fun ApplyFormScreen(viewModel: MainViewModel) {
    val serviceSelected by viewModel.selectedServiceForForm.collectAsStateWithLifecycle()
    val successAppId by viewModel.submissionSuccessAppId.collectAsStateWithLifecycle()
    val servicesList by viewModel.activeServices.collectAsStateWithLifecycle()
    val config by viewModel.appConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val finalUpiId = config?.upiId ?: "7231932256@paytm"
    val finalShopName = config?.shopName ?: "Shree Shyam E-Mitra"

    if (successAppId != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(AccentGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("✅", fontSize = 40.sp)
            }

            Text("आवेदन सफलता पूर्वक जमा हुआ!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SSE Application ID", color = GoldPrimary, fontSize = 12.sp)
                    Text(successAppId ?: "", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = 2.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("कृपया यह ID सुरक्षित नोट कर लें", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }

            Text(
                "आपका आवेदन जाँचने के बाद संचालक 24 घंटों में समाधान करेंगे। रसीद व दस्तावेज़ की जानकारी WhatsApp पर स्वतः प्राप्त करने के लिए नीचे भेजें।",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = TextGray
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.navigateTo(Screen.TRACK); viewModel.trackApplication(successAppId ?: "") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Track Status")
                }

                Button(
                    onClick = {
                        val whatsappUrl = "https://wa.me/91${config?.phone1 ?: "7231932256"}?text=" +
                                "नमस्ते! मैंने ऑनलाइन फॉर्म जमा किया है।\n" +
                                "आवेदन ID: $successAppId\n" +
                                "सेवा: ${serviceSelected?.name ?: ""}"
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl)))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Text("💬 WhatsApp Sync")
                }
            }

            Button(
                onClick = { viewModel.resetSubmission() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Text("नया आवेदन करें")
            }
        }
    } else {
        // Main input form
        var name by remember { mutableStateOf("") }
        var mobile by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var fatherName by remember { mutableStateOf("") }
        var aadhaar by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        var paymentMethod by remember { mutableStateOf("upi") }
        var utrTransactionId by remember { mutableStateOf("") }

        val contextLocal = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("📝 ऑनलाइन आवेदन फॉर्म भरें", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 17.sp)

            Text("Select Target Service *", fontWeight = FontWeight.Bold, fontSize = 12.sp)

            // Spinner/Selector for Services list
            var dropdownExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { dropdownExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                ) {
                    Text(serviceSelected?.name ?: "-- Choose Active Service * --", fontSize = 13.sp)
                }
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    servicesList.forEach { service ->
                        DropdownMenuItem(
                            text = { Text("${service.icon} ${service.name} (₹${service.price})") },
                            onClick = {
                                viewModel.selectServiceForForm(service)
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            serviceSelected?.let { s ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("💡 इस सेवा के लिए आवश्यक दस्तावेज (Scan/Photo):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                        val docs = s.requiredDocs.split("\n").filter { it.isNotBlank() }
                        docs.forEach { d ->
                            Text("• $d", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            HorizontalDivider()

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("आवेदक का पूरा नाम (Full Name) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("मोबाइल नंबर *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = fatherName,
                    onValueChange = { fatherName = it },
                    label = { Text("पिता/पति का नाम") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = aadhaar,
                    onValueChange = { aadhaar = it },
                    label = { Text("Aadhaar Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("पूरा पता (Address) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("आवश्यक टिप्पणी / Notes") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // Documents Selection simulation card
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📎 अपलोड दस्तावेज (Scan copy upload)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Aadhaar Card, Photo, Marksheet आदि फाइलें सेलेक्ट करें।", fontSize = 11.sp, color = TextGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            Toast.makeText(contextLocal, "Simulated: File selected successfully copy!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("फ़ाइलें सेलेक्ट करें (Select Files)")
                    }
                }
            }

            HorizontalDivider()

            // Payment selection M3 layout
            Text("💳 पेमेंट मैथेड चुनें (Payment Method)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("upi", "📱 UPI / QR"),
                    Pair("cash", "💵 Cash"),
                    Pair("later", "🕐 Later")
                ).forEach { (key, label) ->
                    val isSelected = paymentMethod == key
                    Button(
                        onClick = { paymentMethod = key },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (paymentMethod == "upi") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.03f)),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Scan QR Code to Pay dynamically", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)

                        // Beautiful QR code simulator box
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(BorderStroke(2.dp, GoldPrimary), RoundedCornerShape(8.dp))
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("QR Code", fontSize = 10.sp, color = TextGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("📱", fontSize = 34.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Scan to Pay", fontSize = 10.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text("UPI ID: $finalUpiId", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                        Text(finalShopName, fontSize = 11.sp, color = TextGray)

                        OutlinedTextField(
                            value = utrTransactionId,
                            onValueChange = { utrTransactionId = it },
                            placeholder = { Text("UTR / Ref. Number (Optional)") },
                            modifier = Modifier.fillMaxWidth(0.9f),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    if (serviceSelected == null) {
                        Toast.makeText(contextLocal, "कृपया पहले Service चयन करें!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (name.isBlank() || mobile.isBlank() || address.isBlank()) {
                        Toast.makeText(contextLocal, "कृपया सभी आवश्यक (*) फ़ील्ड भरें!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (mobile.length < 10) {
                        Toast.makeText(contextLocal, "कृपया मान्य 10-अंकीय मोबाइल नंबर दर्ज़ करें!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val simFiles = listOf("Simulated_Aadhaar_${mobile}.pdf", "Simulated_Photo_${mobile}.jpg")

                    viewModel.submitApplication(
                        name = name,
                        mobile = mobile,
                        email = email,
                        serviceName = serviceSelected!!.name,
                        address = address,
                        fatherName = fatherName,
                        aadhaar = aadhaar,
                        notes = notes,
                        paymentMethod = paymentMethod,
                        feesAmount = serviceSelected!!.price,
                        uploadedFiles = simFiles
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📤 आवेदन दर्ज करें (Submit Application)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

// ══════════════════════════════════════════════
// 6. NOTIFICATIONS ALERTS TAB
// ══════════════════════════════════════════════
@Composable
fun NotificationsScreen(viewModel: MainViewModel) {
    val notifs by viewModel.allNotifications.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🔔 महत्वपूर्ण घोषणाएं (Announcements)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            if (notifs.isEmpty()) {
                item {
                    Text(
                        "अभी संचारित करने के लिए कोई सूचनाएं नहीं हैं। ",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(notifs) { notif ->
                    val colorBg = when (notif.type) {
                        "important" -> Color(0xFFFFF6E6)
                        "offer" -> Color(0xFFE8F8ED)
                        "camp" -> Color(0xFFF3E8FF)
                        else -> Color(0xFFE6F0FF)
                    }
                    val iconText = when (notif.type) {
                        "important" -> "⚠️"
                        "offer" -> "🎉"
                        "camp" -> "🗺️"
                        else -> "ℹ️"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorBg),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(iconText, fontSize = 18.sp)
                                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Text("📅 ${notif.date}", fontSize = 10.sp, color = TextGray)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(notif.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp)

                            if (notif.link.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(notif.link)))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("अधिक जानकारी", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════
// 7. CHATBOT DIALOG / FLOATING ACTION WIDGET
// ══════════════════════════════════════════════
@Composable
fun ChatbotDialog(viewModel: MainViewModel, onDismiss: () -> Unit) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isLoading by viewModel.isChatbotLoading.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤖 AI Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(AccentGreen)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Messages Box
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(messages) { msg ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (msg.isBot) Alignment.CenterStart else Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (msg.isBot) NavyPrimary else GoldPrimary)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .widthIn(max = 240.dp)
                            ) {
                                Text(
                                    msg.text,
                                    color = if (msg.isBot) Color.White else NavyPrimary,
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    if (isLoading) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(8.dp)) {
                                Text("thinking...", fontSize = 11.sp, color = TextGray)
                            }
                        }
                    }
                }

                // Input Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask anything...") },
                        modifier = Modifier.weight(1f),
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessageToChatbot(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = NavyPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { viewModel.clearChat() }) {
                    Text("Clear Chat", color = AccentRed)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close", color = NavyPrimary)
                }
            }
        }
    )
}

// ══════════════════════════════════════════════
// 8. ADMIN LOGIN & AREA
// ══════════════════════════════════════════════
@Composable
fun AdminLoginScreen(viewModel: MainViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isAlreadyIn by viewModel.isAdminLoggedIn.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (isAlreadyIn) {
        LaunchedEffect(Unit) {
            viewModel.navigateTo(Screen.ADMIN_DASHBOARD)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(GoldPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = NavyPrimary, modifier = Modifier.size(36.dp))
        }

        Text("Admin Security Login", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NavyPrimary)
        Text("Authorized operations only. Please sign in.", color = TextGray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Registered Email Address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Security Access Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                val ok = viewModel.verifyAdminLogin(email, password)
                if (ok) {
                    viewModel.navigateTo(Screen.ADMIN_DASHBOARD)
                    Toast.makeText(context, "Welcome Admin Prem!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Invalid Email or Secret Key / Password!", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = NavyPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("🔐 Access Dashboard", fontWeight = FontWeight.Bold)
        }
    }
}
