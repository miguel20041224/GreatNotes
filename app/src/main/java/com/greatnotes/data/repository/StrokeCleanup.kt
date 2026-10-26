package com.greatnotes.data.repository

import com.greatnotes.data.local.file.StrokeFileDataSource
import java.io.IOException

/**
 * Deletes a page's stroke file after its DB row is gone. A failure only leaves an orphan
 * file (harmless, can be swept later), so it must not fail the user-facing operation.
 */
internal suspend fun StrokeFileDataSource.deleteBestEffort(pageId: String) {
    try {
        delete(pageId)
    } catch (_: IOException) {
        // Orphan file; ignored on purpose.
    }
}
