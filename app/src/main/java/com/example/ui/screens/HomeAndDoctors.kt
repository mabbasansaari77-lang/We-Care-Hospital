package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppointmentEntity
import com.example.data.DoctorEntity
import com.example.data.UserEntity
import com.example.viewmodel.AppointmentViewModel
import java.text.NumberFormat
import java.util.*

// --- Home Screen ---
@Composable
fun HomeScreen(
    viewModel: AppointmentViewModel,
    onNavigateToDoctorList: () -> Unit,
    onNavigateToDoctorDetails: (DoctorEntity) -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAppointments: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val doctors by viewModel.filteredDoctors.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header bar
            HomeHeaderBar(user = user, onNotifClick = onNavigateToNotifications)

            when (user?.role) {
                "doctor" -> {
                    DoctorDashboardSection(
                        user = user!!,
                        appointments = appointments,
                        viewModel = viewModel
                    )
                }
                "admin" -> {
                    AdminDashboardSection(
                        user = user!!,
                        appointments = appointments,
                        doctors = viewModel.doctorsList.collectAsState().value,
                        viewModel = viewModel,
                        onNavigateToDoctorDetails = onNavigateToDoctorDetails
                    )
                }
                else -> {
                    PatientDashboardSection(
                        user = user,
                        appointments = appointments,
                        doctors = doctors,
                        viewModel = viewModel,
                        onSeeAllDoctors = onNavigateToDoctorList,
                        onNavigateToDoctorDetails = onNavigateToDoctorDetails,
                        onNavigateToAppointments = onNavigateToAppointments
                    )
                }
            }
        }
    }
}

@Composable
fun HomeHeaderBar(
    user: UserEntity?,
    onNotifClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                color = Color.LightGray
            ) {
                AsyncImage(
                    model = if (user?.profileImage?.isNotEmpty() == true) user.profileImage else "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150",
                    contentDescription = "User Headshot Avatar",
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Welcome back 👋",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = user?.name ?: "Guest User",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextDefault
                )
            }
        }

        IconButton(
            onClick = onNotifClick,
            modifier = Modifier
                .background(Color.White, CircleShape)
                .testTag("home_notif_icon_btn")
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Bell Indicator Notification Link",
                tint = BluePrimary
            )
        }
    }
}

// --- Patient Dashboard ---
@Composable
fun PatientDashboardSection(
    user: UserEntity?,
    appointments: List<AppointmentEntity>,
    doctors: List<DoctorEntity>,
    viewModel: AppointmentViewModel,
    onSeeAllDoctors: () -> Unit,
    onNavigateToDoctorDetails: (DoctorEntity) -> Unit,
    onNavigateToAppointments: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val featuredSpecs = listOf("All", "Cardiology", "Pediatrics", "Dermatology", "Neurology", "Orthopedics")
    val selectedSpec by viewModel.selectedSpecialization.collectAsState()

    val nextUpcoming = appointments.firstOrNull { it.status == "upcoming" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Search & Book Header Row
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.updateSearchQuery(it)
                },
                placeholder = { Text("Search specialized general practitioners, clinics...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, "Search Icon", tint = BluePrimary) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_field"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = SlateTextDefault,
                    unfocusedTextColor = SlateTextDefault
                )
            )
        }

        // Promotional Banner Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BluePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Special Clinic Day 🩺",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Get up to 30% discount on out-of-pocket checkups this weekend.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }

        // Specialization Quick Badges
        item {
            Column {
                Text(
                    text = "Specialties",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextDefault,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(featuredSpecs) { spec ->
                        val isSelected = selectedSpec.equals(spec, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateSpecialization(spec) },
                            label = { Text(spec) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("spec_filter_chip_$spec")
                        )
                    }
                }
            }
        }

        // Next Upcoming Appointment Card
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Upcoming Appointment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextDefault
                    )
                    TextButton(onClick = onNavigateToAppointments) {
                        Text("See All", color = BluePrimary)
                    }
                }

                if (nextUpcoming != null) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToAppointments() }
                            .testTag("next_upcoming_appt_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                color = Color.LightGray
                            ) {
                                AsyncImage(
                                    model = nextUpcoming.doctorImage,
                                    contentDescription = nextUpcoming.doctorName,
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nextUpcoming.doctorName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextDefault
                                )
                                Text(
                                    text = nextUpcoming.doctorSpecialization,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarToday, "Calendar", modifier = Modifier.size(14.dp), tint = BluePrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${nextUpcoming.date} • ${nextUpcoming.time}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BluePrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.cancelAppointment(nextUpcoming.id) }) {
                                Icon(Icons.Default.Cancel, "Cancel Booking", tint = Color.Red.copy(alpha = 0.7f))
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.EventNote, "Empty Appointment Note", modifier = Modifier.size(40.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No upcoming appointments found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Recommended Doctors Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Our Doctors",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextDefault
                )
                TextButton(onClick = onSeeAllDoctors) {
                    Text("Filter Spec", color = BluePrimary)
                }
            }
        }

        items(doctors.take(4)) { doc ->
            DoctorCard(doctor = doc, onClick = { onNavigateToDoctorDetails(doc) })
        }
    }
}

