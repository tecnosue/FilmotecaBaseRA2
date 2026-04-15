package com.campusdigitalfp.filmotecav2.repository

import android.util.Log
import com.campusdigitalfp.filmotecav2.model.Film
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FilmRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getFilmsCollection() =
        auth.currentUser?.let { user ->
            db.collection("usuarios").document(user.uid).collection("films")
        }

    suspend fun addFilm(film: Film): String? {
        val collection = getFilmsCollection() ?: return null
        val newDocRef = collection.document()
        return try {
            val filmWithId = film.copy(id = newDocRef.id)
            newDocRef.set(filmWithId).await()
            newDocRef.id
        } catch (e: Exception) {
            Log.e("FilmRepo", "Error al añadir película: ${e.message}")
            null
        }
    }

    suspend fun getFilms(): List<Film> {
        return try {
            getFilmsCollection()?.get()?.await()?.documents?.mapNotNull {
                it.toObject(Film::class.java)?.copy(id = it.id)
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e("FilmRepo", "Error al obtener películas: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateFilm(film: Film) {
        try {
            getFilmsCollection()?.document(film.id)?.set(film)?.await()
        } catch (e: Exception) {
            Log.e("FilmRepo", "Error al actualizar película: ${e.message}")
        }
    }

    suspend fun deleteFilm(filmId: String) {
        try {
            getFilmsCollection()?.document(filmId)?.delete()?.await()
        } catch (e: Exception) {
            Log.e("FilmRepo", "Error al eliminar película: ${e.message}")
        }
    }

    fun listenToFilmsUpdates(onUpdate: (List<Film>) -> Unit) {
        getFilmsCollection()?.addSnapshotListener { snapshot, exception ->
            if (exception != null) {
                Log.e("FilmRepo", "Error al escuchar películas: ${exception.message}")
                return@addSnapshotListener
            }
            val films = snapshot?.documents?.mapNotNull {
                it.toObject(Film::class.java)?.copy(id = it.id)
            } ?: emptyList()
            onUpdate(films)
        }
    }

    suspend fun addExampleFilms() {
        val collection = getFilmsCollection() ?: return
        val films = listOf(
            Film(title = "Harry Potter y la piedra filosofal", director = "Chris Columbus", imagen = "harry_potter_y_la_piedra_filosofal", comments = "Una aventura mágica en Hogwarts.", format = Film.FORMAT_DVD, genre = Film.GENRE_ACTION, imdbUrl = "http://www.imdb.com/title/tt0241527", year = 2001),
            Film(title = "Regreso al futuro", director = "Robert Zemeckis", imagen = "regreso_al_futuro", comments = "", format = Film.FORMAT_DIGITAL, genre = Film.GENRE_SCIFI, imdbUrl = "http://www.imdb.com/title/tt0088763", year = 1985),
            Film(title = "El rey león", director = "Roger Allers, Rob Minkoff", imagen = "el_rey_leon", comments = "Una historia de crecimiento y responsabilidad.", format = Film.FORMAT_BLURAY, genre = Film.GENRE_ACTION, imdbUrl = "http://www.imdb.com/title/tt0110357", year = 1994),
            Film(title = "Matrix", director = "Lana Wachowski, Lilly Wachowski", imagen = "matrix", comments = "Revolucionaria película de ciencia ficción.", format = Film.FORMAT_BLURAY, genre = Film.GENRE_SCIFI, imdbUrl = "http://www.imdb.com/title/tt0133093", year = 1999),
            Film(title = "Titanic", director = "James Cameron", imagen = "titanic", comments = "Un clásico romántico y dramático.", format = Film.FORMAT_DVD, genre = Film.GENRE_DRAMA, imdbUrl = "http://www.imdb.com/title/tt0120338", year = 1997),
            Film(title = "Inception", director = "Christopher Nolan", imagen = "inception", comments = "Un thriller psicológico con capas de realidad.", format = Film.FORMAT_BLURAY, genre = Film.GENRE_SCIFI, imdbUrl = "http://www.imdb.com/title/tt1375666", year = 2010)
        )

        val batch = db.batch()
        films.forEach { film ->
            val newDocRef = collection.document()
            batch.set(newDocRef, film.copy(id = newDocRef.id))
        }
        try {
            batch.commit().await()
            Log.i("FilmRepo", "Películas de ejemplo añadidas correctamente")
        } catch (e: Exception) {
            Log.e("FilmRepo", "Error al añadir películas: ${e.message}")
        }
    }
}