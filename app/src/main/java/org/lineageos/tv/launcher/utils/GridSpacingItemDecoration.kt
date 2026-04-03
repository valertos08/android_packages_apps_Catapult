/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val horizontalSpacing: Int,
    private val verticalSpacing: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = state.itemCount

        outRect.left = if (position == 0) 0 else horizontalSpacing
        outRect.right = if (position == itemCount - 1) 0 else horizontalSpacing
        outRect.top = verticalSpacing
        outRect.bottom = verticalSpacing
    }
}
