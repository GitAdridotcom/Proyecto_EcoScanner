package com.example.ecoscanner

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import java.lang.ref.WeakReference

object SupabaseManager {
    private var _client: SupabaseClient? = null
    private var activityRef: WeakReference<android.app.Activity>? = null

    val client: SupabaseClient
        get() = _client ?: throw IllegalStateException("Client not initialized")

    fun setClient(client: SupabaseClient) {
        _client = client
    }

    fun setActivity(activity: android.app.Activity) {
        activityRef = WeakReference(activity)
    }

    fun logout(context: Context) {
        val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        _client = null

        activityRef?.get()?.let { activity ->
            android.content.Intent(activity, MainActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }.also { intent ->
                activity.startActivity(intent)
                activity.finish()
            }
        }
    }
}