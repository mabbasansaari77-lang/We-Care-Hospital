package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppointmentViewModel
import com.example.viewmodel.AppointmentViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val db = remember { AppDatabase.getDatabase(context) }
                val repository = remember { Repository(db) }
                val appViewModel: AppointmentViewModel = viewModel(
                    factory = AppointmentViewModelFactory(application, repository, db)
                )

                val navController = rememberNavController()
                val currentUser by appViewModel.currentUser.collectAsState()
                val selectedDoctor by appViewModel.selectedDoctor.collectAsState()

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Determine whether to display the bottom navigation bar
                val showBottomBar = currentUser != null && currentRoute in listOf(
                    "home",
                    "my_appointments",
                    "settings"
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = Color.White,
                                modifier = Modifier.testTag("app_bottom_nav_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, "Home Dashboard tab") },
                                    label = { Text("Dashboard") },
                                    modifier = Modifier.testTag("nav_tab_home")
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "my_appointments",
                                    onClick = {
                                        navController.navigate("my_appointments") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Assignment, "Appointments tab") },
                                    label = { Text("Consults") },
                                    modifier = Modifier.testTag("nav_tab_appointments")
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "settings",
                                    onClick = {
                                        navController.navigate("settings") {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Settings, "Settings preferences tab") },
                                    label = { Text("Preferences") },
                                    modifier = Modifier.testTag("nav_tab_settings")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // ---欢迎与认证---
                        composable("splash") {
                            SplashScreen(appViewModel) {
                                navController.navigate("onboarding") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        }

                        composable("onboarding") {
                            OnboardingScreen {
                                navController.navigate("login") {
                                    popUpTo("onboarding") { inclusive = true }
                                }
                            }
                        }

                        composable("login") {
                            LoginScreen(
                                viewModel = appViewModel,
                                onNavigateToSignup = { navController.navigate("signup") },
                                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("signup") {
                            RegistrationScreen(
                                viewModel = appViewModel,
                                onNavigateToLogin = { navController.navigate("login") },
                                onSignupSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                viewModel = appViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // ---主要应用界面---
                        composable("home") {
                            HomeScreen(
                                viewModel = appViewModel,
                                onNavigateToDoctorList = { navController.navigate("doctor_list") },
                                onNavigateToDoctorDetails = { doc ->
                                    appViewModel.selectDoctor(doc)
                                    navController.navigate("doctor_details")
                                },
                                onNavigateToNotifications = { navController.navigate("notifications") },
                                onNavigateToAppointments = { navController.navigate("my_appointments") }
                            )
                        }

                        composable("doctor_list") {
                            DoctorListScreen(
                                viewModel = appViewModel,
                                onNavigateToDoctorDetails = { doc ->
                                    appViewModel.selectDoctor(doc)
                                    navController.navigate("doctor_details")
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("doctor_details") {
                            selectedDoctor?.let { doc ->
                                DoctorDetailsScreen(
                                    doctor = doc,
                                    onNavigateToBooking = { navController.navigate("booking") },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            } ?: run {
                                navController.popBackStack()
                            }
                        }

                        composable("booking") {
                            selectedDoctor?.let { doc ->
                                AppointmentBookingScreen(
                                    viewModel = appViewModel,
                                    doctor = doc,
                                    onBookingSaved = {
                                        navController.navigate("my_appointments") {
                                            popUpTo("home")
                                        }
                                    },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            } ?: run {
                                navController.popBackStack()
                            }
                        }

                        composable("my_appointments") {
                            MyAppointmentsScreen(
                                viewModel = appViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("notifications") {
                            NotificationsScreen(
                                viewModel = appViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                viewModel = appViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                viewModel = appViewModel,
                                onNavigateToProfile = { navController.navigate("profile") },
                                onLoggedOut = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
