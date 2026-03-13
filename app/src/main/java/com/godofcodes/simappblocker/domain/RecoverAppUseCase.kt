package com.godofcodes.simappblocker.domain

import com.godofcodes.simappblocker.AppItem
import com.godofcodes.simappblocker.data.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecoverAppUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(item: AppItem): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val result = repository.executeCommand("pm install-existing --user 0 ${item.packageName}")
            if (!result.contains("installed for user") && !result.contains("Success")) error(result)
        }
    }
}
