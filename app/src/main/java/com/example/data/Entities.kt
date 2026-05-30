package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String, // "patient", "doctor", "admin"
    val profileImage: String,
    val createdAt: String,
    val token: String,
    // Doctor or Patient additional attributes saved as flattened JSON or simple text fields
    val bloodGroup: String = "",
    val allergies: String = "",
    val medicalHistory: String = "",
    val specialization: String = "",
    val experience: String = "",
    val consultationFee: Double = 0.0,
    val qualification: String = ""
) : Serializable

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val specialization: String,
    val qualification: String,
    val experience: String,
    val hospitalName: String,
    val consultationFee: Double,
    val rating: Double,
    val profileImage: String,
    val availableDays: String, // comma-separated like "Monday,Wednesday,Friday"
    val availableSlots: String // comma-separated like "09:00 AM,10:00 AM,02:00 PM"
) : Serializable

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey val id: String,
    val patientId: String,
    val patientName: String,
    val doctorId: String,
    val doctorName: String,
    val doctorSpecialization: String,
    val doctorImage: String,
    val date: String,
    val time: String,
    val status: String, // "upcoming", "completed", "cancelled"
    val notes: String = "",
    val fee: Double = 0.0,
    val createdAt: String = ""
) : Serializable

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey val id: String,
    val appointmentId: String,
    val doctorId: String,
    val doctorName: String,
    val patientId: String,
    val patientName: String,
    val prescriptionFile: String,
    val notes: String,
    val date: String
) : Serializable

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val isRead: Boolean,
    val date: String
) : Serializable
