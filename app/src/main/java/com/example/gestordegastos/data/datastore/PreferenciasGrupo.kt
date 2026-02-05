package com.example.gestordegastos.data.datastore

import Grupo
import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.gestordegastos.data.cache.GrupoCache

private val Context.dataStore by preferencesDataStore(name = "group_prefs")

class GroupPreferences(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val FIRESTORE_ID = stringPreferencesKey("firestore_id")
        val CODIGO_GRUPO = stringPreferencesKey("codigo_grupo")
    }

    val grupoFlow: Flow<GrupoCache?> = dataStore.data.map { prefs ->
        val firestoreId = prefs[FIRESTORE_ID]
        val codigo = prefs[CODIGO_GRUPO]

        if (firestoreId != null && codigo != null) {
            GrupoCache(
                firestoreId = firestoreId,
                codigoGrupo = codigo
            )
        } else null
    }

    suspend fun guardarGrupo(grupo: Grupo) {
        dataStore.edit { prefs ->
            prefs[FIRESTORE_ID] = grupo.firestoreId
            prefs[CODIGO_GRUPO] = grupo.codigoGrupo
        }
    }

    suspend fun clearGrupo() {
        dataStore.edit { prefs ->
            prefs.remove(FIRESTORE_ID)
            prefs.remove(CODIGO_GRUPO)
        }
    }
}
