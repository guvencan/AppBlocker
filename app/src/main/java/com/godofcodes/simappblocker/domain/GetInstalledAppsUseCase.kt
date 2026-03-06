package com.godofcodes.simappblocker.domain

import com.godofcodes.simappblocker.AppItem
import com.godofcodes.simappblocker.data.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetInstalledAppsUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(): List<AppItem> = withContext(Dispatchers.IO) {
        repository.getInstalledApps()
    }
}
