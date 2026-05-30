package com.example.network

import retrofit2.Response
import retrofit2.http.*

// Request & Response Data Transfer Objects (DTOs)

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String // "patient", "doctor", "admin"
)

data class AuthUser(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val profileImage: String,
    val createdAt: String
)

data class LoginResponse(
    val token: String,
    val user: AuthUser
)

data class RegisterResponse(
    val message: String,
    val token: String,
    val user: AuthUser
)

data class ForgotPasswordRequest(val email: String)
data class OtpVerifyRequest(val email: String, val otp: String)
data class ResetPasswordRequest(val email: String, val otp: String, val newPasswordProperty: String)

data class StandardResponse(val message: String, val success: Boolean = true)

data class ScheduleRequest(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String
)

data class DoctorDto(
    val id: String,
    val name: String,
    val specialization: String,
    val qualification: String,
    val experience: String,
    val hospitalName: String,
    val consultationFee: Double,
    val rating: Double,
    val profileImage: String,
    val availableDays: String,
    val availableSlots: String
)

data class AppointmentRequest(
    val doctorId: String,
    val appointmentDate: String,
    val appointmentTime: String,
    val notes: String = ""
)

data class AppointmentDto(
    val id: String,
    val patientId: String,
    val patientName: String,
    val doctorId: String,
    val doctorName: String,
    val doctorSpecialization: String,
    val doctorImage: String,
    val appointmentDate: String,
    val appointmentTime: String,
    val status: String,
    val notes: String,
    val fee: Double,
    val createdAt: String
)

data class ProfileResponse(
    val user: AuthUser,
    val bloodGroup: String = "",
    val allergies: String = "",
    val medicalHistory: String = "",
    val specialization: String = "",
    val qualification: String = "",
    val experience: String = "",
    val consultationFee: Double = 0.0
)

data class UpdateProfileRequest(
    val name: String,
    val phone: String,
    val profileImage: String = "",
    val bloodGroup: String = "",
    val allergies: String = "",
    val medicalHistory: String = "",
    val specialization: String = "",
    val qualification: String = "",
    val experience: String = "",
    val consultationFee: Double = 0.0
)

interface ApiService {
    // --- Authentication APIs ---
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<StandardResponse>

    @POST("verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): Response<StandardResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<StandardResponse>


    // --- Doctor APIs ---
    @GET("doctors")
    suspend fun getDoctors(
        @Header("Authorization") token: String,
        @Query("specialization") specialization: String? = null,
        @Query("hospital") hospital: String? = null
    ): Response<List<DoctorDto>>

    @GET("doctors/{id}")
    suspend fun getDoctorById(
        @Header("Authorization") token: String,
        @Path("id") doctorId: String
    ): Response<DoctorDto>

    @POST("doctor/schedule")
    suspend fun setDoctorSchedule(
        @Header("Authorization") token: String,
        @Body request: ScheduleRequest
    ): Response<StandardResponse>

    @PUT("doctor/schedule")
    suspend fun updateDoctorSchedule(
        @Header("Authorization") token: String,
        @Body request: ScheduleRequest
    ): Response<StandardResponse>


    // --- Appointment APIs ---
    @POST("appointments")
    suspend fun createAppointment(
        @Header("Authorization") token: String,
        @Body request: AppointmentRequest
    ): Response<AppointmentDto>

    @GET("appointments")
    suspend fun getAppointments(
        @Header("Authorization") token: String
    ): Response<List<AppointmentDto>>

    @PUT("appointments/{id}")
    suspend fun updateAppointmentStatus(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: String,
        @Body body: Map<String, String> // e.g. {"status": "completed"}
    ): Response<AppointmentDto>

    @DELETE("appointments/{id}")
    suspend fun cancelAppointment(
        @Header("Authorization") token: String,
        @Path("id") appointmentId: String
    ): Response<StandardResponse>


    // --- Patient APIs ---
    @GET("profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ProfileResponse>

    @PUT("profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): Response<ProfileResponse>

    @Multipart
    @POST("upload-report")
    suspend fun uploadReport(
        @Header("Authorization") token: String,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<StandardResponse>
}
