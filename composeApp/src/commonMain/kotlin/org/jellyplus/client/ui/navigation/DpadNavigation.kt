package org.jellyplus.client.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import kotlinx.coroutines.delay
import org.jellyplus.client.logDebug

/** adb logcat -s JellyDpad  → trace every D-pad focus decision. */
private const val DPAD_LOG = "JellyDpad"

/**
 * Robustly requests focus on [focusRequester] once it is attached.
 *
 * On a TV screen something must always be focused, but the target may not be
 * laid out yet on first composition (async content). This retries for a short
 * window so focus reliably lands instead of silently failing and leaving the
 * D-pad stuck with nothing focused.
 *
 * Re-runs whenever [key] changes (pass the content/loading state so focus is
 * (re)claimed when a screen transitions from loading/empty to ready).
 */
@Composable
fun RequestInitialFocus(focusRequester: FocusRequester, key: Any? = Unit) {
    LaunchedEffect(key) {
        repeat(20) { attempt ->
            try {
                focusRequester.requestFocus()
                logDebug(DPAD_LOG, "RequestInitialFocus OK (attempt ${attempt + 1}, key=$key)")
                return@LaunchedEffect
            } catch (_: IllegalStateException) {
                delay(24)
            }
        }
        logDebug(DPAD_LOG, "RequestInitialFocus GAVE UP after 20 attempts (key=$key) — nothing focused!")
    }
}

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
        if (index !in 0 until count) {
            logDebug(DPAD_LOG, "focusSection($index) IGNORED — out of range [0,$count)")
            return
        }
        try {
            requesters[index].requestFocus()
            logDebug(DPAD_LOG, "focusSection($index) OK")
        } catch (e: IllegalStateException) {
            // This is the smoking gun for the stuck-focus bug: the section's
            // FocusRequester is not attached (its row item was recycled).
            logDebug(DPAD_LOG, "focusSection($index) FAILED — requester not attached: ${e.message}")
        }
    }

    /**
     * Vertical navigation always consumes the key so focus can never escape
     * the content area to nowhere (Leanback/Google-TV rule). At the last
     * section this is a no-op — focus simply stays put.
     */
    fun navigateDown(currentIndex: Int): Boolean {
        if (currentIndex < count - 1) {
            logDebug(DPAD_LOG, "DOWN: section $currentIndex -> ${currentIndex + 1} (count=$count)")
            focusSection(currentIndex + 1)
        } else {
            logDebug(DPAD_LOG, "DOWN: section $currentIndex is LAST — no-op (consumed)")
        }
        return true
    }

    /** Always consumes; at the top, delegates to [onExitTop] (e.g. sidebar). */
    fun navigateUp(currentIndex: Int): Boolean {
        if (currentIndex > 0) {
            logDebug(DPAD_LOG, "UP: section $currentIndex -> ${currentIndex - 1} (count=$count)")
            focusSection(currentIndex - 1)
        } else {
            logDebug(DPAD_LOG, "UP: section $currentIndex is TOP — exitTop=${onExitTop != null}")
            onExitTop?.invoke()
        }
        return true
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
    onNavigateUp: (() -> Unit)? = null,
    onNavigateDown: (() -> Unit)? = null,
): Modifier = this.onKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
    when (event.key) {
        Key.DirectionDown -> {
            logDebug(DPAD_LOG, "KEY Down @ section=$sectionIndex item=$itemIndex/$itemCount")
            onNavigateDown?.invoke()
            navigator.navigateDown(sectionIndex)
        }

        Key.DirectionUp -> {
            logDebug(DPAD_LOG, "KEY Up @ section=$sectionIndex item=$itemIndex/$itemCount")
            onNavigateUp?.invoke()
            navigator.navigateUp(sectionIndex)
        }

        Key.DirectionRight -> {
            val consume = itemIndex == itemCount - 1
            logDebug(DPAD_LOG, "KEY Right @ section=$sectionIndex item=$itemIndex/$itemCount consume=$consume")
            consume
        }
        Key.DirectionLeft -> {
            logDebug(DPAD_LOG, "KEY Left @ section=$sectionIndex item=$itemIndex (exitLeft=${itemIndex == 0})")
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

    private fun focusRow(rowIndex: Int) {
        try {
            rowRequesters[rowIndex].requestFocus()
            logDebug(DPAD_LOG, "grid focusRow($rowIndex) OK")
        } catch (e: IllegalStateException) {
            logDebug(DPAD_LOG, "grid focusRow($rowIndex) FAILED — requester not attached: ${e.message}")
        }
    }

    /** Always consumes (no-op at the first row) so focus never escapes. */
    fun navigateUp(rowIndex: Int): Boolean {
        if (rowIndex > 0) focusRow(rowIndex - 1)
        return true
    }

    /** Always consumes (no-op at the last row) so focus never escapes. */
    fun navigateDown(rowIndex: Int): Boolean {
        if (rowIndex < rowCount - 1) focusRow(rowIndex + 1)
        return true
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
        Key.DirectionDown -> navigator.navigateDown(rowIndex)
        Key.DirectionUp -> navigator.navigateUp(rowIndex)

        Key.DirectionRight -> colIndex == colCount - 1
        else -> false
    }
}
