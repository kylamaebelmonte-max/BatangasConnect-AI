package com.example.batangasconnectai

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TourismCategory(val name: String, val emoji: String, val color: Color)
data class ChatMessage(val text: String, val isUser: Boolean)
data class MapSpot(val title: String, val location: String, val category: String, val emoji: String)
data class FeaturedSpotItem(val title: String, val description: String, val imageRes: Int)
data class TourismItem(
    val title: String,
    val category: String,
    val description: String,
    val emoji: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    elevenLabsService: ElevenLabsService,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var userInput by remember { mutableStateOf("") }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                "Mabuhay! Welcome to BatangasConnect AI, your smart tourism assistant for Batangas. How can I help you explore our beautiful beaches, rich history, and delicious local food today?",
                false
            )
        )
    }

    fun handleUserMessage(prompt: String) {
        if (prompt.isBlank()) return

        messages.add(ChatMessage(prompt, isUser = true))

        val aiReply = generateNaturalTourismResponse(prompt)
        messages.add(ChatMessage(aiReply, isUser = false))

        elevenLabsService.speak(aiReply)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BatangasConnect AI", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Smart Tourism Assistant", fontSize = 12.sp, color = Color.Gray)
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("🗺️", fontSize = 20.sp) },
                    label = { Text("Explore") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("📍", fontSize = 20.sp) },
                    label = { Text("Map") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("🤖", fontSize = 20.sp) },
                    label = { Text("AI Guide") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> ExploreDashboardView(
                    onCategoryClick = { categoryName ->
                        userInput = ""
                        val speechResponse = generateNaturalTourismResponse(categoryName)
                        elevenLabsService.speak(speechResponse)
                    }
                )
                1 -> BatangasInteractiveMapView(
                    onSpotSelected = { spotCategory ->
                        val speechResponse = generateNaturalTourismResponse(spotCategory)
                        elevenLabsService.speak(speechResponse)
                    }
                )
                2 -> ChatGuideView(
                    messages = messages,
                    userInput = userInput,
                    onUserInputChange = { userInput = it },
                    onSend = {
                        val query = userInput
                        userInput = ""
                        handleUserMessage(query)
                    },
                    onPlayVoice = { text -> elevenLabsService.speak(text) }
                )
            }
        }
    }
}

