package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.model.HistoryEntry
import com.prj.musicft.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentHistoryUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    operator fun invoke(limit: Int): Flow<List<HistoryEntry>> {
        return historyRepository.getRecentHistory(limit)
    }
}
