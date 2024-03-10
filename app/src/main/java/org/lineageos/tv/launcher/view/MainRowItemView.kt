/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.leanback.widget.HorizontalGridView
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.MainRowItem

class MainRowItemView : LinearLayout {
    // Views
    private val horizontalGridView by lazy { findViewById<HorizontalGridView>(R.id.horizontal_grid) }
    private val titleView by lazy { findViewById<TextView>(R.id.title) }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun setData(mainRowItem: MainRowItem) {
        titleView.text = mainRowItem.label
        horizontalGridView.adapter = mainRowItem.adapter
    }

    init {
        inflate(context, R.layout.vertical_grid_row, this)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}
