import DiffLineType.*
import java.io.File


fun String.indented(cnt: Int): String {
    return prependIndent("    ".repeat((cnt)))
}

fun Array<out ToHTML>.toHtml(indent: Int): String {
    return joinToString(separator = "\n") { x -> x.toHtml(indent) }
}

interface ToHTML {
    fun toHtml(indent: Int = 0): String
}

class Styles(val selector: String, vararg val stylesList: Pair<String, String>) : ToHTML {
    override fun toHtml(indent: Int): String {
        return "$selector {".indented(indent) + "\n" +
                stylesList.joinToString(separator = "\n") { (name, value) -> "$name: $value;" }
                    .indented(indent + 1) + "\n" +
                "}".indented(indent)
    }
}

class Props(vararg val propsList: Pair<String, String>) : ToHTML {
    override fun toHtml(indent: Int): String {
        return propsList.joinToString(separator = " ") { (name, value) -> "$name=$value" }
    }
}

val emptyProps = Props()

class Text(val text: String) : ToHTML {
    override fun toHtml(indent: Int): String {
        return text.indented(indent)
    }
}

class Tag(val name: String, val props: Props = emptyProps, vararg val children: ToHTML) : ToHTML {
    override fun toHtml(indent: Int): String {
        val propsRendered = "${if (props == emptyProps) "" else " "}${props.toHtml()}"
        return "<$name$propsRendered${if (children.isEmpty()) " /" else ""}>".indented(indent) +
                if (children.isNotEmpty())
                    "\n" + children.toHtml(indent + 1) + "\n" +
                            "</$name>".indented(indent)
                else ""
    }
}

fun div(className: String, vararg children: ToHTML): Tag {
    return Tag("div", Props("class" to "\"$className\""), *children)
}

fun div(vararg children: ToHTML): Tag {
    return Tag("div", emptyProps, *children)
}

fun tag(name: String, props: Props, vararg children: ToHTML): Tag {
    return Tag(name, props, *children)
}

fun tag(name: String, vararg children: ToHTML): Tag {
    return Tag(name, emptyProps, *children)
}

fun <T> createReport(diffList: List<DiffLine<T>>, fileNames: Pair<String, String>): Tag {
    fun DiffLine<T>.toHtml(): String {
        var className = when (type) {
            ADDED -> "added"
            KEPT -> "kept"
            REMOVED -> "removed"
            BLANK -> "blank"
            MODIFIED_FROM -> "modified-from"
            MODIFIED_TO -> "modified-to"
        }
        className = (if (type != BLANK) "withBorder " else "") + className

        return "<div class=\"$className\">${if (type == BLANK) "" else value.toString()}</div>"
    }

    val leftList = mutableListOf<DiffLine<T>>()
    val rightList = mutableListOf<DiffLine<T>>()

    for (line in diffList) {
        val (type, _) = line
        when (type) {
            KEPT -> {
                leftList.add(line)
                rightList.add(line)
            }
            REMOVED -> {
                leftList.add(line)
                rightList.add(DiffLine(BLANK))
            }
            ADDED -> {
                leftList.add(DiffLine(BLANK))
                rightList.add(line)
            }
            MODIFIED_FROM -> {
                leftList.add(line)
            }
            MODIFIED_TO -> {
                rightList.add(line)
            }
            BLANK -> {
                leftList.add(DiffLine(BLANK))
                rightList.add(DiffLine(BLANK))
            }
        }
    }

    val (firstFileName, secondFileName) = fileNames

    val layout = div(
        "layout",
        div(
            "left",
            div("fileName", Text(firstFileName)),
            div(Text(leftList.joinToString(separator = "\n") { line -> line.toHtml() }))
        ),
        div(
            "right",
            div("filename", Text(secondFileName)),
            div(Text(rightList.joinToString(separator = "\n") { line -> line.toHtml() }))
        ),
    )

    val htmlText = tag(
        "html", Props("lang" to "\"en\""),
        tag(
            "head",
            tag("title", Text("Diff Report")),
            tag("meta", Props("charset" to "UTF-8")),
            tag(
                "style",
                Styles(
                    ".added",
                    "background-color" to "#517C00"
                ),
                Styles(
                    ".added::before",
                    "content" to "\"\uD83D\uDE0E\""
                ),
                Styles(
                    ".removed",
                    "background-color" to "#DC143C"
                ),
                Styles(
                    ".removed::before",
                    "content" to "\"\uD83D\uDC80\""
                ),
                Styles(
                    ".blank::before",
                    "content" to "\"⬇️\""
                ),
                Styles(
                    ".kept",
                    "background-color" to "#DBDECD"
                ),
                Styles(
                    ".kept::before",
                    "content" to "\"\uD83D\uDE10\""
                ),
                Styles(
                    ".modified-from",
                    "background-color" to "#4663E9"
                ),
                Styles(
                    ".modified-to",
                    "background-color" to "#4663E9"
                ),
                Styles(
                    ".modified-from::before, .modified-to::before",
                    "content" to "\"\uD83D\uDE35\""
                ),
                Styles(
                    ".layout",
                    "display" to "flex",
                    "flex-direction" to "row",
                    "width" to "100%",
                    "justify-content" to "center"
                ),
                Styles(
                    ".layout > div",
                    "min-width" to "100px",
                    "padding" to "10px"
                ),
                Styles(
                    ".layout div > div",
                    "padding" to "5px",
                    "margin" to "3px"
                ),
                Styles(
                    ".withBorder",
                    "border-radius" to "5px",
                    "border" to "solid 1px black"
                ),
                Styles(
                    ".blank",
                    "border" to "1px solid transparent"
                ),
                Styles(
                    ".fileName",
                    "text-align" to "center",
                    "font-weight" to "bold"
                )
            )
        ),
        tag(
            "body",
            layout
        )
    )

    return htmlText
}