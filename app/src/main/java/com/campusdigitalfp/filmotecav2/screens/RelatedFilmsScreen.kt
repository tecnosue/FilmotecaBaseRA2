package com.campusdigitalfp.filmotecav2.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.campusdigitalfp.filmotecav2.model.Film
//import com.campusdigitalfp.filmotecav2.model.FilmDataSource
import com.campusdigitalfp.filmotecav2.R
import com.campusdigitalfp.filmotecav2.common.FilmTopAppBar
import com.campusdigitalfp.filmotecav2.viewmodel.FilmViewModel
//import com.campusdigitalfp.filmotecav2.model.FilmDataSource.films

@Composable
fun RelatedFilmsScreen(navController: NavHostController, film: Film, viewModel: FilmViewModel = viewModel()) {
    var isActionMode by remember { mutableStateOf(false) }
    //val selectedFilms = remember { mutableStateListOf<Film>() }
    //val currentFilm = films[filmIndex]
    val films by viewModel.films.collectAsState()
    val relatedFilms = films.filter{it.id != film.id}

    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        FilmTopAppBar(navController,

            editar = false,
            //selectedFilms = selectedFilms,
            isActionMode = isActionMode,
            onActionModeChange = {isActionMode =it},
            viewModel = viewModel
        )
    }) { innerPadding ->
        RelatedFilmListContent(
            films = relatedFilms,
            //selectedFilms = selectedFilms,
            isActionMode = isActionMode,
            navController = navController,
            innerPadding = innerPadding
        ) { isActionMode = it }
    }
}

@Composable
fun RelatedFilmListContent(
    films: List<Film>,
    isActionMode: Boolean,
    navController: NavHostController,
    innerPadding: PaddingValues,
    onActionModeChange: (Boolean) -> Unit
) {


    LazyColumn(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        items(films) { film ->
            VistaFilm(film = film, onClick = {

                //  pasarle el index original
                //val originalIndex = FilmDataSource.films.indexOf(film)
                Log.i("Filmoteca", "vamos a la peli seleccionada...")
                navController.navigate("data/$film.id")}

            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VistaFilm(film: Film, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        val context = LocalContext.current

        Image(
            painter = painterResource(id = context.resources.getIdentifier(film.imagen, "drawable", context.packageName).let {
                if (it != 0) it else R.drawable.icono_pelicula
            }),
            contentDescription = "Icono",
            modifier = Modifier
                .padding( 0.dp)
                .size( 80.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = film.title.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = film.director.toString(), style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}