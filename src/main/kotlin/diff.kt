import DiffLineType.*
import java.lang.Integer.max

enum class DiffLineType {
    ADDED, REMOVED, KEPT, MODIFIED_FROM, MODIFIED_TO, BLANK
}

data class DiffLine<T>(
    val type: DiffLineType,
    val value: T? = null
)

fun <T> diff(first: List<T>, second: List<T>): MutableList<DiffLine<T>> {
    val f = Array(first.size + 1) { IntArray(second.size + 1) }

    for ((n1, s1) in first.withIndex()) {
        for ((n2, s2) in second.withIndex()) {
            f[n1 + 1][n2 + 1] =
                if (s1 == s2)
                    f[n1][n2] + 1
                else
                    max(f[n1][n2 + 1], f[n1 + 1][n2])
        }
    }

    // Restore ans
    var current = Pair(first.lastIndex + 1, second.lastIndex + 1)
    val equalsList = mutableListOf<Pair<Int, Int>>()
    while (!(current.first == 0 || current.second == 0)) {
        val (n1, n2) = current
        val s1 = first[current.first - 1]
        val s2 = second[current.second - 1]

        when {
            s1 == s2 -> {
                current = Pair(n1 - 1, n2 - 1)
                equalsList.add(Pair(n1 - 1, n2 - 1))
            }
            else -> {
                current = if (f[n1 - 1][n2] > f[n1][n2 - 1]) {
                    Pair(n1 - 1, n2)
                } else {
                    Pair(n1, n2 - 1)
                }
            }
        }
    }

    equalsList.sortBy { (a, _) -> a }

    var prev = Pair(-1, -1)
    val diffList = mutableListOf<DiffLine<T>>()
    for ((a, b) in equalsList.apply { add(Pair(first.size, second.size)) }) {
        val (prevA, prevB) = prev

        val modifiedRange = (prevA + 1 until a).zip(prevB + 1 until b)
        for ((i, j) in modifiedRange) {
            diffList.add(DiffLine(MODIFIED_FROM, first[i]))
            diffList.add(DiffLine(MODIFIED_TO, second[j]))
        }

        for (i in prevA + 1 + modifiedRange.size until a) {
            diffList.add(DiffLine(REMOVED, first[i]))
        }

        for (i in prevB + 1 + modifiedRange.size until b) {
            diffList.add(DiffLine(ADDED, second[i]))
        }

        if (a != first.size) {
            diffList.add(DiffLine(KEPT, first[a]))
        }

        prev = Pair(a, b)
    }

    return diffList
}