package storage

private const val STORAGE_KEY = "financial_planner_data"

@JsFun("(key, value) => localStorage.setItem(key, value)")
private external fun jsSetItem(key: String, value: String)

@JsFun("(key) => localStorage.getItem(key)")
private external fun jsGetItem(key: String): String?

@JsFun("(key) => localStorage.removeItem(key)")
private external fun jsRemoveItem(key: String)

@JsFun("(jsonData, filename) => { const blob = new Blob([jsonData], { type: 'application/json' }); const url = URL.createObjectURL(blob); const anchor = document.createElement('a'); anchor.href = url; anchor.download = filename; anchor.click(); URL.revokeObjectURL(url); }")
private external fun jsDownloadJson(jsonData: String, filename: String)

@JsFun("(callback) => { const input = document.createElement('input'); input.type = 'file'; input.accept = '.json'; input.onchange = (event) => { const file = event.target.files[0]; if (file) { const reader = new FileReader(); reader.onload = (e) => { callback(e.target.result); }; reader.readAsText(file); } }; input.click(); }")
private external fun jsUploadJson(callback: (String) -> Unit)

/**
 * Web-specific storage implementation using localStorage and File APIs
 */
object WebStorage {
    /**
     * Save data to localStorage
     */
    fun saveToLocalStorage(jsonData: String) {
        try {
            jsSetItem(STORAGE_KEY, jsonData)
        } catch (e: Exception) {
            println("Error saving to localStorage: ${e.message}")
        }
    }

    /**
     * Load data from localStorage
     */
    fun loadFromLocalStorage(): String? {
        return try {
            jsGetItem(STORAGE_KEY)
        } catch (e: Exception) {
            println("Error loading from localStorage: ${e.message}")
            null
        }
    }

    /**
     * Clear data from localStorage
     */
    fun clearLocalStorage() {
        try {
            jsRemoveItem(STORAGE_KEY)
        } catch (e: Exception) {
            println("Error clearing localStorage: ${e.message}")
        }
    }

    /**
     * Download JSON data as a file
     */
    fun downloadJson(jsonData: String, filename: String) {
        try {
            jsDownloadJson(jsonData, filename)
        } catch (e: Exception) {
            println("Error downloading JSON: ${e.message}")
        }
    }

    /**
     * Trigger file upload dialog and return the file content
     */
    fun uploadJson(onFileLoaded: (String) -> Unit) {
        try {
            jsUploadJson(onFileLoaded)
        } catch (e: Exception) {
            println("Error uploading JSON: ${e.message}")
        }
    }
}
