package com.example.gestordegastos.data.repository

import com.example.gestordegastos.domain.model.Gasto
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class GastoRepositoryFirestore {
    private val db = Firebase.firestore

    suspend fun insertarGasto(gasto: Gasto) {
        val ref = db.collection("grupos")
            .document(gasto.grupoId)
            .collection("gastos")
            .document()

        gasto.firestoreId = ref.id
        ref.set(gasto).await()
    }

    fun obtenerGastosDelGrupo(grupoId: String): Flow<List<Gasto>> = callbackFlow {
        val ref = db.collection("grupos")
            .document(grupoId)
            .collection("gastos")

        val listener = ref.addSnapshotListener { snapshot, _ ->
            val gastos = snapshot?.documents?.mapNotNull { doc ->
                doc.toGastoCompat()
            } ?: emptyList()
            trySend(gastos)
        }
        awaitClose { listener.remove() }
    }

    suspend fun eliminarGasto(grupoId: String, gastoId: String) {
        db.collection("grupos")
            .document(grupoId)
            .collection("gastos")
            .document(gastoId)
            .delete()
            .await()
    }

    suspend fun actualizarGasto(gasto: Gasto) {
        require(gasto.firestoreId.isNotBlank())
        require(gasto.grupoId.isNotBlank())

        db.collection("grupos")
            .document(gasto.grupoId)
            .collection("gastos")
            .document(gasto.firestoreId)
            .set(gasto)
            .await()
    }
}

private fun DocumentSnapshot.toGastoCompat(): Gasto? {
    val gasto = toObject(Gasto::class.java)?.copy(firestoreId = id) ?: return null
    if (gasto.montoCentavos != 0L) return gasto

    val montoLegacy = getDouble("monto")
    val montoCentavos = if (montoLegacy != null) {
        kotlin.math.round(montoLegacy * 100.0).toLong()
    } else {
        0L
    }

    return gasto.copy(montoCentavos = montoCentavos)
}
