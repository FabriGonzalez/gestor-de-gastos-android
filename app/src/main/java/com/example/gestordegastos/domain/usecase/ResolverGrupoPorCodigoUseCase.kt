package com.example.gestordegastos.domain.usecase

import Grupo
import com.example.gestordegastos.data.repository.GrupoRepositoryFirestore

class ResolverGrupoPorCodigoUseCase(
    private val grupoRepository: GrupoRepositoryFirestore
) {

    suspend operator fun invoke(codigo: String): Grupo {
        return grupoRepository.obtenerGrupoPorCodigo(codigo)
            ?: throw Exception("No se encontró el grupo con código $codigo")
    }
}
