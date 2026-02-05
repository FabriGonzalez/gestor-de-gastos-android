package com.example.gestordegastos.data.repository

import com.example.gestordegastos.domain.model.Persona
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue

class PersonaRepositoryFirestore {

    private val db = Firebase.firestore

    fun obtenerPersonasDelGrupo(grupoId: String): Flow<List<Persona>> = callbackFlow {
        val ref = db.collection("grupos")
            .document(grupoId)
            .collection("personas")

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val personas = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Persona::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(personas)
        }

        awaitClose { listener.remove() }
    }

    suspend fun insertarPersona(persona: Persona) {
        require(persona.grupoId.isNotBlank()) {
            "Persona debe tener grupoId"
        }

        val personaRef = db.collection("grupos")
            .document(persona.grupoId)
            .collection("personas")
            .document()

        persona.id = personaRef.id
        personaRef.set(persona).await()

        db.collection("grupos")
            .document(persona.grupoId)
            .update("miembrosIds", FieldValue.arrayUnion(persona.id))
            .await()
    }

    suspend fun obtenerPersonaPorId(
        grupoId: String,
        personaId: String
    ): Persona? {
        val doc = db.collection("grupos")
            .document(grupoId)
            .collection("personas")
            .document(personaId)
            .get()
            .await()

        return if (doc.exists()) {
            doc.toObject(Persona::class.java)?.copy(id = doc.id)
        } else null
    }

    suspend fun actualizarPersona(persona: Persona) {
        require(persona.id.isNotBlank())
        require(persona.grupoId.isNotBlank())

        db.collection("grupos")
            .document(persona.grupoId)
            .collection("personas")
            .document(persona.id)
            .update(
                mapOf(
                    "nombre" to persona.nombre
                )
            )
            .await()
    }

    suspend fun eliminarPersona(
        grupoId: String,
        personaId: String
    ) {
        db.collection("grupos")
            .document(grupoId)
            .collection("personas")
            .document(personaId)
            .delete()
            .await()

        db.collection("grupos")
            .document(grupoId)
            .update(
                "miembrosIds",
                FieldValue.arrayRemove(personaId)
            )
            .await()
    }
}
