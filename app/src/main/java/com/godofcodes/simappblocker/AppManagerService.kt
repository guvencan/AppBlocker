package com.godofcodes.simappblocker

class AppManagerService : IAppManager.Stub() {

    override fun executeCommand(command: String): String {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        val output = process.inputStream.bufferedReader().readText()
        val error = process.errorStream.bufferedReader().readText()
        process.waitFor()
        return if (output.isNotBlank()) output.trim() else error.trim()
    }
}
