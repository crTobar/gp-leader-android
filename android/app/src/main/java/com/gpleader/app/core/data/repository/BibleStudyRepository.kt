package com.gpleader.app.core.data.repository

data class EstudioBiblico(
    val id:               String,
    val memberId:         String,
    val studentName:      String,
    val completedLessons: List<Int>,
) {
    val totalCompleted: Int get() = completedLessons.size
    val currentLesson:  Int get() = ((completedLessons.maxOrNull() ?: 0) + 1).coerceAtMost(20)
}

interface BibleStudyRepository {
    suspend fun getEstudios(miembroId: String): Result<List<EstudioBiblico>>
    suspend fun getEstudioById(estudioId: String): Result<EstudioBiblico?>
    suspend fun createEstudio(miembroId: String, studentName: String): Result<EstudioBiblico>
    suspend fun toggleLesson(estudioId: String, lessonNumber: Int, completed: Boolean): Result<Unit>
}
