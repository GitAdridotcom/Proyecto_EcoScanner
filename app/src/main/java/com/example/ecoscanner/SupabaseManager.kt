package com.example.ecoscanner

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseManager {
    private var _client: SupabaseClient? = null
    val client: SupabaseClient
        get() = _client ?: createDefaultClient()

    fun setClient(client: SupabaseClient) {
        _client = client
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
}