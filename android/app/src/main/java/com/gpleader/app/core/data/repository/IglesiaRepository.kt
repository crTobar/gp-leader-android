package com.gpleader.app.core.data.repository

data class ChurchHit(
    val id:           String,
    val churchName:   String,
    val districtName: String,
    val campoName:    String,
)

interface IglesiaRepository {
    suspend fun searchChurches(query: String, maxResults: Int = 5): List<ChurchHit>
    suspend fun getChurchById(churchId: String): ChurchHit?
}
