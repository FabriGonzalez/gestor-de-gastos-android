import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gestordegastos.data.repository.NotaRepositoryFirestore
import com.example.gestordegastos.viewmodel.NotaViewModel

class NotaViewModelFactory(
    private val grupo: Grupo,
    private val notaRepository: NotaRepositoryFirestore
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotaViewModel(
            grupo,
            notaRepository
        ) as T
    }
}