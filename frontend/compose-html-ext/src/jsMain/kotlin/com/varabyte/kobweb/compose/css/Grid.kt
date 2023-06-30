package com.varabyte.kobweb.compose.css

import org.jetbrains.compose.web.css.*

@DslMarker
annotation class GridDslMarker

typealias CSSFlexValue = CSSSizeValue<out CSSUnitFlex>

/**
 * A common interface for a parameter that is either a single grid track size or a group of them.
 *
 * This allows a user to convert a CSS value like "1fr repeat(3, 100px) 1fr" into a list of [GridTrackSizeEntry]s.
 */
sealed interface GridTrackSizeEntry

/**
 * Represents all possible size values that can be used to configure a CSS grid track.
 *
 * A track is the space between two grid lines -- it can be used for rows or columns based on context.
 *
 * For example, "auto 100px minmax(0.px, 1fr)" can be represented in Kotlin as
 * `GridTrackSize.Auto, GridTrackSize(100.px), GridTrackSize.minmax(0.px, 1.fr)`.
 */
sealed class GridTrackSize private constructor(private val value: String) : StylePropertyValue {
    override fun toString() = value

    class Keyword private constructor(value: String) : GridTrackSize(value)

    /**
     * A numeric value (or sizing keyword) used for this track.
     *
     * This essentially excludes singleton keywords like "none", "inherit", etc.
     */
    open class TrackBreadth internal constructor(value: String) : GridTrackSize(value), GridTrackSizeEntry

    /** A size which tells the track to be as small as possible while still fitting all of its contents. */
    class FitContent internal constructor(value: Any) : TrackBreadth("fit-content($value)")

    abstract class Repeat protected constructor(value: Any, entries: Array<out GridTrackSizeEntry>) :
        GridTrackSize("repeat($value, ${entries.toTrackListString()})"), GridTrackSizeEntry {
        init {
            check(entries.none { it is Repeat }) { "Repeat calls cannot nest other repeat calls" }
        }
    }

    class TrackRepeat(count: Int, vararg entries: GridTrackSizeEntry) : Repeat(count, entries)
    class AutoRepeat(type: Type, vararg entries: GridTrackSizeEntry) : Repeat(type, entries) {
        enum class Type(private val value: String) {
            AutoFill("auto-fill"),
            AutoFit("auto-fit");

            override fun toString() = value
        }
    }

    /** A size which represents a range of values this track can be. */
    open class MinMax internal constructor(min: Any, max: Any) : TrackBreadth("minmax($min, $max)")

    /** Like [TrackBreadth] but excludes flex values (e.g. `1fr`) */
    open class InflexibleBreadth internal constructor(value: String) : TrackBreadth(value)

    // these are used for grid-template-*
    sealed interface FixedSize

    /** Represents a track size which is fixed, either a pixel or percentage value (e.g. `100px`, `40%`) */
    class FixedBreadth internal constructor(value: String) : InflexibleBreadth(value), FixedSize

    companion object {
        val Auto get() = InflexibleBreadth("auto")
        val MinContent get() = InflexibleBreadth("min-content")
        val MaxContent get() = InflexibleBreadth("max-content")

        operator fun invoke(value: CSSLengthOrPercentageValue) = FixedBreadth(value.toString())
        operator fun invoke(value: CSSFlexValue) = TrackBreadth(value.toString())

        fun minmax(min: InflexibleBreadth, max: TrackBreadth) = MinMax(min, max)

        fun fitContent(value: CSSLengthOrPercentageValue) = FitContent(value)

        fun repeat(count: Int, vararg entries: GridTrackSizeEntry): Repeat = TrackRepeat(count, *entries)
        fun repeat(type: AutoRepeat.Type, vararg entries: GridTrackSizeEntry): Repeat = AutoRepeat(type, *entries)
    }
}