// --- Doctor Dashboard ---
@Composable
fun DoctorDashboardSection(
    user: UserEntity,
    appointments: List<AppointmentEntity>,
    viewModel: AppointmentViewModel
) {
    val totalEarnings = appointments.filter { it.status == "completed" }.sumOf { it.fee }
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayCount = appointments.filter { it.date == todayDateStr }.size
    val totalPatientsCount = appointments.distinctBy { it.patientId }.size

    var writePrescriptionNotesApptId by remember { mutableStateOf<String?>(null) }
    var rxInputNotes by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Stats Cards grid
        item {
            Column {
                Text(
                    "Doctor Hub Operations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextDefault,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("Earnings", "$${totalEarnings.toInt()}", Icons.Default.MonetizationOn, Modifier.weight(1f))
                    StatCard("Today Appts", "$todayCount", Icons.Default.Assignment, Modifier.weight(1f))
                    StatCard("Patients", "$totalPatientsCount", Icons.Default.Groups, Modifier.weight(1f))
                }
            }
        }

        // Active Patient Consult Queue
        item {
            Text(
                "Patient Consult Bookings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateTextDefault
            )
        }

        val nonCancelled = appointments.filter { it.status != "cancelled" }
        if (nonCancelled.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No active consultation booking queries", color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(nonCancelled) { appt ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("doc_appt_queue_card_${appt.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = appt.patientName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextDefault
                                )
                                Text(
                                    text = "Date: ${appt.date} • Check: ${appt.time}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                                if (appt.notes.isNotEmpty()) {
                                    Text(
                                        text = "Client notes: \"${appt.notes}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BluePrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Badge(
                                containerColor = when (appt.status) {
                                    "completed" -> MintSecondary.copy(alpha = 0.15f)
                                    "upcoming" -> BluePrimary.copy(alpha = 0.15f)
                                    else -> Color.LightGray
                                }
                            ) {
                                Text(
                                    appt.status.uppercase(),
                                    color = when (appt.status) {
                                        "completed" -> MintSecondary
                                        "upcoming" -> BluePrimary
                                        else -> Color.DarkGray
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (appt.status == "upcoming") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { writePrescriptionNotesApptId = appt.id },
                                    colors = ButtonDefaults.buttonColors(containerColor = MintSecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("complete_consult_btn_${appt.id}")
                                ) {
                                    Icon(Icons.Default.HealthAndSafety, "Complete Consult")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mark Complete", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.cancelAppointment(appt.id) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("decline_consult_btn_${appt.id}")
                                ) {
                                    Text("Decline", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Prescription Upload Simulation Modal dialog sheet
    if (writePrescriptionNotesApptId != null) {
        AlertDialog(
            onDismissRequest = { writePrescriptionNotesApptId = null },
            title = { Text("Compile Digital Consult Recipe") },
            text = {
                Column {
                    Text("Provide digital prescriptions and diagnosis review updates for patient", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rxInputNotes,
                        onValueChange = { rxInputNotes = it },
                        placeholder = { Text("Take anti-histamines once daily. Penicillin allergens check...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("rx_input_notes_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val apptId = writePrescriptionNotesApptId!!
                        viewModel.completeAppointment(apptId, rxInputNotes, "rx_physician_${apptId}.pdf")
                        rxInputNotes = ""
                        writePrescriptionNotesApptId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Issue Prescription & Lock")
                }
            },
            dismissButton = {
                TextButton(onClick = { writePrescriptionNotesApptId = null }) { Text("Cancel") }
            }
        )
    }
}

// --- Admin Dashboard ---
@Composable
fun AdminDashboardSection(
    user: UserEntity,
    appointments: List<AppointmentEntity>,
    doctors: List<DoctorEntity>,
    viewModel: AppointmentViewModel,
    onNavigateToDoctorDetails: (DoctorEntity) -> Unit
) {
    var adminViewTab by remember { mutableIntStateOf(1) } // 1: Stats, 2: Doctor Base Management, 3: Active Appointment controls

    // Admin Doctor addition fields
    var isAddingDoctor by remember { mutableStateOf(false) }
    var adName by remember { mutableStateOf("") }
    var adSpec by remember { mutableStateOf("Cardiology") }
    var adQual by remember { mutableStateOf("") }
    var adExp by remember { mutableStateOf("") }
    var adMedHospital by remember { mutableStateOf("St. Mary's General Hospital") }
    var adFee by remember { mutableStateOf("150") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = adminViewTab - 1,
            containerColor = BgLight,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Tab(selected = adminViewTab == 1, onClick = { adminViewTab = 1 }) {
                Text("Stats", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (adminViewTab == 1) BluePrimary else Color.Gray)
            }
            Tab(selected = adminViewTab == 2, onClick = { adminViewTab = 2 }) {
                Text("Doctors", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (adminViewTab == 2) BluePrimary else Color.Gray)
            }
            Tab(selected = adminViewTab == 3, onClick = { adminViewTab = 3 }) {
                Text("Bookings", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (adminViewTab == 3) BluePrimary else Color.Gray)
            }
        }

        when (adminViewTab) {
            1 -> {
                val totalRevenue = appointments.filter { it.status == "completed" }.sumOf { it.fee }
                val totalAppointments = appointments.size
                val doctorCount = doctors.size
                val userCount = appointments.distinctBy { it.patientId }.size + 1 // Add at least one patient user

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard("Revenue", "$${totalRevenue.toInt()}", Icons.Default.Insights, Modifier.weight(1f))
                        StatCard("Bookings", "$totalAppointments", Icons.Default.Event, Modifier.weight(1f))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard("Active Docs", "$doctorCount", Icons.Default.MedicalServices, Modifier.weight(1f))
                        StatCard("Patients Base", "$userCount", Icons.Default.Group, Modifier.weight(1f))
                    }

                    // Simulated interactive graphical revenue distribution card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Revenue Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Simple chart columns
                                listOf(35, 60, 48, 80, 100, 75).forEachIndexed { i, hPct ->
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(hPct / 100f)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (i == 4) MintSecondary else BluePrimary)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")[i], fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hospital Doctors Base", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { isAddingDoctor = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            modifier = Modifier.testTag("admin_add_doctor_trigger_btn")
                        ) {
                            Icon(Icons.Default.Add, "Add Doctor")
                            Text("Add Doctor")
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(doctors) { doc ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape),
                                        color = Color.LightGray
                                    ) {
                                        AsyncImage(model = doc.profileImage, contentDescription = doc.name, contentScale = ContentScale.Crop)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(doc.name, fontWeight = FontWeight.Bold, color = SlateTextDefault)
                                        Text(doc.specialization, style = MaterialTheme.typography.bodySmall, color = BluePrimary)
                                        Text(doc.hospitalName, fontSize = 11.sp, color = Color.Gray)
                                    }
                                    IconButton(
                                        onClick = { viewModel.adminDeleteDoctor(doc.id) },
                                        modifier = Modifier.testTag("admin_delete_doctor_btn_${doc.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(appointments) { appt ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Patient: ${appt.patientName}", fontWeight = FontWeight.Bold, color = SlateTextDefault)
                                        Text("Doctor: ${appt.doctorName} (${appt.doctorSpecialization})", style = MaterialTheme.typography.bodySmall)
                                        Text("Schedule: ${appt.date} • ${appt.time}", fontSize = 11.sp, color = BluePrimary)
                                    }
                                    Badge(containerColor = if (appt.status == "cancelled") Color.Red.copy(alpha = 0.1f) else MintSecondary.copy(alpha = 0.1f)) {
                                        Text(appt.status.uppercase(), color = if (appt.status == "cancelled") Color.Red else MintSecondary, modifier = Modifier.padding(horizontal = 6.dp))
                                    }
                                }
                                if (appt.status == "upcoming") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.adminCancelAppointment(appt.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("admin_cancel_booking_btn_${appt.id}")
                                        ) {
                                            Text("Cancel", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        // Quick doctor reassignment menu simulation
                                        var showReassignDialog by remember { mutableStateOf(false) }
                                        Button(
                                            onClick = { showReassignDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("admin_reassign_btn_${appt.id}")
                                        ) {
                                            Text("Reassign", fontSize = 12.sp)
                                        }

                                        if (showReassignDialog) {
                                            AlertDialog(
                                                onDismissRequest = { showReassignDialog = false },
                                                title = { Text("Reassign Consultation Doctor") },
                                                text = {
                                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        Text("Select new practicing physician to care for this booking:", color = Color.Gray, fontSize = 13.sp)
                                                        doctors.filter { it.id != appt.doctorId }.forEach { itemDoc ->
                                                            Card(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clickable {
                                                                        viewModel.adminReassignDoctor(appt.id, itemDoc.id)
                                                                        showReassignDialog = false
                                                                    }
                                                            ) {
                                                                Text(itemDoc.name, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                },
                                                confirmButton = {},
                                                dismissButton = { TextButton(onClick = { showReassignDialog = false }) { Text("Dismiss") } }
                                            )
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

    // Modal adding Doctor Dialog form
    if (isAddingDoctor) {
        AlertDialog(
            onDismissRequest = { isAddingDoctor = false },
            title = { Text("Register New Doctor") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(value = adName, onValueChange = { adName = it }, label = { Text("Physician Full Name") }, modifier = Modifier.fillMaxWidth().testTag("add_doc_name"))
                    
                    // Specialty
                    Text("Specialization Category:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Cardiology", "Pediatrics", "Dermatology", "Neurology").forEach { specCat ->
                            FilterChip(
                                selected = adSpec == specCat,
                                onClick = { adSpec = specCat },
                                label = { Text(specCat, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(value = adQual, onValueChange = { adQual = it }, label = { Text("Qualifications (eg. MD)") }, modifier = Modifier.fillMaxWidth().testTag("add_doc_qualification"))
                    OutlinedTextField(value = adExp, onValueChange = { adExp = it }, label = { Text("Experience Years (eg. 12 Years)") }, modifier = Modifier.fillMaxWidth().testTag("add_doc_experience"))
                    OutlinedTextField(value = adMedHospital, onValueChange = { adMedHospital = it }, label = { Text("Affiliated Hospital Location") }, modifier = Modifier.fillMaxWidth().testTag("add_doc_hospital"))
                    OutlinedTextField(value = adFee, onValueChange = { adFee = it }, label = { Text("Fee ($ USD)") }, modifier = Modifier.fillMaxWidth().testTag("add_doc_fee"))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedFee = adFee.toDoubleOrNull() ?: 150.0
                        if (adName.isNotEmpty() && adQual.isNotEmpty()) {
                            viewModel.adminAddDoctor(
                                name = adName,
                                specialization = adSpec,
                                qualification = adQual,
                                experience = adExp,
                                hospitalName = adMedHospital,
                                consultationFee = parsedFee
                            )
                            // Clear & Dismiss
                            adName = ""
                            adQual = ""
                            adExp = ""
                            isAddingDoctor = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Saves & Publish")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddingDoctor = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun StatCard(label: String, valText: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = label, tint = BluePrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = valText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

// --- Doctor list screen ---
@Composable
fun DoctorListScreen(
    viewModel: AppointmentViewModel,
    onNavigateToDoctorDetails: (DoctorEntity) -> Unit,
    onNavigateBack: () -> Unit
) {
    val doctors by viewModel.filteredDoctors.collectAsState()
    var searchKey by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BgLight,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Arrow")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Practice Specialist", color = SlateTextDefault, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchKey,
                onValueChange = {
                    searchKey = it
                    viewModel.updateSearchQuery(it)
                },
                placeholder = { Text("Filter doctors, specialty or clinics name...") },
                leadingIcon = { Icon(Icons.Default.Search, "Search Icon", tint = BluePrimary) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("doc_list_search_input"),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (doctors.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, "No matching doctors", modifier = Modifier.size(48.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No doctors match your query", color = Color.Gray)
                        }
                    }
                } else {
                    items(doctors) { doc ->
                        DoctorCard(doctor = doc, onClick = { onNavigateToDoctorDetails(doc) })
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorCard(
    doctor: DoctorEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("doctor_card_${doctor.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Color.LightGray
            ) {
                AsyncImage(
                    model = doctor.profileImage,
                    contentDescription = doctor.name,
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextDefault
                )
                Text(
                    text = doctor.specialization,
                    style = MaterialTheme.typography.bodySmall,
                    color = BluePrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = doctor.hospitalName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(doctor.rating.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "$${doctor.consultationFee.toInt()} consult fee",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, "View Details", tint = Color.LightGray)
        }
    }
}

// --- Doctor Details Screen ---
@Composable
fun DoctorDetailsScreen(
    doctor: DoctorEntity,
    onNavigateToBooking: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Elegant Doctor Top details banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(BluePrimary)
            ) {
                AsyncImage(
                    model = doctor.profileImage,
                    contentDescription = doctor.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Navigation top controls row over visual image
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back Arrow Button", tint = Color.White)
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-24).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = BgLight
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = doctor.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextDefault
                    )
                    Text(
                        text = "${doctor.specialization} • ${doctor.experience} Experience",
                        style = MaterialTheme.typography.titleMedium,
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailBadge("Rating", "${doctor.rating} ⭐", Modifier.weight(1f))
                        DetailBadge("Affiliated", doctor.hospitalName, Modifier.weight(1f))
                        DetailBadge("Consult Fee", "$${doctor.consultationFee.toInt()}", Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Physician Credentials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = doctor.qualification,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Operational Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        doctor.availableDays.split(",").forEach { day ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(day.trim(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Available Slots", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        doctor.availableSlots.split(",").take(4).forEach { slot ->
                            Badge(containerColor = LightBlueCard) {
                                Text(slot.trim(), color = BluePrimary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = onNavigateToBooking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("book_consult_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Initiate Appointment Booking", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailBadge(valHeader: String, valBody: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valHeader, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(valBody, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

// --- Appointment Booking Details Confirm Screen ---
@Composable
fun AppointmentBookingScreen(
    viewModel: AppointmentViewModel,
    doctor: DoctorEntity,
    onBookingSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var reasonNote by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }

    val daysMockList = remember {
        val list = mutableListOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd (E)", Locale.getDefault())
        val cal = Calendar.getInstance()
        repeat(7) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val availableTimes = doctor.availableSlots.split(",")

    Scaffold(
        containerColor = BgLight,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Arrow")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Consultancy Slot", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SlateTextDefault)
            }
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mini Doctor Card review item
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color.LightGray
                    ) {
                        AsyncImage(model = doctor.profileImage, contentDescription = "", contentScale = ContentScale.Crop)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(doctor.name, fontWeight = FontWeight.Bold, color = SlateTextDefault)
                        Text(doctor.specialization, style = MaterialTheme.typography.bodySmall, color = BluePrimary)
                    }
                }
            }

            // Date picker
            Text("Select Date & Time Slot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(daysMockList) { mockDate ->
                    val isSelected = selectedDate == mockDate
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedDate = mockDate },
                        label = { Text(mockDate) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("date_chip_$mockDate")
                    )
                }
            }

            // Time Slots flow
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableTimes) { mockTime ->
                    val cleanTime = mockTime.trim()
                    val isSelected = selectedTime == cleanTime
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTime = cleanTime },
                        label = { Text(cleanTime) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MintSecondary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("time_chip_$cleanTime")
                    )
                }
            }

            // Consultation reason input Notes section
            Text("Medical Consultation Motive", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)
            OutlinedTextField(
                value = reasonNote,
                onValueChange = { reasonNote = it },
                placeholder = { Text("Describe details of symptoms, headache logs, checkups...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .testTag("booking_reason_notes_input"),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            // Price list Breakdown Receipt review
            Card(
                colors = CardDefaults.cardColors(containerColor = LightBlueCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Outpatient consultation fee:", color = Color.DarkGray)
                        Text("$${doctor.consultationFee.toInt()}", fontWeight = FontWeight.Bold, color = SlateTextDefault)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Service delivery taxing details:", color = Color.Gray)
                        Text("FREE", color = MintSecondary, fontWeight = FontWeight.Bold)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total Payable:", fontWeight = FontWeight.Bold, color = SlateTextDefault)
                        Text("$${doctor.consultationFee.toInt()}", color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (selectedDate.isNotEmpty() && selectedTime.isNotEmpty()) {
                        viewModel.lockAppointment(
                            doctor = doctor,
                            date = selectedDate.substringBefore(" ("), // Trim the day tag
                            time = selectedTime,
                            notes = reasonNote,
                            onSuccess = onBookingSaved
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("booking_confirm_purchase_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedDate.isNotEmpty() && selectedTime.isNotEmpty()
            ) {
                Text("Confirm Slot & Purchase Consult", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
