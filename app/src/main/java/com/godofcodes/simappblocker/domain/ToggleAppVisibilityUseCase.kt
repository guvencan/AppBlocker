package com.godofcodes.simappblocker.domain

import com.godofcodes.simappblocker.AppItem
import com.godofcodes.simappblocker.data.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ToggleAppVisibilityUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(item: AppItem): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            val command = if (item.isHidden) {
                "pm enable ${item.packageName}"
            } else {
                "pm disable-user --user 0 ${item.packageName}"
            }
            val result = repository.executeCommand(command)
            if (result.contains("new state") || result.contains("enabled")) {
                !item.isHidden
            } else {
                error(result)
            }
        }
    }
}
