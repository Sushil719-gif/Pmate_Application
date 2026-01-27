package com.example.pmate.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.pmate.Auth.LoginScreen

import com.example.pmate.ui.Admin.jobs.AddJobScreen
import com.example.pmate.ui.Admin.jobs.AdminJobDetailsScreen
import com.example.pmate.ui.Admin.jobs.AdminJobs
import com.example.pmate.ui.Admin.AdminMainScreen
import com.example.pmate.ui.Admin.dashboard.company.AllNoticesAdminScreen



import com.example.pmate.ui.Admin.dashboard.company.CompanyListScreen

import com.example.pmate.ui.Admin.dashboard.company.SendNoticeScreen


// CORRECT IMPORTS –


import com.example.pmate.ui.Admin.jobs.ApplicantsListScreen
import com.example.pmate.ui.Admin.jobs.EditJobScreen
import com.example.pmate.ui.Admin.settings.AboutAppScreen
import com.example.pmate.ui.Admin.settings.ChangePasswordScreen
import com.example.pmate.Auth.LogoutConfirmScreen
import com.example.pmate.ui.Admin.dashboard.company.ActiveCompaniesListScreen
import com.example.pmate.ui.Admin.dashboard.company.CompanyOnHoldListScreen
import com.example.pmate.ui.Admin.dashboard.company.CompletedCompaniesListScreen

import com.example.pmate.ui.Admin.dashboard.company.PlacedStudentsScreen
import com.example.pmate.ui.Admin.settings.UpdateProfileScreen


import com.example.pmate.ui.Student.studentjobs.JobDetailsScreen
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
                onAdminClick = { navController.navigate("login/admin") },
                onStudentClick = { navController.navigate("login/student") }


            )
        }

        //Login

        composable(
            route = "login/{expectedRole}",
            arguments = listOf(navArgument("expectedRole") { type = NavType.StringType })
        ) { backStack ->

            val expectedRole = backStack.arguments?.getString("expectedRole") ?: "student"
            LoginScreen(navController, expectedRole)
        }



        // ADMIN MODULE......

        composable(Screen.AdminMain.route) {
            AdminMainScreen(navController)
        }

        //placed students details

        composable("PlacedStudents/{batch}") {
            val batch = it.arguments?.getString("batch")!!
            PlacedStudentsScreen(navController, batch)
        }




        // JOB SCREENS
        composable("addJob") { AddJobScreen(navController) }
        composable("adminJobs") { "adminJobs/{batch}" }

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

        composable("CompanyList/{batch}") { backStackEntry ->
            val batch = backStackEntry.arguments?.getString("batch") ?: ""
            CompanyListScreen(navController, batch)
        }

        composable("ActiveCompanies/{batch}") { backStack ->
            val batch = backStack.arguments?.getString("batch")!!
            ActiveCompaniesListScreen(
                navController = navController,
                batch = batch
            )
        }

        composable("HoldCompanies/{batch}") { backStack ->
            val batch = backStack.arguments?.getString("batch")!!
            CompanyOnHoldListScreen(
                navController = navController,
                batch = batch
            )
        }

        composable("CompletedCompanies/{batch}") { backStack ->
            val batch = backStack.arguments?.getString("batch")!!
            CompletedCompaniesListScreen(
                navController = navController,
                batch = batch
            )
        }



        // ------------------- Notice Board Section -------------------

        composable("SendNotice/{batch}") {
            val batch = it.arguments?.getString("batch")!!
            SendNoticeScreen(navController, batch)
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
