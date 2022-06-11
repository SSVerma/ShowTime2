package com.ssverma.core.networking.utils

/**
 * Merge two source sets based on given rules:
 *  1. Keeps identical values only once in the result
 *  2. Keeps most recent identical instance in the list
 *  3. Appends the new source set at the beginning
 *  4. Always return new instance of merged list
 */
internal fun <T> List<T>.mergeOrderedWith(
    with: List<T>,
    identical: (oldValue: T, newValue: T) -> Boolean
): List<T> {
    val result = mutableListOf<T>()

    if (this.isEmpty() && with.isEmpty()) {
        return result
    }

    if (this.isEmpty()) {
        result.add(with.first())

        with.subList(1, with.size).forEach { value ->
            var contains = false
            for (i in 0 until result.size) {
                if (identical(result[i], value)) {
                    result[i] = value
                    contains = true
                    break
                }
            }
            if (!contains) {
                result.add(value)
            }
        }

        return result
    }

    if (with.isEmpty()) {
        result.addAll(this)
        return result
    }

    val original = mutableListOf<T>()
    original.addAll(this)

    val newArrived = LinkedHashSet<T>()
    newArrived.addAll(with)

    this.forEachIndexed { index, oldValue ->
        with.forEach { newValue ->
            if (identical(oldValue, newValue)) {
                original[index] = newValue
                newArrived.remove(newValue)
            }
        }
    }

    // keep new arrived first
    result.addAll(newArrived)
    // then existing
    result.addAll(original)

    return result
}