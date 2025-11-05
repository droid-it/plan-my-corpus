import kotlin.random.Random

@JsFun("() => new Date().getFullYear()")
private external fun jsGetFullYear(): Int

@JsFun("() => new Date().getTime()")
private external fun jsGetTime(): Double

@JsFun("(path) => { if (window.location.pathname !== path) { window.history.pushState({}, '', path); } }")
private external fun jsPushState(path: String)

@JsFun("() => window.location.pathname")
private external fun jsGetPath(): String

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
    val path = when (screen) {
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
    jsPushState(path)
}

actual fun getBrowserPath(): String {
    return jsGetPath()
}

@JsFun("() => { const now = new Date(); const year = now.getFullYear(); const month = String(now.getMonth() + 1).padStart(2, '0'); const day = String(now.getDate()).padStart(2, '0'); return year + '-' + month + '-' + day; }")
private external fun jsFormatDate(): String

actual fun formatDateForFilename(): String {
    return jsFormatDate()
}
