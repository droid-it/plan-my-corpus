package model

import kotlinx.serialization.Serializable

/**
 * Defines the type of a yearly snapshot
 */
@Serializable
enum class SnapshotType {
    /**
     * Historical snapshot - past year data that is locked and read-only
     */
    HISTORICAL,

    /**
     * Current snapshot - active year data used for projections and editable
     */
    CURRENT
}
