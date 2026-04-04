/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.utils

import android.graphics.drawable.GradientDrawable

enum class GradientDirection(val orientation: GradientDrawable.Orientation) {
    TOP_BOTTOM(GradientDrawable.Orientation.TOP_BOTTOM),
    BOTTOM_TOP(GradientDrawable.Orientation.BOTTOM_TOP),
    LEFT_RIGHT(GradientDrawable.Orientation.LEFT_RIGHT),
    RIGHT_LEFT(GradientDrawable.Orientation.RIGHT_LEFT),
    TR_BL(GradientDrawable.Orientation.TR_BL),
    BL_TR(GradientDrawable.Orientation.BL_TR),
    BR_TL(GradientDrawable.Orientation.BR_TL),
    TL_BR(GradientDrawable.Orientation.TL_BR);

    companion object {
        fun fromInt(value: Int): GradientDirection {
            return entries.getOrElse(value) { TOP_BOTTOM }
        }
    }
}
