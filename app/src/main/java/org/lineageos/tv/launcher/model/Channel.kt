/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.model

class Channel(
    val id: Long,
    val title: String,
) {
    companion object {
        const val FAVORITE_APPS_ID: Long = -2
        const val ALL_APPS_ID: Long = -3
        const val WATCH_NEXT_ID: Long = -4
    }
}