// TODO: Runtime exception if person uses AutoRepeat and TrackRepeat withflexible sizes in the same list?
private fun Array<out GridTrackSizeEntry>.toTrackListString(): String = buildString {
    val names = mutableListOf<String>()
    val entries = this@toTrackListString

    fun appendWithLeadingSpace(value: Any) {
        if (isNotEmpty()) {
            append(' ')
        }
        append(value)
    }

    fun appendNamesIfAny() {
        if (names.isNotEmpty()) {
            appendWithLeadingSpace(names.joinToString(" ", prefix = "[", postfix = "]"))
            names.clear()
        }
    }

    fun appendSize(value: Any) {
        appendNamesIfAny()
        appendWithLeadingSpace(value)
    }

    for (entry in entries) {
        when (entry) {
            is NamedGridTrackSize -> {
                if (entry.startNames != null) {
                    names.addAll(entry.startNames)
                }
                appendSize(entry.size)
                if (entry.endNames != null) {
                    names.addAll(entry.endNames)
                }
            }

            is GridTrackSize -> appendSize(entry)
        }
    }
    appendNamesIfAny()
}

private fun List<GridTrackSizeEntry>.toTrackListString() = toTypedArray().toTrackListString()

/**
 * A CSS grid track size tagged with names.
 */
class NamedGridTrackSize(
    val size: GridTrackSize,
    val startNames: List<String>? = null,
    val endNames: List<String>? = null
) : GridTrackSizeEntry {
    constructor(size: GridTrackSize, startName: String? = null, endName: String? = null) : this(
        size,
        startName?.let { listOf(it) },
        endName?.let { listOf(it) })

    fun replaceNames(startNames: List<String>? = null, endNames: List<String>? = null) =
        NamedGridTrackSize(
            size,
            startNames ?: this.startNames,
            endNames ?: this.endNames
        )
}

fun GridTrackSize.named(startNames: List<String>? = null, endNames: List<String>? = null) =
    NamedGridTrackSize(this, startNames, endNames)

fun GridTrackSize.named(startName: String? = null, endName: String? = null) =
    NamedGridTrackSize(this, startName, endName)

@GridDslMarker
abstract class GridTrackBuilderInRepeat {
    val auto get() = GridTrackSize.Auto
    val minContent get() = GridTrackSize.MinContent
    val maxContent get() = GridTrackSize.MaxContent
    val autoFit get() = GridTrackSize.AutoRepeat.Type.AutoFit
    val autoFill get() = GridTrackSize.AutoRepeat.Type.AutoFill

    internal val tracks = mutableListOf<GridTrackSizeEntry>()

    fun add(track: GridTrackSizeEntry) {
        tracks.add(track)
    }

    fun add(value: CSSLengthOrPercentageValue) = add(GridTrackSize(value))

    fun add(value: CSSFlexValue) = add(GridTrackSize(value))

    fun fitContent(value: CSSPercentageValue) = add(GridTrackSize.fitContent(value))

    fun minmax(min: GridTrackSize.InflexibleBreadth, max: GridTrackSize.TrackBreadth) =
        add(GridTrackSize.minmax(min, max))

    fun minmax(min: GridTrackSize.FixedBreadth, max: GridTrackSize.TrackBreadth) =
        add(GridTrackSize.minmax(min, max))

    fun minmax(min: GridTrackSize.InflexibleBreadth, max: CSSFlexValue) = minmax(min, GridTrackSize(max))

    fun minmax(min: GridTrackSize.InflexibleBreadth, max: CSSLengthOrPercentageValue) = minmax(min, GridTrackSize(max))

    fun minmax(min: CSSLengthOrPercentageValue, max: GridTrackSize.TrackBreadth) = minmax(GridTrackSize(min), max)

    fun minmax(min: CSSLengthOrPercentageValue, max: CSSLengthOrPercentageValue) =
        minmax(GridTrackSize(min), GridTrackSize(max))

    fun minmax(min: CSSLengthOrPercentageValue, max: CSSFlexValue) = minmax(GridTrackSize(min), GridTrackSize(max))

