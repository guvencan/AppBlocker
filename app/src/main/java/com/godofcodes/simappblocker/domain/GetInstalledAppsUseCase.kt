package com.godofcodes.simappblocker.domain

import com.godofcodes.simappblocker.AppItem
import com.godofcodes.simappblocker.data.AppRepository

class GetInstalledAppsUseCase(private val repository: AppRepository) {
    suspend operator fun invoke(): List<AppItem> = repository.getInstalledApps()
}
