import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestordegastos.data.repository.GastoRepositoryFirestore
import com.example.gestordegastos.data.repository.PersonaRepositoryFirestore
import com.example.gestordegastos.viewmodel.GastoViewModel

class GastoViewModelFactory(
    private val grupo : Grupo,
    private val gastoRepository: GastoRepositoryFirestore,
    private val personaRepository: PersonaRepositoryFirestore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GastoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GastoViewModel(grupo, gastoRepository, personaRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
