import kotlin.random.Random

@JsFun("() => new Date().getFullYear()")
private external fun jsGetFullYear(): Int

@JsFun("() => new Date().getTime()")
private external fun jsGetTime(): Double

@JsFun("(path) => { if (window.location.pathname !== path) { window.history.pushState({}, '', path); } }")
private external fun jsPushState(path: String)

@JsFun("() => window.location.pathname")
private external fun jsGetPath(): String

@JsFun("() => { const path = window.location.pathname; const segments = path.split('/').filter(s => s); return segments.length > 0 && !['profile', 'inflation-rates', 'investment-categories', 'portfolio', 'contributions', 'goals', 'analysis', 'settings', 'about'].includes(segments[0]) ? '/' + segments[0] : ''; }")
private external fun jsGetBasePath(): String

actual fun getCurrentYear(): Int {
    return jsGetFullYear()
}

actual fun currentTimeMillis(): Long {
    return jsGetTime().toLong()
}

actual fun randomUUID(): String {
    // Simple UUID v4 generator
    return buildString {
        for (i in 0 until 36) {
            when (i) {
                8, 13, 18, 23 -> append('-')
                14 -> append('4') // UUID version 4
                19 -> append(listOf('8', '9', 'a', 'b').random())
                else -> append(Random.nextInt(16).toString(16))
            }
        }
    }
}

actual fun updateBrowserUrl(screen: Screen) {
    val basePath = jsGetBasePath()
    val screenPath = when (screen) {
        Screen.Dashboard -> "/"
        Screen.UserProfile -> "/profile"
        Screen.InflationRates -> "/inflation-rates"
        Screen.InvestmentCategories -> "/investment-categories"
        Screen.Portfolio -> "/portfolio"
        Screen.Goals -> "/goals"
        Screen.Analysis -> "/analysis"
        Screen.Settings -> "/settings"
        Screen.About -> "/about"
    }
    val fullPath = if (basePath.isEmpty() || screenPath == "/") {
        basePath + screenPath
    } else {
        basePath + screenPath
    }
    jsPushState(fullPath)
}

actual fun getBrowserPath(): String {
    val fullPath = jsGetPath()
    val basePath = jsGetBasePath()

    // Strip base path to get the app route
    return if (basePath.isNotEmpty() && fullPath.startsWith(basePath)) {
        fullPath.substring(basePath.length).ifEmpty { "/" }
    } else {
        fullPath
    }
}

@JsFun("() => { const now = new Date(); const year = now.getFullYear(); const month = String(now.getMonth() + 1).padStart(2, '0'); const day = String(now.getDate()).padStart(2, '0'); return year + '-' + month + '-' + day; }")
private external fun jsFormatDate(): String

actual fun formatDateForFilename(): String {
    return jsFormatDate()
}
