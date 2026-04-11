package com.example.gestordegastos.data.repository

import com.example.gestordegastos.domain.model.Nota
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotaRepositoryFirestore {
    private val db = Firebase.firestore

    suspend fun insertarNota(nota: Nota) {
        val ref = db.collection("grupos")
            .document(nota.grupoId)
            .collection("notas")
            .document()

        nota.firestoreId = ref.id
        ref.set(nota).await()
    }

    fun obtenerNotasDelGrupo(grupoId: String): Flow<List<Nota>> = callbackFlow {
        val ref = db.collection("grupos")
            .document(grupoId)
            .collection("notas")

        val listener = ref.orderBy("fechaCreacion",com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val notas = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Nota::class.java)?.copy(firestoreId = doc.id)
                } ?: emptyList()
                trySend(notas)
            }
        awaitClose { listener.remove() }
    }

    suspend fun eliminarNota(grupoId: String, notaId: String) {
        db.collection("grupos")
            .document(grupoId)
            .collection("notas")
            .document(notaId)
            .delete()
            .await()
    }

    suspend fun actualizarNota(nota: Nota) {
        require(nota.firestoreId.isNotBlank())
        require(nota.grupoId.isNotBlank())

        db.collection("grupos")
            .document(nota.grupoId)
            .collection("notas")
            .document(nota.firestoreId)
            .set(nota)
            .await()
    }
}

