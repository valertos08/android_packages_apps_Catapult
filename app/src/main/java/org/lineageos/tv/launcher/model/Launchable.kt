/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.model

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

open class Launchable(
    val mLabel: String,
    val mPackageName: String,
    val mIcon: Drawable,
    val mContext: Context,
) {
    open val mHasMenu = true
    val mLaunchIntent by lazy { setIntent() }

    protected open fun setIntent(): Intent? {
        return null
    }
}
