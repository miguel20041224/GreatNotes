package com.greatnotes.domain.util

import java.util.UUID

/** Source of the current time, injectable so use cases stay deterministic in tests. */
fun interface TimeProvider {
    fun nowMs(): Long
}

/** Source of unique identifiers for new entities. */
fun interface IdGenerator {
    fun newId(): String
}

object SystemTimeProvider : TimeProvider {
    override fun nowMs(): Long = System.currentTimeMillis()
}

object UuidIdGenerator : IdGenerator {
    override fun newId(): String = UUID.randomUUID().toString()
}
