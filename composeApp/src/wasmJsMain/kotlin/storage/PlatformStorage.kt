package storage

/**
 * Web platform implementation of StorageManager
 */
actual object PlatformStorage : StorageManager {
    override fun saveToLocalStorage(jsonData: String) {
        WebStorage.saveToLocalStorage(jsonData)
    }

    override fun loadFromLocalStorage(): String? {
        return WebStorage.loadFromLocalStorage()
    }

    override fun clearLocalStorage() {
        WebStorage.clearLocalStorage()
    }

    override fun downloadJson(jsonData: String, filename: String) {
        WebStorage.downloadJson(jsonData, filename)
    }

    override fun uploadJson(onFileLoaded: (String) -> Unit) {
        WebStorage.uploadJson(onFileLoaded)
    }
}
