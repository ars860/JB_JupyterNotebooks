import DiffLineType.*
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiffTest {
    @Test
    fun equalTest() {
        val lines = listOf("a", "b", "c")

        val diffList = diff(lines, lines)
        val expected = lines.map { x -> DiffLine(KEPT, x) }
        assertEquals(expected, diffList)
    }

    @Test
    fun addedOneTest() {
        val lines1 = listOf("a", "b", "c")
        val lines2 = listOf("a", "b", "c", "d")

        val diffList = diff(lines1, lines2)
        val expected = lines1.map { x -> DiffLine(KEPT, x) }.toMutableList().apply { add(DiffLine(ADDED, "d")) }
        assertEquals(expected, diffList)
    }

    @Test
    fun multipleAddsAndRemoves() {
        val lines1 = mutableListOf(*(1..20).map { i -> i.toString() }.toTypedArray())
        val lines2 = mutableListOf(*(1..20).map { i -> i.toString() }.toTypedArray())

        for (remove in listOf(11, 8, 5, 3)) {
            lines2.removeAt(remove)
        }
        for (add in listOf(15, 12, 7, 0)) {
            lines2.add(add, "x")
        }

        val diffList = diff(lines1, lines2)

        assertEquals(4, diffList.count { line -> line.type == REMOVED })
        assertEquals(4, diffList.count { line -> line.type == ADDED })

        for (remove in listOf(4, 6, 9, 13)) {
            assertTrue { diffList[remove].type == REMOVED }
        }
        for (remove in listOf(0, 11, 18, 22)) {
            assertTrue { diffList[remove].type == ADDED }
        }
    }

    @Test
    fun modifiedTest() {
        val lines1 = listOf("a", "b", "c", "d", "e", "f")
        val lines2 = listOf("a", "b", "c", "c", "c", "f")

        val diffList = diff(lines1, lines2)
        val expected = listOf(
            DiffLine(KEPT, "a"),
            DiffLine(KEPT, "b"),
            DiffLine(KEPT, "c"),
            DiffLine(MODIFIED_FROM, "d"),
            DiffLine(MODIFIED_TO, "c"),
            DiffLine(MODIFIED_FROM, "e"),
            DiffLine(MODIFIED_TO, "c"),
            DiffLine(KEPT, "f"),
        )

        assertEquals(expected, diffList)
    }

    @Test
    fun firstSecondFilesTest() {
        val lines1 = File("src/test/resources/first.txt").readLines()
        val lines2 = File("src/test/resources/second.txt").readLines()

        val diffList = diff(lines1, lines2)
        val expected = listOf(
            DiffLine(type = REMOVED, value = "test"),
            DiffLine(type = KEPT, value = "abacaba"),
            DiffLine(type = KEPT, value = "b"),
            DiffLine(type = KEPT, value = "c"),
            DiffLine(type = KEPT, value = "d"),
            DiffLine(type = ADDED, value = "e"),
            DiffLine(type = KEPT, value = "f"),
            DiffLine(type = KEPT, value = "g"),
            DiffLine(type = MODIFIED_FROM, value = "x"),
            DiffLine(type = MODIFIED_TO, value = "i"),
            DiffLine(type = MODIFIED_FROM, value = "x"),
            DiffLine(type = MODIFIED_TO, value = "i"),
            DiffLine(type = REMOVED, value = "x"),
            DiffLine(type = REMOVED, value = "x"),
            DiffLine(type = REMOVED, value = "h"),
            DiffLine(type = KEPT, value = "j"),
            DiffLine(type = MODIFIED_FROM, value = "q"),
            DiffLine(type = MODIFIED_TO, value = "k"),
            DiffLine(type = ADDED, value = "r"),
            DiffLine(type = ADDED, value = "x"),
            DiffLine(type = ADDED, value = "y"),
            DiffLine(type = KEPT, value = "z"),
            DiffLine(type = KEPT, value = "Lorem ipsum dolor sit amet"),
            DiffLine(type = REMOVED, value = "abacaba"),
        )
        assertEquals(expected, diffList)
    }
}