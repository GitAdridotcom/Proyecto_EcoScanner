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
            supabaseUrl = "https://buodriyoosvuxwclzcyh.supabase.co",
            supabaseKey = "sb_publishable_26_DWNG8dxnkBbr8bf1aFg_zjgz1Mav"
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
            // Ignore logout errors
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