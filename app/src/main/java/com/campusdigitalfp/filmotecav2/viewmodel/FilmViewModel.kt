package com.campusdigitalfp.filmotecav2.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusdigitalfp.filmotecav2.model.Film
import com.campusdigitalfp.filmotecav2.repository.FilmRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FilmViewModel : ViewModel() {

    private val repository = FilmRepository()

    private val _films = MutableStateFlow<List<Film>>(emptyList())
    val films: StateFlow<List<Film>> get() = _films

    private val auth = FirebaseAuth.getInstance()

    init {
        listenToFilms()
        observeUserChanges()
    }

    private fun observeUserChanges() {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                listenToFilms()
                loadFilms()
            } else {
                _films.value = emptyList()
            }
        }
    }

    private fun loadFilms() {
        viewModelScope.launch {
            _films.value = repository.getFilms()
        }
    }

    private fun listenToFilms() {
        repository.listenToFilmsUpdates { updatedFilms ->
            _films.value = updatedFilms
        }
    }

    private fun fetchFilms() {
        viewModelScope.launch {
            _films.value = repository.getFilms()
        }
    }

    fun addFilm(film: Film) {
        viewModelScope.launch {
            repository.addFilm(film)
        }
    }

    fun updateFilm(film: Film) {
        viewModelScope.launch {
            repository.updateFilm(film)
        }
    }

    fun deleteFilm(filmId: String) {
        viewModelScope.launch {
            repository.deleteFilm(filmId)
        }
    }

    fun addExampleFilms() {
        viewModelScope.launch {
            repository.addExampleFilms()
        }
    }

    fun logout(context: android.content.Context) {
        auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(context, gso).signOut()
    }
}