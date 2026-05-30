package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.network.UpdateProfileRequest
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppointmentViewModel(application: Application, private val repository: Repository, val db: AppDatabase) : AndroidViewModel(application) {

    // --- Core Authenticative States ---
    val currentUser: StateFlow<UserEntity?> = repository.loggedInUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val doctorsList: StateFlow<List<DoctorEntity>> = repository.allDoctors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Appointments for Currently Active Role ---
    private val _appointmentsState = MutableStateFlow<List<AppointmentEntity>>(emptyList())
    val appointments: StateFlow<List<AppointmentEntity>> = _appointmentsState.asStateFlow()

    private val _prescriptionsState = MutableStateFlow<List<PrescriptionEntity>>(emptyList())
    val prescriptions: StateFlow<List<PrescriptionEntity>> = _prescriptionsState.asStateFlow()

    // --- Support Dynamic Filtering for Doctors ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedSpecialization = MutableStateFlow("All")
    val selectedSpecialization = _selectedSpecialization.asStateFlow()

    val filteredDoctors: StateFlow<List<DoctorEntity>> = combine(doctorsList, searchQuery, selectedSpecialization) { doctors, query, spec ->
        doctors.filter { doc ->
            val matchesQuery = doc.name.contains(query, ignoreCase = true) || 
                               doc.hospitalName.contains(query, ignoreCase = true) ||
                               doc.specialization.contains(query, ignoreCase = true)
            val matchesSpec = spec == "All" || doc.specialization.equals(spec, ignoreCase = true)
            matchesQuery && matchesSpec
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Selected Doctor for Details & Bookings ---
    private val _selectedDoctor = MutableStateFlow<DoctorEntity?>(null)
    val selectedDoctor = _selectedDoctor.asStateFlow()

    fun selectDoctor(doctor: DoctorEntity?) {
        _selectedDoctor.value = doctor
    }

    // --- UI/Operation Feedback States ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    init {
        // Collect appointments and prescriptions dynamic changes
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    repository.prepopulateDatabaseIfEmpty()
                    refreshUserData(user.id, user.role)
                } else {
                    _appointmentsState.value = emptyList()
                    _prescriptionsState.value = emptyList()
                }
            }
        }
    }

    fun refreshUserData(userId: String, role: String) {
        viewModelScope.launch {
            repository.getAppointments(userId, role).collect {
                _appointmentsState.value = it
            }
        }
        viewModelScope.launch {
            repository.getPrescriptions(userId, role).collect {
                _prescriptionsState.value = it
            }
        }
    }

    // --- Dynamic Search Filters ---
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSpecialization(spec: String) {
        _selectedSpecialization.value = spec
    }

    // --- Operations ---
    fun loginUser(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            repository.login(email, password)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    _authError.value = it.message ?: "Authentication failed"
                }
        }
    }

    fun registerUser(name: String, email: String, phone: String, password: String, role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            repository.register(name, email, phone, password, role)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    _authError.value = it.message ?: "Registration failed"
                }
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onSuccess()
        }
    }

    fun requestForgotPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.forgotPassword(email)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                }
        }
    }

    fun verifyForgotPasswordOtp(email: String, otp: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.verifyOtp(email, otp)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                    onFailure(it.message ?: "Incorret OTP")
                }
        }
    }

    fun resetUserPassword(email: String, otp: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.resetPassword(email, otp, pass)
                .onSuccess {
                    _isLoading.value = false
                    onSuccess()
                }
                .onFailure {
                    _isLoading.value = false
                }
        }
    }

    fun editProfile(
        name: String,
        phone: String,
        bloodGroup: String = "",
        allergies: String = "",
        medicalHistory: String = "",
        specialization: String = "",
        qualification: String = "",
        experience: String = "",
        consultationFee: Double = 0.0,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val req = UpdateProfileRequest(
                name = name,
                phone = phone,
                bloodGroup = bloodGroup,
                allergies = allergies,
                medicalHistory = medicalHistory,
                specialization = specialization,
                qualification = qualification,
                experience = experience,
                consultationFee = consultationFee
            )
            repository.updateProfile(req)
                .onSuccess {
                    onSuccess()
                }
        }
    }

    // --- Appointment Management ---
    fun lockAppointment(
        doctor: DoctorEntity,
        date: String,
        time: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.bookAppointment(
                doctorId = doctor.id,
                doctorName = doctor.name,
                doctorSpec = doctor.specialization,
                doctorImage = doctor.profileImage,
                date = date,
                time = time,
                notes = notes,
                fee = doctor.consultationFee
            ).onSuccess {
                _isLoading.value = false
                onSuccess()
                // Refresh list
                currentUser.value?.let { refreshUserData(it.id, it.role) }
            }.onFailure {
                _isLoading.value = false
            }
        }
    }

    fun cancelAppointment(apptId: String) {
        viewModelScope.launch {
            repository.deleteLocalAppointment(apptId)
            currentUser.value?.let { refreshUserData(it.id, it.role) }
        }
    }

    fun rescheduleAppointment(apptId: String, newDate: String, newTime: String) {
        viewModelScope.launch {
            // Cancel current & schedule with new details (or update dates)
            repository.updateAppointmentStatusLocal(apptId, "cancelled")
            
            // Re-fetch current data & model
            val user = currentUser.value
            val appointmentsList = appointments.value
            val foundMatch = appointmentsList.firstOrNull { it.id == apptId }
            if (foundMatch != null && user != null) {
                repository.bookAppointment(
                    doctorId = foundMatch.doctorId,
                    doctorName = foundMatch.doctorName,
                    doctorSpec = foundMatch.doctorSpecialization,
                    doctorImage = foundMatch.doctorImage,
                    date = newDate,
                    time = newTime,
                    notes = "Rescheduled: " + foundMatch.notes,
                    fee = foundMatch.fee
                )
                refreshUserData(user.id, user.role)
            }
        }
    }

    // --- Doctor Portal Actions ---
    fun acceptAppointment(apptId: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatusLocal(apptId, "upcoming")
            currentUser.value?.let { refreshUserData(it.id, it.role) }
        }
    }

    fun completeAppointment(apptId: String, notes: String, prescriptionFile: String = "") {
        viewModelScope.launch {
            repository.updateAppointmentStatusLocal(apptId, "completed")
            // Create corresponding prescription
            val matching = _appointmentsState.value.firstOrNull { it.id == apptId }
            val user = currentUser.value
            if (matching != null && user != null) {
                // Ingest dummy prescription file
                val rxFile = prescriptionFile.ifEmpty { "rx_${apptId}.pdf" }
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                db.prescriptionDao().insertPrescription(
                    PrescriptionEntity(
                        id = "rx_${UUID.randomUUID()}",
                        appointmentId = apptId,
                        doctorId = user.id,
                        doctorName = user.name,
                        patientId = matching.patientId,
                        patientName = matching.patientName,
                        prescriptionFile = rxFile,
                        notes = notes,
                        date = date
                    )
                )
                refreshUserData(user.id, user.role)
            }
        }
    }

    // --- Admin Operations ---
    fun adminAddDoctor(
        name: String,
        specialization: String,
        qualification: String,
        experience: String,
        hospitalName: String,
        consultationFee: Double,
        availableDays: String = "Monday, Wednesday, Friday",
        availableSlots: String = "09:00 AM, 11:00 AM, 02:00 PM",
        profileImage: String = ""
    ) {
        viewModelScope.launch {
            val docId = "doc_${UUID.randomUUID()}"
            val pImage = profileImage.ifEmpty { "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?auto=format&fit=crop&q=80&w=300" }
            val dEntity = DoctorEntity(
                id = docId,
                userId = "user_$docId",
                name = name,
                specialization = specialization,
                qualification = qualification,
                experience = experience,
                hospitalName = hospitalName,
                consultationFee = consultationFee,
                rating = 4.8,
                profileImage = pImage,
                availableDays = availableDays,
                availableSlots = availableSlots
            )
            db.doctorDao().insertDoctors(listOf(dEntity))
        }
    }

    fun adminDeleteDoctor(doctorId: String) {
        viewModelScope.launch {
            // Custom delete from SQLite
            db.openHelper.writableDatabase.execSQL("DELETE FROM doctors WHERE id = '$doctorId'")
            // Reload list
            val docs = db.doctorDao().getAllDoctors().firstOrNull() ?: emptyList()
            db.doctorDao().clearDoctors()
            db.doctorDao().insertDoctors(docs.filter { it.id != doctorId })
        }
    }

    fun adminCancelAppointment(apptId: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatusLocal(apptId, "cancelled")
            currentUser.value?.let { refreshUserData(it.id, it.role) }
        }
    }

    fun adminReassignDoctor(apptId: String, newDoctorId: String) {
        viewModelScope.launch {
            val dMatch = db.doctorDao().getDoctorById(newDoctorId)
            val appts = _appointmentsState.value
            val matchAppt = appts.firstOrNull { it.id == apptId }
            if (dMatch != null && matchAppt != null) {
                val updatedAppt = matchAppt.copy(
                    doctorId = dMatch.id,
                    doctorName = dMatch.name,
                    doctorSpecialization = dMatch.specialization,
                    doctorImage = dMatch.profileImage,
                    fee = dMatch.consultationFee
                )
                db.appointmentDao().insertAppointment(updatedAppt)
                currentUser.value?.let { refreshUserData(it.id, it.role) }
            }
        }
    }
}

class AppointmentViewModelFactory(
    private val application: Application,
    private val repository: Repository,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            return AppointmentViewModel(application, repository, db) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
