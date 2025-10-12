package com.thatcakeid.zrytezene.updater

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.QuerySnapshot

class UpdateHelper(
    private val context: Context,
    private val versionDb: CollectionReference
) {

    fun check(
        onUpdateAvailable: () -> Unit,
        onUpToDate: () -> Unit,
        onError: (Exception) -> Unit) {
        versionDb.get()
            .addOnSuccessListener { query: QuerySnapshot ->
                val doc = query.documents.firstOrNull() ?: return@addOnSuccessListener

                try {
                    val info = context.packageManager.getPackageInfo(context.packageName, 0)
                    val localCode = PackageInfoCompat.getLongVersionCode(info)
                    val remoteCode = doc.getLong("version") ?: return@addOnSuccessListener
                    if (remoteCode > localCode) onUpdateAvailable() else onUpToDate()
                } catch (e: PackageManager.NameNotFoundException) {
                    onError(e)
                }
        }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}