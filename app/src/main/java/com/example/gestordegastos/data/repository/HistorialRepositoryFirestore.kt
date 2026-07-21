package com.example.gestordegastos.data.repository

import com.example.gestordegastos.domain.model.Historial
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class HistorialRepositoryFirestore {

    private val db = Firebase.firestore

    suspend fun guardarHistorial(historial: Historial) {
        val ref = db.collection("grupos")
            .document(historial.grupoId)
            .collection("historial")
            .document()

        historial.firestoreId = ref.id

        ref.set(historial).await()
    }

    fun obtenerHistorialDelGrupo(grupoId: String): Flow<List<Historial>> = callbackFlow {

        val ref = db.collection("grupos")
            .document(grupoId)
            .collection("historial")

        val listener = ref
            .orderBy("fechaLiquidacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val historial = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Historial::class.java)?.copy(
                        firestoreId = doc.id
                    )
                } ?: emptyList()

                trySend(historial)
            }

        awaitClose {
            listener.remove()
        }
    }
}