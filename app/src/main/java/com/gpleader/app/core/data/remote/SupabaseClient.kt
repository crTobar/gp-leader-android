package com.gpleader.app.core.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

fun createGpSupabaseClient(
    supabaseUrl: String,
    supabaseAnonKey: String,
) = createSupabaseClient(
    supabaseUrl = supabaseUrl,
    supabaseKey = supabaseAnonKey,
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
}
