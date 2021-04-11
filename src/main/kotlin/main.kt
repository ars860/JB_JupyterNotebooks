import java.io.File
import java.nio.file.Paths

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Expected 2 file names")
        return
    }
    var (firstFileName, secondFileName) = args

    val baseDirectory = Paths.get("").toAbsolutePath()
    firstFileName = baseDirectory.resolve(firstFileName).toAbsolutePath().toString()
    secondFileName = baseDirectory.resolve(secondFileName).toAbsolutePath().toString()

    val firstFileLines = try {
        File(firstFileName).readLines()
    } catch (e: Exception) {
        println("Can't read the first file")
        return
    }

    val secondFileLines = try {
        File(secondFileName).readLines()
    } catch (e: Exception) {
        println("Can't read the second file")
        return
    }

    val diffList = diff(firstFileLines, secondFileLines)

    val outputFileName = baseDirectory.resolve("diff.html").toString()
    println("Report generated in: $outputFileName")
    val reportHtml = createReport(diffList, firstFileName to secondFileName)

    File(outputFileName).printWriter().use { out ->
        out.println(reportHtml.toHtml())
    }
}