    private fun nameLastTrack(startNames: List<String>?, endNames: List<String>?) {
        check(tracks.isNotEmpty()) { "You must add at least one track before calling this method" }

        val lastTrack = tracks.removeLast()
        tracks.add(
            when (lastTrack) {
                is NamedGridTrackSize -> lastTrack.replaceNames(startNames, endNames)
                is GridTrackSize -> lastTrack.named(startNames, endNames)
            }
        )
    }

    fun named(start: String, end: String) = nameLastTrack(listOf(start), listOf(end))

    fun named(start: String, end: List<String>) = nameLastTrack(listOf(start), end)

    fun named(start: List<String>, end: String) = nameLastTrack(start, listOf(end))

    fun named(start: List<String>, end: List<String>) = nameLastTrack(start, end)

    fun startName(name: String) = nameLastTrack(listOf(name), null)

    fun startNames(vararg names: String) = nameLastTrack(names.toList(), null)

    fun endName(name: String) = nameLastTrack(null, listOf(name))

    fun endNames(vararg names: String) = nameLastTrack(null, names.toList())
}

/**
 * A builder for simplifying the creation of grid track lists.
 */
class GridTrackBuilder : GridTrackBuilderInRepeat() {
    fun repeat(count: Int, block: GridTrackBuilderInRepeat.() -> Unit) {
        val repeatTracks = GridTrackBuilder().apply(block).tracks.toTypedArray()
        add(GridTrackSize.repeat(count, *repeatTracks))
    }

    fun repeat(type: GridTrackSize.AutoRepeat.Type, block: GridTrackBuilderInRepeat.() -> Unit) {
        val repeatTracks = GridTrackBuilder().apply(block).tracks.toTypedArray()
        add(GridTrackSize.repeat(type, *repeatTracks))
    }
}

sealed class GridAuto private constructor(private val value: String) : StylePropertyValue {
    override fun toString() = value

    class Keyword internal constructor(value: String) : GridAuto(value)

    companion object {
        // Keywords
        val None get() = Keyword("none")

        // Global values
        val Inherit get() = Keyword("inherit")
        val Initial get() = Keyword("initial")
        val Revert get() = Keyword("revert")
        val Unset get() = Keyword("unset")
    }
}

fun StyleScope.gridAutoColumns(gridAutoColumns: GridAuto.Keyword) {
    gridAutoColumns(gridAutoColumns.toString())
}

fun StyleScope.gridAutoColumns(vararg gridAutoColumns: GridTrackSizeEntry) {
    gridAutoColumns(gridAutoColumns.toTrackListString())
}

fun StyleScope.gridAutoColumns(block: GridTrackBuilder.() -> Unit) {
    gridAutoColumns(GridTrackBuilder().apply(block).tracks.toTrackListString())
}

fun StyleScope.gridAutoRows(gridAutoRows: GridAuto.Keyword) {
    gridAutoRows(gridAutoRows.toString())
}

fun StyleScope.gridAutoRows(vararg gridAutoRows: GridTrackSizeEntry) {
    gridAutoRows(gridAutoRows.toTrackListString())
}

fun StyleScope.gridAutoRows(block: GridTrackBuilder.() -> Unit) {
    gridAutoRows(GridTrackBuilder().apply(block).tracks.toTrackListString())
}


/**
 * Represents all possible values that can be passed into a CSS grid property.
 *
 * Note: "masonry" purposely excluded as it is not supported in any major browsers
 * See also: https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_grid_layout/Masonry_layout
 *
 * See also: https://developer.mozilla.org/en-US/docs/Web/CSS/grid-template
 */
sealed class GridTemplate private constructor(private val value: String) : StylePropertyValue {
    override fun toString() = value

    class Keyword internal constructor(value: String) : GridTemplate(value)
    companion object {
        // Keywords
        val None get() = Keyword("none")

        // Global
        val Initial get() = Keyword("initial")
        val Inherit get() = Keyword("inherit")
        val Revert get() = Keyword("revert")
        val Unset get() = Keyword("unset")
    }
}

