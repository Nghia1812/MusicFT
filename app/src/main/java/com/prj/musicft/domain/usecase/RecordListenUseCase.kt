package com.prj.musicft.domain.usecase

import com.prj.musicft.domain.repository.HistoryRepository
import javax.inject.Inject

class RecordListenUseCase @Inject constructor(
    private val historyRepository: HistoryRepository
) {
    suspend operator fun invoke(songId: Long) {
        historyRepository.recordListen(songId)
    }
}
