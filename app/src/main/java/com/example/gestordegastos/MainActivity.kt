package com.example.gestordegastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestordegastos.data.datastore.GroupPreferences
import com.example.gestordegastos.data.repository.GastoRepositoryFirestore
import com.example.gestordegastos.data.repository.PersonaRepositoryFirestore
import com.example.gestordegastos.ui.screens.MainScreen
import com.example.gestordegastos.ui.screens.StartScreen
import com.example.gestordegastos.ui.theme.GestorDeGastosTheme
import com.example.gestordegastos.viewmodel.GastoViewModel
import com.example.gestordegastos.viewmodel.StartViewModel
import GastoViewModelFactory
import com.example.gestordegastos.data.repository.GrupoRepositoryFirestore
import com.example.gestordegastos.viewmodel.StartViewModelFactory
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner


class MainActivity : ComponentActivity() {

    private var splashVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {

        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { splashVisible }

        super.onCreate(savedInstanceState)

        val groupPreferences = GroupPreferences(this)
        val grupoRepository = GrupoRepositoryFirestore()

        setContent {
            GestorDeGastosTheme {

                val startViewModel: StartViewModel = viewModel(
                    factory = StartViewModelFactory(
                        groupPreferences,
                        grupoRepository
                    )
                )

                val grupo by startViewModel.grupo.collectAsState()
                val cargando by startViewModel.cargando.collectAsState()
                val error by startViewModel.error.collectAsState()
                val inicializado by startViewModel.inicializado.collectAsState()

                LaunchedEffect(inicializado) {
                    if (inicializado) splashVisible = false
                }

                when {
                    !inicializado -> Unit

                    grupo == null -> {
                        StartScreen(
                            cargando = cargando,
                            error = error,
                            onCrearGrupo = { startViewModel.crearGrupo() },
                            onUnirseAGrupo = { id -> startViewModel.unirseAGrupo(id) }
                        )
                    }

                    else -> {
                        val gastoRepository = remember { GastoRepositoryFirestore() }
                        val personaRepository = remember { PersonaRepositoryFirestore() }

                        val grupoId = grupo!!.codigoGrupo

                        val owner = remember(grupoId) {
                            object : ViewModelStoreOwner {
                                override val viewModelStore = ViewModelStore()
                            }
                        }

                        val gastoViewModel: GastoViewModel = viewModel(
                            viewModelStoreOwner = owner,
                            factory = GastoViewModelFactory(
                                grupo = grupo!!,
                                gastoRepository = gastoRepository,
                                personaRepository = personaRepository
                            )
                        )

                        MainScreen(
                            viewModel = gastoViewModel,
                            onSalirDelGrupo = {
                                startViewModel.salirDelGrupo()
                            }
                        )
                    }

                }
            }
        }
    }
}