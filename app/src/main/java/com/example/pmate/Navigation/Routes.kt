package com.example.pmate.Navigation

sealed class Screen(val route: String) {
    object RoleSelection : Screen("role_selection")
    object AdminMain : Screen("admin_main")
    object StudentMain : Screen("student_main")
    object Login : Screen("login")
    object JobDetails : Screen("job_details/{jobId}")


}