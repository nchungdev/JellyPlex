package org.jellyplex.client.ui.common.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

// ─────────────────────────────────────────────────────────────────────────────
// Section Navigator — vertical list of focusable sections (e.g. rows on a home screen)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Manages focus across an ordered vertical list of sections.
 *
 * Each section exposes a [FocusRequester] via [requesters]. Call [navigateUp]/[navigateDown]
 * to move focus between sections. When navigating past the first section, [onExitTop] is invoked
 * (e.g. to return focus to a sidebar). Navigation stops silently at the last section (no wrap).
 */
@Stable
class DpadSectionNavigator(
    val count: Int,
    private val onExitTop: (() -> Unit)? = null,
) {
    val requesters: Array<FocusRequester> = Array(count) { FocusRequester() }

    fun focusSection(index: Int) {
        if (index in 0 until count) {
            try {
                requesters[index].requestFocus()
            } catch (_: IllegalStateException) {
            }
        }
    }

    fun navigateDown(currentIndex: Int) {
        if (currentIndex < count - 1) focusSection(currentIndex + 1)
        // At last section: intentionally no-op (no wrap-around)
    }

    fun navigateUp(currentIndex: Int) {
        if (currentIndex > 0) focusSection(currentIndex - 1)
        else onExitTop?.invoke()
    }
}

@Composable
fun rememberDpadSectionNavigator(
    count: Int,
    onExitTop: (() -> Unit)? = null,
): DpadSectionNavigator = remember(count) { DpadSectionNavigator(count, onExitTop) }

/**
 * Key handler for an item inside a horizontally-scrolling section row.
 *
 * - Up/Down: delegates to [navigator] for cross-section movement.
 * - Right at last item: consumed (prevents focus escaping the row boundary).
 * - Left at first item: calls [onExitLeft] then lets focus traverse naturally (sidebar).
 */
fun Modifier.sectionItemDpadHandler(
    itemIndex: Int,
    itemCount: Int,
    sectionIndex: Int,
    navigator: DpadSectionNavigator,
    onExitLeft: (() -> Unit)? = null,
): Modifier = this.onKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
    when (event.key) {
        Key.DirectionDown -> {
            navigator.navigateDown(sectionIndex); true
        }

        Key.DirectionUp -> {
            navigator.navigateUp(sectionIndex); true
        }

        Key.DirectionRight -> itemIndex == itemCount - 1
        Key.DirectionLeft -> {
            if (itemIndex == 0) onExitLeft?.invoke()
            false // always let Compose traverse naturally toward sidebar
        }

        else -> false
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Grid Navigator — 2D grid of focusable items (e.g. MediaGrid with chunked rows)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Manages focus across rows of a 2D grid. Each row has its own [FocusRequester]
 * via [rowRequesters]. Navigation stops at the first and last rows (no wrap).
 */
@Stable
class DpadGridNavigator(val rowCount: Int) {
    val rowRequesters: Array<FocusRequester> = Array(rowCount) { FocusRequester() }

    fun navigateUp(rowIndex: Int) {
        if (rowIndex > 0) {
            try {
                rowRequesters[rowIndex - 1].requestFocus()
            } catch (_: IllegalStateException) {
            }
        }
    }

    fun navigateDown(rowIndex: Int) {
        if (rowIndex < rowCount - 1) {
            try {
                rowRequesters[rowIndex + 1].requestFocus()
            } catch (_: IllegalStateException) {
            }
        }
    }
}

@Composable
fun rememberDpadGridNavigator(rowCount: Int): DpadGridNavigator =
    remember(rowCount) { DpadGridNavigator(rowCount) }

/**
 * Key handler for an item inside a grid row.
 *
 * - Up/Down: delegates to [navigator] for cross-row movement.
 * - Right at last column: consumed (prevents focus escaping the row boundary).
 */
fun Modifier.gridItemDpadHandler(
    rowIndex: Int,
    colIndex: Int,
    colCount: Int,
    navigator: DpadGridNavigator,
): Modifier = this.onKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
    when (event.key) {
        Key.DirectionDown -> {
            navigator.navigateDown(rowIndex); true
        }

        Key.DirectionUp -> {
            navigator.navigateUp(rowIndex); true
        }

        Key.DirectionRight -> colIndex == colCount - 1
        else -> false
    }
}
