/*
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.ScrollView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class FocusScrollingScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    private var enforcing = false
    private var highlightedCard: MaterialCardView? = null

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, y)
        enforceFocusedCardVisible()
    }

    override fun scrollToDescendant(child: View) {
        super.scrollToDescendant(findCard(child) ?: child)
        enforceFocusedCardVisible()
    }

    override fun requestChildFocus(child: View, focused: View) {
        super.requestChildFocus(child, focused)
        enforceFocusedCardVisible()
        highlightFocusedCard()
        post { enforceFocusedCardVisible() }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) clearHighlight() else highlightFocusedCard()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        enforceFocusedCardVisible()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        post { enforceFocusedCardVisible() }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        enforceFocusedCardVisible()
    }

    private fun enforceFocusedCardVisible() {
        if (enforcing) return
        val content = getChildAt(0) ?: return
        val focused = findFocus() ?: return
        val card = findCard(focused) ?: return
        if (height == 0) return

        enforcing = true
        val visibleTop = scrollY + paddingTop
        val visibleBottom = scrollY + height - paddingBottom
        val cardTop = card.top
        val cardBottom = card.top + card.height

        var delta = 0
        if (cardBottom > visibleBottom) {
            delta = cardBottom - visibleBottom
        } else if (cardTop < visibleTop) {
            delta = cardTop - visibleTop
        }
        if (delta != 0) {
            scrollTo(scrollX, scrollY + delta)
        }
        enforcing = false
    }

    private fun highlightFocusedCard() {
        val focused = findFocus()
        val card = focused?.let { findCardView(it) }
        if (card === highlightedCard) return
        clearHighlight()
        if (card == null) return
        card.tag = SavedStroke(card.strokeColorStateList, card.strokeWidth, card.cardElevation)
        val focusColor = MaterialColors.getColor(
            this, com.google.android.material.R.attr.colorPrimary
        )
        card.setStrokeColor(ColorStateList.valueOf(focusColor))
        card.strokeWidth = (2 * resources.displayMetrics.density).toInt()
        card.cardElevation = 2 * resources.displayMetrics.density
        highlightedCard = card
    }

    private fun clearHighlight() {
        val card = highlightedCard ?: return
        val saved = card.tag as? SavedStroke
        if (saved != null) {
            card.setStrokeColor(saved.stroke ?: ColorStateList.valueOf(card.strokeColor))
            card.strokeWidth = saved.width
            card.cardElevation = saved.elevation
        }
        card.tag = null
        highlightedCard = null
    }

    private class SavedStroke(
        val stroke: ColorStateList?,
        val width: Int,
        val elevation: Float
    )

    private fun findCardView(view: View): MaterialCardView? {
        val content = getChildAt(0) ?: return null
        var current: View = view
        while (current !== content) {
            if (current is MaterialCardView) return current
            current = current.parent as? View ?: return null
        }
        return null
    }

    private fun findCard(view: View): View? {
        val content = getChildAt(0) ?: return null
        if (view === content) return null
        var current: View = view
        while (current.parent !== content && current.parent is View) {
            current = current.parent as View
        }
        return if (current.parent === content) current else null
    }
}
