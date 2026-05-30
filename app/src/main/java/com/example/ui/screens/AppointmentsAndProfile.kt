package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppointmentEntity
import com.example.data.NotificationEntity
import com.example.data.PrescriptionEntity
import com.example.data.UserEntity
import com.example.viewmodel.AppointmentViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- My Appointments Screen ---
@Composable
fun MyAppointmentsScreen(
    viewModel: AppointmentViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val appointments by viewModel.appointments.collectAsState()
    val prescriptions by viewModel.prescriptions.collectAsState()

    var activeTab by remember { mutableIntStateOf(1) } // 1: Upcoming, 2: Completed, 3: Cancelled

    var reschedulingApptId by remember { mutableStateOf<String?>(null) }
    var reschedDate by remember { mutableStateOf("") }
    var reschedTime by remember { mutableStateOf("") }

    val daysMockList = remember {
        val list = mutableListOf<String>()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        repeat(5) {
            list.add(sdf.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val timesMockList = listOf("09:00 AM", "11:00 AM", "01:30 PM", "03:00 PM")

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
                Text("My Consultations & Recipes", color = SlateTextDefault, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(horizontal = 16.dp)
        ) {
            TabRow(
                selectedTabIndex = activeTab - 1,
                containerColor = BgLight,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                    Text("Upcoming", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (activeTab == 1) BluePrimary else Color.Gray)
                }
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }) {
                    Text("Completed", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (activeTab == 2) BluePrimary else Color.Gray)
                }
                Tab(selected = activeTab == 3, onClick = { activeTab = 3 }) {
                    Text("Cancelled", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if (activeTab == 3) BluePrimary else Color.Gray)
                }
            }

            val filteredList = when (activeTab) {
                1 -> appointments.filter { it.status == "upcoming" }
                2 -> appointments.filter { it.status == "completed" }
                else -> appointments.filter { it.status == "cancelled" }
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CalendarToday, "No Appointments icon", modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No consultations found in this category", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { appt ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("appt_management_card_${appt.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        color = Color.LightGray
                                    ) {
                                        AsyncImage(model = appt.doctorImage, contentDescription = "", contentScale = ContentScale.Crop)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(appt.doctorName, fontWeight = FontWeight.Bold, color = SlateTextDefault)
                                        Text(appt.doctorSpecialization, style = MaterialTheme.typography.bodySmall, color = BluePrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Date: ${appt.date} • ${appt.time}", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(
                                        "$${appt.fee.toInt()}",
                                        color = BluePrimary,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                // If Completed, find and present prescription card notes
                                if (activeTab == 2) {
                                    val matchingRx = prescriptions.firstOrNull { it.appointmentId == appt.id }
                                    if (matchingRx != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = BgLight),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Receipt, "Rx Prescriptions Icon", tint = MintSecondary)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Physician Instructions Required:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SlateTextDefault)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "\"${matchingRx.notes}\"",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.DarkGray
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("Attachment: ${matchingRx.prescriptionFile}", fontSize = 10.sp, color = BluePrimary)
                                                    IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                                                        Icon(Icons.Default.Download, "Download prescription file", modifier = Modifier.size(18.dp), tint = BluePrimary)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (activeTab == 1) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.cancelAppointment(appt.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("patient_cancel_booking_btn_${appt.id}")
                                        ) {
                                            Text("Cancel Booking", fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { reschedulingApptId = appt.id },
                                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("patient_reschedule_btn_${appt.id}")
                                        ) {
                                            Text("Reschedule", fontSize = 12.sp)
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

    // Reschedule Interactive Dialog Picker
    if (reschedulingApptId != null) {
        AlertDialog(
            onDismissRequest = { reschedulingApptId = null },
            title = { Text("Reschedule Consult Booking") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select a new calendar date slot:", color = Color.Gray, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(daysMockList) { mDate ->
                            FilterChip(
                                selected = reschedDate == mDate,
                                onClick = { reschedDate = mDate },
                                label = { Text(mDate, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Select a new timing slot:", color = Color.Gray, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(timesMockList) { mTime ->
                            FilterChip(
                                selected = reschedTime == mTime,
                                onClick = { reschedTime = mTime },
                                label = { Text(mTime, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val apptId = reschedulingApptId!!
                        if (reschedDate.isNotEmpty() && reschedTime.isNotEmpty()) {
                            viewModel.rescheduleAppointment(apptId, reschedDate, reschedTime)
                            // Reset & dismiss
                            reschedulingApptId = null
                            reschedDate = ""
                            reschedTime = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                    enabled = reschedDate.isNotEmpty() && reschedTime.isNotEmpty()
                ) {
                    Text("Confirm reschedule")
                }
            },
            dismissButton = {
                TextButton(onClick = { reschedulingApptId = null }) { Text("Cancel") }
            }
        )
    }
}

// --- Notifications Screen ---
@Composable
fun NotificationsScreen(
    viewModel: AppointmentViewModel,
    onNavigateBack: () -> Unit
) {
    val nonifs by viewModel.notifications.collectAsState()

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
                Text("System Notifications", color = SlateTextDefault, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    ) { padding ->
        if (nonifs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, "Empty notifications icon", modifier = Modifier.size(52.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No notifications received yet", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(nonifs) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(BluePrimary.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.NotificationImportant, "Notif Bell Alert", tint = BluePrimary, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontWeight = FontWeight.Bold, color = SlateTextDefault, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(item.message, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.date, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Profile Screen ---
@Composable
fun ProfileScreen(
    viewModel: AppointmentViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()

    // Form inputs state
    var formName by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    
    // Dynamic Patients specific attributes
    var bloodGroup by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var medicalHistory by remember { mutableStateOf("") }

    // Dynamic Doctor specific attributes
    var specialization by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var feeStr by remember { mutableStateOf("") }
    var qualification by remember { mutableStateOf("") }

    var isSavedConfirmMsg by remember { mutableStateOf(false) }

    LaunchedEffect(user) {
        user?.let {
            formName = it.name
            formPhone = it.phone
            bloodGroup = it.bloodGroup
            allergies = it.allergies
            medicalHistory = it.medicalHistory
            specialization = it.specialization
            experience = it.experience
            feeStr = if (it.consultationFee > 0.0) it.consultationFee.toInt().toString() else "100"
            qualification = it.qualification
        }
    }

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
                Text("Edit Professional File", color = SlateTextDefault, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile dynamic visual card image
            val currentUserVal = user
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = if (currentUserVal?.profileImage?.isNotEmpty() == true) currentUserVal.profileImage else "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&q=80&w=150",
                    contentDescription = "User Headshot Avatars",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                "Basic Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateTextDefault
            )

            OutlinedTextField(
                value = formName,
                onValueChange = { formName = it },
                label = { Text("Legal Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_name_field"),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            OutlinedTextField(
                value = formPhone,
                onValueChange = { formPhone = it },
                label = { Text("Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_phone_field"),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
            )

            // Dynamic Form layout depending on roles (Patient / Doctor)
            if (user?.role == "doctor") {
                Text("Practicing Qualifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)
                
                OutlinedTextField(
                    value = specialization,
                    onValueChange = { specialization = it },
                    label = { Text("Specialty Class (eg. Dermatology)") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_doc_spec_field"),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                OutlinedTextField(
                    value = qualification,
                    onValueChange = { qualification = it },
                    label = { Text("Licenses & Academics") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_doc_qual_field"),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = experience,
                        onValueChange = { experience = it },
                        label = { Text("XP Years") },
                        modifier = Modifier.weight(1f).testTag("profile_doc_xp_field"),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )
                    OutlinedTextField(
                        value = feeStr,
                        onValueChange = { feeStr = it },
                        label = { Text("Fee ($ USD)") },
                        modifier = Modifier.weight(1f).testTag("profile_doc_fee_field"),
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                    )
                }
            } else {
                Text("Medical Log Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)
                
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = { bloodGroup = it },
                    label = { Text("Blood Group (eg. O-Positive)") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_patient_blood_field"),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Known Allergies & Penicillins") },
                    modifier = Modifier.fillMaxWidth().testTag("profile_patient_allergies_field"),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )

                OutlinedTextField(
                    value = medicalHistory,
                    onValueChange = { medicalHistory = it },
                    label = { Text("Past Medical History Logs") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("profile_patient_history_field"),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val feeDbl = feeStr.toDoubleOrNull() ?: 100.0
                    viewModel.editProfile(
                        name = formName,
                        phone = formPhone,
                        bloodGroup = bloodGroup,
                        allergies = allergies,
                        medicalHistory = medicalHistory,
                        specialization = specialization,
                        qualification = qualification,
                        experience = experience,
                        consultationFee = feeDbl,
                        onSuccess = {
                            isSavedConfirmMsg = true
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("profile_save_changes_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Save & Update File Record", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (isSavedConfirmMsg) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MintSecondary.copy(alpha = 0.15f))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, "Save Successful Checklist icon", tint = MintSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Profile medical stats updated successfully!", color = MintSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

// --- Settings Screen ---
@Composable
fun SettingsScreen(
    viewModel: AppointmentViewModel,
    onNavigateToProfile: () -> Unit,
    onLoggedOut: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var isDarkThemeLocal by remember { mutableStateOf(false) }

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
                Text("Settings & Privacy", color = SlateTextDefault, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("General Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)

            // Dynamic Medical Profile Link Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProfile() }
                    .testTag("settings_goto_profile_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountCircle, "Profile Link Icon", tint = BluePrimary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Medical & Professional Profile", fontWeight = FontWeight.Bold)
                        Text("Manage credentials, diseases, allergies, and fee rates", fontSize = 11.sp, color = Color.Gray)
                    }
                    Icon(Icons.Default.ChevronRight, "Chevron Direction Right View Link", tint = Color.LightGray)
                }
            }

            // Dark Mode switch Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Lightbulb, "Dark Mode Toggle icon", tint = BluePrimary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Force Dark Layout Theme", fontWeight = FontWeight.Bold)
                            Text("Render custom colors optimal for night sight", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Switch(
                        checked = isDarkThemeLocal,
                        onCheckedChange = { isDarkThemeLocal = it },
                        modifier = Modifier.testTag("settings_dark_mode_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Support Care Desk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SlateTextDefault)

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LiveHelp, "Support Hotline help desk icon", tint = MintSecondary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Intercom Medical Hotline: +1 (800) 555-CARE", fontSize = 13.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, "Version terms desk icon", tint = MintSecondary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Application build version: 1.0.4-WeCare-Pro", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout red Active CTA button
            Button(
                onClick = {
                    viewModel.logout(onLoggedOut)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("logout_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, "Logout Door Link icon", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of We Care", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
