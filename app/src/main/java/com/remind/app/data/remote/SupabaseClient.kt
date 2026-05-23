package com.remind.app.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = "https://baazvuehahrxntutrskm.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJhYXp2dWVoYWhyeG50dXRyc2ttIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk1MjQ2MzgsImV4cCI6MjA5NTEwMDYzOH0.vOHRA11LUn1dxs4N2po7qWXbFhUET2GTGNNhSjveH8k"
    ) {
        install(Auth) {
            scheme = "remindplus"
            host = "login"
        }
        install(Postgrest)
    }
}
