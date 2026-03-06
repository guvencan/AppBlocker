package com.godofcodes.simappblocker.domain

import com.godofcodes.simappblocker.AppItem
import com.godofcodes.simappblocker.data.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UninstallAppUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(item: AppItem): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val result = repository.executeCommand("pm uninstall -k --user 0 ${item.packageName}")
            if (!result.contains("Success")) error(result)
        }
    }
}