fun StyleScope.gridTemplateColumns(gridTemplateColumns: GridTemplate.Keyword) {
    gridTemplateColumns(gridTemplateColumns.toString())
}

fun StyleScope.gridTemplateColumns(vararg gridTemplateColumns: GridTrackSizeEntry) {
    gridTemplateColumns(gridTemplateColumns.toTrackListString())
}

fun StyleScope.gridTemplateColumns(block: GridTrackBuilder.() -> Unit) {
    gridTemplateColumns(GridTrackBuilder().apply(block).tracks.toTrackListString())
}

fun StyleScope.gridTemplateRows(gridTemplateRows: GridTemplate.Keyword) {
    gridTemplateRows(gridTemplateRows.toString())
}

fun StyleScope.gridTemplateRows(vararg gridTemplateRows: GridTrackSizeEntry) {
    gridTemplateRows(gridTemplateRows.toTrackListString())
}

fun StyleScope.gridTemplateRows(block: GridTrackBuilder.() -> Unit) {
    gridTemplateRows(GridTrackBuilder().apply(block).tracks.toTrackListString())
}

@GridDslMarker
abstract class GridBuilderInAuto {
    protected var cols: List<GridTrackSizeEntry>? = null
    protected var rows: List<GridTrackSizeEntry>? = null
    protected var autoBuilder: GridBuilder? = null

    fun col(value: CSSLengthOrPercentageValue) {
        cols = GridTrackBuilder().apply { add(value) }.tracks
    }

    fun col(value: CSSFlexValue) {
        cols = GridTrackBuilder().apply { add(value) }.tracks
    }

    fun row(value: CSSLengthOrPercentageValue) {
        rows = GridTrackBuilder().apply { add(value) }.tracks
    }

    fun row(value: CSSFlexValue) {
        rows = GridTrackBuilder().apply { add(value) }.tracks
    }

    fun cols(block: GridTrackBuilder.() -> Unit) {
        cols = GridTrackBuilder().apply(block).tracks
    }

    fun rows(block: GridTrackBuilder.() -> Unit) {
        rows = GridTrackBuilder().apply(block).tracks
    }

    internal fun buildInto(scope: StyleScope) {
        scope.display(DisplayStyle.Grid)
        cols?.let { scope.gridTemplateColumns(it.toTrackListString()) }
        rows?.let { scope.gridTemplateRows(it.toTrackListString()) }
        autoBuilder?.let { autoBuilder ->
            autoBuilder.cols?.let { scope.gridAutoColumns(it.toTrackListString()) }
            autoBuilder.rows?.let { scope.gridAutoRows(it.toTrackListString()) }
        }
    }
}

/**
 * A class which allows for a more concise way of declaring a grid.
 *
 * For example:
 *
 * ```
 * // Without the builder
 * Modifier.
 *  gridTemplateColumns(
 *     GridTrackSize(40.px),
 *     GridTrackSize(1.fr),
 *     GridTrackSize.repeat(3, GridTrackSize(200.px))
 *  )
 *  gridTemplateRows(
 *    GridTrackSize(1.fr),
 *    GridTrackSize(1.fr),
 *  )
 *  gridAutoColumns(GridTrackSize(50.px))
 *
 * // With the builder
 * Modifier.grid {
 *   cols { add(40.px); add(1.fr); repeat(3) { add(200.px) } }
 *   rows { add(1.fr); add(1.fr) }
 *   auto { col(50.px) }
 * }
 * ```
 */
class GridBuilder : GridBuilderInAuto() {
    fun auto(block: GridBuilderInAuto.() -> Unit) {
        autoBuilder = GridBuilder().apply(block)
    }
}

fun StyleScope.grid(block: GridBuilder.() -> Unit) {
    GridBuilder().apply(block).buildInto(this)
}
