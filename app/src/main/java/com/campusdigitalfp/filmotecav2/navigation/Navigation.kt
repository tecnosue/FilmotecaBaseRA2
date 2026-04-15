package com.campusdigitalfp.filmotecav2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.campusdigitalfp.filmotecav2.screens.AboutScreen
import com.campusdigitalfp.filmotecav2.screens.FilmDataScreen
import com.campusdigitalfp.filmotecav2.screens.FilmEditScreen
import com.campusdigitalfp.filmotecav2.screens.FilmListScreen
import com.campusdigitalfp.filmotecav2.screens.LoginScreen
import com.campusdigitalfp.filmotecav2.screens.RegisterScreen
import com.campusdigitalfp.filmotecav2.screens.RelatedFilmsScreen
import com.campusdigitalfp.filmotecav2.viewmodel.AuthViewModel
import com.campusdigitalfp.filmotecav2.viewmodel.FilmViewModel
import com.google.firebase.auth.FirebaseAuth

fun comprobarUsuario(): Boolean {
    return FirebaseAuth.getInstance().currentUser != null
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val filmViewModel: FilmViewModel = viewModel()
    //val authViewModel: AuthViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as android.app.Application
        )
    )
    val films by filmViewModel.films.collectAsState()

    val startDestination = if (comprobarUsuario()) "list" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            if (comprobarUsuario())
                FilmListScreen(navController, filmViewModel)
            else
                LoginScreen(navController, authViewModel)
        }
        composable("register") {
            RegisterScreen(navController, authViewModel)
        }
        composable("list") {
            FilmListScreen(navController, filmViewModel)
        }
        composable("data/{filmId}") { backStackEntry ->
            val filmId = backStackEntry.arguments?.getString("filmId") ?: ""
            val film = films.find { it.id == filmId }
            film?.let {
                FilmDataScreen(navController, film = it, viewModel = filmViewModel)
            }
        }
        composable("edit/{filmId}") { backStackEntry ->
            val filmId = backStackEntry.arguments?.getString("filmId") ?: ""
            val film = films.find { it.id == filmId }
            film?.let {
                FilmEditScreen(navController, film = it, viewModel = filmViewModel)
            }
        }
        composable("about") {
            AboutScreen(navController)
        }
        composable("related/{filmId}") { backStackEntry ->
            val filmId = backStackEntry.arguments?.getString("filmId") ?: ""
            val film = films.find { it.id == filmId }
            film?.let {
                RelatedFilmsScreen(navController, film = it, viewModel = filmViewModel)
            }
        }
    }
}