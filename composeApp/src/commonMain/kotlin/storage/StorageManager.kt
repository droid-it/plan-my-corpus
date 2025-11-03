package storage

/**
 * Common interface for storage operations
 */
interface StorageManager {
    fun saveToLocalStorage(jsonData: String)
    fun loadFromLocalStorage(): String?
    fun clearLocalStorage()
    fun downloadJson(jsonData: String, filename: String)
    fun uploadJson(onFileLoaded: (String) -> Unit)
}

/**
 * Expect declaration - platform will provide implementation
 */
expect object PlatformStorage : StorageManager