@Composable
fun ExploreDashboardView(onCategoryClick: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryForPopup by remember { mutableStateOf<TourismCategory?>(null) }
    var showFeaturedAlbumPopup by remember { mutableStateOf(false) }

    val categoriesRow1 = listOf(
        TourismCategory("Beaches", "🏖️", Color(0xFF4CAF50)),
        TourismCategory("Food & Cafe", "☕", Color(0xFFFF9800)),
        TourismCategory("Heritage", "🏛️", Color(0xFF9C27B0))
    )
    val categoriesRow2 = listOf(
        TourismCategory("Resorts", "🏨", Color(0xFF2196F3)),
        TourismCategory("Diving", "🤿", Color(0xFFE91E63)),
        TourismCategory("Culture", "🎭", Color(0xFF673AB7))
    )

    // Featured Spots accurately using R.drawable.basilica_img
    val featuredSpots = remember {
        listOf(
            FeaturedSpotItem("Taal Basilica & Heritage Town", "Explore the largest Catholic church in Asia and well-preserved Spanish colonial ancestral homes.", R.drawable.basilica_img),
            FeaturedSpotItem("Taal Volcano & Lake Scenic View", "Famous panoramic volcano and crater lake view of Talisay.", R.drawable.taal_img),
            FeaturedSpotItem("Laiya White Sand Beach", "Famous coastline with white sand resorts in San Juan.", R.drawable.laiya_beach),
            FeaturedSpotItem("San Sebastian Cathedral", "Iconic Lipa Cathedral with beautiful dome ceiling art.", R.drawable.san_sebastian_cathedral)
        )
    }

    val allItems = remember {
        listOf(
            // 5 Beaches
            TourismItem("Laiya White Sand Beach", "Beaches", "Famous coastline with white sand resorts in San Juan.", "🏖️"),
            TourismItem("Masasa Beach", "Beaches", "Crystal clear waters and marine life on Tingloy Island.", "🏝️"),
            TourismItem("Calatagan Beach", "Beaches", "Quiet shallow beaches famous for stilt huts and white sandbars.", "🏖️"),
            TourismItem("Fortune Island", "Beaches", "Grecian-style pillars and cliff diving off Nasugbu coast.", "🏛️"),
            TourismItem("Verde Island", "Beaches", "Pristine beach shores located in the center of marine biodiversity.", "🏝️"),

            // 5 Food & Cafe
            TourismItem("Sizzling Batangas Lomi", "Food & Cafe", "Thick egg noodles with rich pork liver and chicharon.", "🍜"),
            TourismItem("Kapeng Barako", "Food & Cafe", "Strong, dark Liberica coffee brewed fresh in Lipa City.", "☕"),
            TourismItem("Goto Batangas", "Food & Cafe", "Flavorful beef tripe broth seasoned with garlic and calamansi.", "🍲"),
            TourismItem("Batangas Bulalo", "Food & Cafe", "Slow-cooked beef shank soup with tender bone marrow.", "🍲"),
            TourismItem("Tapa Taal", "Food & Cafe", "Marinated garlic beef tapa paired with sinangag fried rice.", "🥩"),

            // 5 Heritage
            TourismItem("Taal Basilica & Heritage Town", "Heritage", "Largest Catholic church in Asia with Spanish colonial ancestral homes.", "🏛️"),
            TourismItem("San Sebastian Cathedral", "Heritage", "Iconic Lipa Cathedral with beautiful dome ceiling art.", "⛪"),
            TourismItem("Cape Santiago Lighthouse", "Heritage", "Historic 1890s lighthouse with panoramic ocean views in Calatagan.", "🚨"),
            TourismItem("Casa de Segunda", "Heritage", "1880s ancestral home of Segunda Katigbak in Lipa City.", "🏠"),
            TourismItem("Apacible Museum", "Heritage", "Historical home of revolutionary hero Galicano Apacible in Taal.", "📜"),

            // 5 Resorts
            TourismItem("The Farm at San Benito", "Resorts", "World-class eco-wellness retreat in Lipa City.", "🌿"),
            TourismItem("Acellaya Resort", "Resorts", "Beachfront villas along Laiya coastline.", "🏨"),
            TourismItem("Stilts Calatagan Beach Resort", "Resorts", "Overwater Maldives-inspired wooden cottages.", "🛖"),
            TourismItem("Matabungkay Beach Resort", "Resorts", "Classic family resort with floating balsa rafts.", "🌊"),
            TourismItem("Pico de Loro Cove", "Resorts", "Luxury beach club resort in Nasugbu.", "⛰️"),

            // 5 Diving
            TourismItem("Anilao Marine Sanctuary", "Diving", "World-renowned macro photography and coral reef diving in Mabini.", "🤿"),
            TourismItem("Twin Rocks Reef", "Diving", "Famous marine sanctuary with schooling jacks and soft corals.", "🐠"),
            TourismItem("Cathedral Rock", "Diving", "Underwater cross sanctuary surrounded by colorful reef fish.", "✝️"),
            TourismItem("Mainit Point", "Diving", "Exciting drift dive with rich macro sea life.", "🌊"),
            TourismItem("Sombrero Island Reef", "Diving", "Hat-shaped island with shallow coral gardens for divers.", "🏝️"),

            // 5 Culture
            TourismItem("Sublian Festival Traditions", "Culture", "Centuries-old devotional dance celebrating the Holy Cross.", "🎭"),
            TourismItem("Balisong Crafting in Taal", "Culture", "Handmade traditional Batangas butterfly knife artisan workshops.", "🔪"),
            TourismItem("Mount Maculot Trek", "Culture", "Scenic mountain trek offering views of Taal Lake and Volcano.", "⛰️"),
            TourismItem("Taal Embroidery (Bordado)", "Culture", "Hand-stitched Barong Tagalog and Filipiniana heritage craft.", "🪡"),
            TourismItem("Mount Gulugod Baboy Hike", "Culture", "Beginner-friendly coastal mountain hike with 360 ocean views.", "🥾")
        )
    }

    // POP-UP DIALOG MODAL FOR CATEGORY TAP
    selectedCategoryForPopup?.let { cat ->
        val categorySpots = allItems.filter { it.category.equals(cat.name, ignoreCase = true) }

        AlertDialog(
            onDismissRequest = { selectedCategoryForPopup = null },
            confirmButton = {
                TextButton(onClick = { selectedCategoryForPopup = null }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = Color(0xFF1B4F72))
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(cat.emoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${cat.name} Recommendations", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categorySpots.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF2F4F4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCategoryClick(item.title)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(item.emoji, fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                                    Text(item.description, fontSize = 11.sp, color = Color.Gray)
                                }
                                Text("🔊", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        )
    }

    // POP-UP ALBUM MODAL FOR "SEE ALL" FEATURED SPOTS
    if (showFeaturedAlbumPopup) {
        AlertDialog(
            onDismissRequest = { showFeaturedAlbumPopup = false },
            confirmButton = {
                TextButton(onClick = { showFeaturedAlbumPopup = false }) {
                    Text("Close", fontWeight = FontWeight.Bold, color = Color(0xFF1B4F72))
                }
            },
            title = {
                Text("🖼️ Featured Spots Album", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredSpots) { spot ->
                        Card(
                            onClick = {
                                showFeaturedAlbumPopup = false
                                onCategoryClick(spot.title)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Image(
                                    bitmap = ImageBitmap.imageResource(id = spot.imageRes),
                                    contentDescription = spot.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                )
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(spot.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                    Text(spot.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2)
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search places, food, activities...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (searchQuery.isBlank()) {
            item {
                Text("Categories", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categoriesRow1.forEach { cat ->
                        CategoryCardTile(
                            category = cat,
                            onClick = {
                                selectedCategoryForPopup = cat
                                onCategoryClick(cat.name)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    categoriesRow2.forEach { cat ->
                        CategoryCardTile(
                            category = cat,
                            onClick = {
                                selectedCategoryForPopup = cat
                                onCategoryClick(cat.name)
                            }
                        )
                    }
                }
            }

            // FEATURED SPOTS HEADER WITH "SEE ALL" ALBUM BUTTON
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Featured Spots", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    TextButton(onClick = { showFeaturedAlbumPopup = true }) {
                        Text("See All", fontWeight = FontWeight.Bold, color = Color(0xFF1B4F72), fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(featuredSpots) { spot ->
                        Card(
                            onClick = { onCategoryClick(spot.title) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            modifier = Modifier.width(300.dp)
                        ) {
                            Column {
                                Image(
                                    bitmap = ImageBitmap.imageResource(id = spot.imageRes),
                                    contentDescription = spot.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                )
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(spot.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(spot.description, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            val searchResults = allItems.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }

            item {
                Text("Search Results", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            }

            items(searchResults) { item ->
                Card(
                    onClick = { onCategoryClick(item.title) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                            Text(item.description, fontSize = 12.sp, color = Color.Gray)
                        }
                        Text("🔊 Voice", fontSize = 11.sp, color = Color(0xFFF5B041), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCardTile(category: TourismCategory, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.width(105.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(category.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(category.emoji, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun BatangasInteractiveMapView(onSpotSelected: (String) -> Unit) {
    val spots = listOf(
        MapSpot("Laiya White Sand Beach", "San Juan, Batangas", "Beaches", "🏖️"),
        MapSpot("Taal Basilica & Heritage Town", "Taal, Batangas", "Heritage", "🏛️"),
        MapSpot("Anilao Marine Sanctuary", "Mabini, Batangas", "Diving", "🤿"),
        MapSpot("Lipa Cathedral & Kapeng Barako", "Lipa City, Batangas", "Food & Cafe", "☕")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F4F4))
            .padding(16.dp)
    ) {
        Text("Interactive Tourism Map", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Text("Geographically accurate map of Batangas destination pins.", fontSize = 12.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF76D7EA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val batangasLand = Path().apply {
                        moveTo(0f, h * 0.1f)
                        lineTo(w, h * 0.1f)
                        lineTo(w, h * 0.75f)
                        quadraticTo(w * 0.75f, h * 0.95f, w * 0.6f, h * 0.75f)
                        lineTo(w * 0.5f, h * 0.65f)
                        quadraticTo(w * 0.35f, h * 0.95f, w * 0.28f, h * 0.95f)
                        quadraticTo(w * 0.2f, h * 0.7f, w * 0.35f, h * 0.58f)
                        quadraticTo(w * 0.1f, h * 0.65f, 0f, h * 0.55f)
                        close()
                    }
                    drawPath(path = batangasLand, color = Color(0xFFD4EFDF))

                    val lakeX = w * 0.38f
                    val lakeY = h * 0.25f
                    val lakeW = w * 0.24f
                    val lakeH = h * 0.22f

                    drawOval(
                        color = Color(0xFF5DADE2),
                        topLeft = Offset(lakeX, lakeY),
                        size = Size(lakeW, lakeH)
                    )

                    drawCircle(
                        color = Color(0xFF27AE60),
                        radius = 10f,
                        center = Offset(lakeX + (lakeW * 0.5f), lakeY + (lakeH * 0.5f))
                    )

                    drawOval(
                        color = Color(0xFFABEBC6),
                        topLeft = Offset(w * 0.22f, h * 0.88f),
                        size = Size(w * 0.12f, h * 0.05f)
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍 Batangas Map View", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF1B4F72))
                    }
                }

                MapPinButton(
                    emoji = "☕",
                    label = "Lipa City",
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 40.dp, end = 24.dp),
                    onClick = { onSpotSelected("Food & Cafe") }
                )

                MapPinButton(
                    emoji = "🏛️",
                    label = "Taal Town",
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 70.dp, bottom = 20.dp),
                    onClick = { onSpotSelected("Heritage") }
                )

                MapPinButton(
                    emoji = "🤿",
                    label = "Anilao",
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 45.dp, bottom = 12.dp),
                    onClick = { onSpotSelected("Diving") }
                )

                MapPinButton(
                    emoji = "🏖️",
                    label = "Laiya Beach",
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 24.dp),
                    onClick = { onSpotSelected("Beaches") }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Key Destinations", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(spots) { spot ->
                Card(
                    onClick = { onSpotSelected(spot.category) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(spot.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(spot.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(spot.location, fontSize = 12.sp, color = Color.Gray)
                        }
                        Text("🔊 Play", fontSize = 12.sp, color = Color(0xFFF5B041), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MapPinButton(emoji: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
fun ChatGuideView(
    messages: List<ChatMessage>,
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onPlayVoice: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                val bgColor = if (msg.isUser) Color(0xFFF5B041) else Color(0xFFEAECEE)
                val textColor = if (msg.isUser) Color.White else Color.Black

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
                    Surface(
                        color = bgColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = msg.text, fontSize = 14.sp, color = textColor)
                            if (!msg.isUser) {
                                Spacer(modifier = Modifier.height(6.dp))
                                TextButton(
                                    onClick = { onPlayVoice(msg.text) },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("🔊 Replay Voice", fontSize = 11.sp, color = Color(0xFF1B4F72))
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = onUserInputChange,
                placeholder = { Text("Ask BatangasConnect AI...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onSend,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5B041))
            ) {
                Text("Send", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun generateNaturalTourismResponse(query: String): String {
    val q = query.lowercase().trim()

    return when {
        q.contains("food") || q.contains("cafe") || q.contains("eat") || q.contains("lomi") || q.contains("coffee") || q.contains("barako") || q.contains("goto") || q.contains("bulalo") || q.contains("tapa") ->
            "Here are five top food recommendations in Batangas: 1. Sizzling Batangas Lomi, 2. Freshly brewed Kapeng Barako, 3. Authentic Goto Batangas, 4. Hot Batangas Bulalo, and 5. Marinated Tapa Taal!"

        q.contains("beach") || q.contains("island") || q.contains("sand") || q.contains("swim") || q.contains("laiya") || q.contains("calatagan") || q.contains("masasa") || q.contains("lighthouse") || q.contains("fortune") ->
            "Here are top beach destinations in Batangas: 1. Laiya White Sand Beach, 2. Masasa Beach in Tingloy, 3. Calatagan Beach, 4. Fortune Island, and 5. Verde Island!"

        q.contains("heritage") || q.contains("taal") || q.contains("basilica") || q.contains("cathedral") ->
            "Here are famous heritage spots: 1. Taal Basilica, 2. San Sebastian Cathedral, 3. Casa de Segunda, 4. Cape Santiago Lighthouse, and 5. Apacible Museum!"

        q.contains("resort") ->
            "Here are top resorts in Batangas: 1. The Farm at San Benito, 2. Acellaya Beach Resort, 3. Stilts Calatagan, 4. Matabungkay Resort, and 5. Pico de Loro Cove!"

        q.contains("dive") || q.contains("diving") || q.contains("coral") || q.contains("anilao") ->
            "Here are five world-class dive spots: 1. Anilao Marine Sanctuary, 2. Twin Rocks Reef, 3. Cathedral Rock underwater cross, 4. Mainit Point drift dive, and 5. Sombrero Island Reef!"

        q.contains("culture") || q.contains("activity") || q.contains("activities") ->
            "Here are top outdoor and cultural activities: 1. Sublian Festival traditions, 2. Balisong crafting in Taal, 3. Mount Maculot trekking, 4. Taal Bordado embroidery, and 5. Mount Gulugod Baboy hiking!"

        else ->
            "Batangas offers amazing spots! Explore our tourist places, local foods, and outdoor activities anytime!"
    }
}