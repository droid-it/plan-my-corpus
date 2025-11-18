package model

import kotlinx.serialization.Serializable

/**
 * Metadata for exported financial plan snapshots
 * Provides context and versioning information for imports/exports
 */
@Serializable
data class ExportMetadata(
    val appVersion: String = "2.0.0",
    val exportDate: String = "",           // ISO format: "2025-01-15"
    val snapshotLabel: String = "",        // User-provided: "Annual Review 2025"
    val userNotes: String = "",            // Optional multi-line notes
    val totalYears: Int = 0                // Number of years tracked in this export
)
