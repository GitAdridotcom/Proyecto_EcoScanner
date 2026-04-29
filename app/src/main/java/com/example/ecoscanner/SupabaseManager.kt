package com.example.ecoscanner

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference

object SupabaseManager {
    private var _client: SupabaseClient? = null
    private var activityRef: WeakReference<android.app.Activity>? = null
    
    val client: SupabaseClient
        get() = _client ?: createDefaultClient()

    fun setClient(client: SupabaseClient) {
        _client = client
    }
    
    fun setActivity(activity: android.app.Activity) {
        activityRef = WeakReference(activity)
    }

    private fun createDefaultClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://xhwuqwfqbyplcohsbomq.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inhod3Vxd2ZxYnlwbGNvaHNib21xIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzc0NjgyODgsImV4cCI6MjA5MzA0NDI4OH0.kqHHy1RyqHZHIqg7el9t61E-lvQdnlsI81HSmIPfpXk"
        ) {
            install(Auth)
            install(Postgrest)
        }
    }
    
    fun logout() {
        try {
            runBlocking {
                client.auth.signOut()
            }
        } catch (e: Exception) {
        }
        _client = null
        
        // Navigate to login
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