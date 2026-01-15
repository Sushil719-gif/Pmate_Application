package com.example.pmate.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument

import com.example.pmate.ui.Admin.jobs.AddJobScreen
import com.example.pmate.ui.Admin.jobs.AdminJobDetailsScreen
import com.example.pmate.ui.Admin.jobs.AdminJobs
import com.example.pmate.ui.Admin.AdminMainScreen
import com.example.pmate.ui.Admin.dashboard.company.AllNoticesAdminScreen
import com.example.pmate.ui.Admin.dashboard.company.CompanyActiveDetailsScreen

import com.example.pmate.ui.Admin.dashboard.company.CompanyDetailsScreen
import com.example.pmate.ui.Admin.dashboard.company.CompanyListScreen
import com.example.pmate.ui.Admin.dashboard.company.CompanyHoldListScreen
import com.example.pmate.ui.Admin.dashboard.company.SendNoticeScreen


// CORRECT IMPORTS –


import com.example.pmate.ui.Admin.jobs.ApplicantsListScreen
import com.example.pmate.ui.Admin.jobs.EditJobScreen
import com.example.pmate.ui.Admin.settings.AboutAppScreen
import com.example.pmate.ui.Admin.settings.ChangePasswordScreen
import com.example.pmate.ui.Admin.settings.LogoutConfirmScreen
import com.example.pmate.ui.Admin.settings.UpdateProfileScreen
import com.example.pmate.ui.Student.StudentHomeScreen
import com.example.pmate.ui.Student.JobDetailsScreen
import com.example.pmate.ui.Role.RoleSelectionScreen
import com.example.pmate.ui.Student.StudentMainScreen
import com.example.pmate.ui.Student.studentdashboard.StudentNoticeBoardScreen


@Composable
fun AppNavigation(navController: NavHostController) {

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route
    ) {

        // ROLE SELECTION
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onAdminClick = { navController.navigate(Screen.AdminMain.route) },
                onStudentClick = { navController.navigate(Screen.StudentMain.route) }
            )
        }

        // ADMIN MODULE......

        composable(Screen.AdminMain.route) {
            AdminMainScreen(navController)
        }

        // JOB SCREENS
        composable("addJob") { AddJobScreen(navController) }
        composable("adminJobs") { AdminJobs(navController) }

        composable(
            route = "job_details_admin/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) {
            val jobId = it.arguments?.getString("jobId")!!
            AdminJobDetailsScreen(navController, jobId)
        }

        composable("editJob/{jobId}") {
            val id = it.arguments?.getString("jobId")!!
            EditJobScreen(navController, id)
        }

        composable("applicants/{jobId}") {
            val id = it.arguments?.getString("jobId")!!
            ApplicantsListScreen(navController, id)
        }



        composable(
            route = "job_details/{jobId}",
            arguments = listOf(navArgument("jobId") { type = NavType.StringType })
        ) {
            val jobId = it.arguments?.getString("jobId")!!
            JobDetailsScreen(jobId = jobId, isAdmin = false)
        }

        composable("update_profile") { UpdateProfileScreen(navController) }
        composable("change_password") { ChangePasswordScreen(navController) }
        composable("about_app") { AboutAppScreen(navController) }
        composable("logout_confirm") { LogoutConfirmScreen(navController) }

        // ------------------ COMPANY DASHBOARD ------------------

        composable("CompanyList") {
            CompanyListScreen(navController)
        }

        // COMPLETED COMPANIES → CompanyDetailsScreen
        composable("CompanyDetails/{companyName}") { entry ->
            val companyName = entry.arguments?.getString("companyName") ?: ""
            CompanyDetailsScreen(navController, companyName)
        }

        // ACTIVE COMPANIES → CompanyActiveDetailsScreen
        composable("CompanyActiveDetails/{companyName}") { entry ->
            val companyName = entry.arguments?.getString("companyName") ?: ""
            CompanyActiveDetailsScreen(navController, companyName)
        }
        composable("CompanyHoldList") {
            CompanyHoldListScreen(navController)
        }

        // ------------------- Notice Board Section -------------------

        composable("SendNotice") {
            SendNoticeScreen(navController)
        }
        composable("AllNoticesAdmin") {
            AllNoticesAdminScreen(navController)
        }

//STUDENT MODULE.....

        // STUDENT HOME
        composable(Screen.StudentMain.route) {
            StudentMainScreen(navController)
        }
        composable("StudentNotices") {
            StudentNoticeBoardScreen(navController)
        }

    }
}
