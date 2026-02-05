// Kotlin
package com.example.gestordegastos.data.repository

import Grupo
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class GrupoRepositoryFirestore {
    private val db = Firebase.firestore
    private val gruposRef = db.collection("grupos")

    private val cacheCodigoAGrupo = mutableMapOf<String, Grupo>()

    suspend fun crearGrupo(codigoGrupo: String): Grupo {
        val grupoRef = gruposRef.document()
        val firestoreId = grupoRef.id


        val grupo = Grupo(
            firestoreId = firestoreId,
            codigoGrupo = codigoGrupo
        )

        grupoRef.set(grupo).await()
        return grupo
    }

    suspend fun buscarGrupoPorCodigo(codigo: String): Grupo? {
        val snap = gruposRef
            .whereEqualTo("codigoGrupo", codigo.uppercase())
            .get()
            .await()

        val doc = snap.documents.firstOrNull() ?: return null

        return doc.toObject(Grupo::class.java)?.copy(
            firestoreId = doc.id
        )
    }

    suspend fun obtenerGrupoPorCodigo(codigo: String): Grupo? {
        cacheCodigoAGrupo[codigo]?.let { return it }

        val snap = gruposRef
            .whereEqualTo("codigoGrupo", codigo)
            .limit(1)
            .get()
            .await()

        val doc = snap.documents.firstOrNull() ?: return null

        val grupo = doc.toObject(Grupo::class.java)?.copy(
            firestoreId = doc.id
        )
        if (grupo != null) {
            cacheCodigoAGrupo[codigo] = grupo
        }

        return grupo
    }

    fun limpiarCache() {
        cacheCodigoAGrupo.clear()
    }

    suspend fun unirseAGrupo(codigo: String): Grupo? {
        val grupo = buscarGrupoPorCodigo(codigo) ?: return null

        return grupo
    }

    suspend fun agregarMiembro(grupoFirestoreId: String, personaId: String) {
        gruposRef
            .document(grupoFirestoreId)
            .update("miembrosIds", FieldValue.arrayUnion(personaId))
            .await()
    }

    fun generarCodigoDeGrupo(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }
}
