/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.ext

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

fun <T> SharedPreferences.valueFlow(
    key: String,
    valueGetter: SharedPreferences.(key: String) -> T,
) = callbackFlow {
    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
        changedKey?.takeIf { it == key }?.let {
            trySend(valueGetter(it))
        }
    }

    registerOnSharedPreferenceChangeListener(listener)

    // Emit the latest value
    trySend(valueGetter(key))

    awaitClose {
        unregisterOnSharedPreferenceChangeListener(listener)
    }
}

const val FAVORITE_APPS_KEY = "favorite_apps"

/**
 * The list of apps the user added to favorites.
 */
var SharedPreferences.favoriteApps: List<String>
    get() = getString(FAVORITE_APPS_KEY, null)?.split(",") ?: listOf()
    set(value) = edit {
        putString(FAVORITE_APPS_KEY, value.joinToString(","))
    }

const val KNOWN_CHANNELS_KEY = "known_channels"

/**
 * The list of known channels, used for ordering.
 */
var SharedPreferences.knownChannels: List<Long>
    get() = getString(KNOWN_CHANNELS_KEY, null)?.split(",")?.map {
        it.toLong()
    } ?: listOf()
    set(value) = edit {
        putString(KNOWN_CHANNELS_KEY, value.joinToString(","))
    }

const val HIDDEN_CHANNELS_KEY = "hidden_channels"

/**
 * The list of channels' IDs hidden by the user.
 */
var SharedPreferences.hiddenChannels: Set<Long>
    get() = getStringSet(HIDDEN_CHANNELS_KEY, setOf())?.map {
        it.toLong()
    }?.toSet() ?: setOf()
    set(value) = edit {
        putStringSet(HIDDEN_CHANNELS_KEY, value.map { it.toString() }.toSet())
    }

const val HOME_ROLE_REQUEST_DIALOG_DISMISSED = "home_role_request_dialog_dismissed"

/**
 * Whether the user asked to never show again the home role request dialog.
 */
var SharedPreferences.homeRoleRequestDialogDismissed: Boolean
    get() = getBoolean(HOME_ROLE_REQUEST_DIALOG_DISMISSED, false)
    set(value) = edit {
        putBoolean(HOME_ROLE_REQUEST_DIALOG_DISMISSED, value)
    }

const val BACKGROUND_TYPE_KEY = "background_type"

var SharedPreferences.backgroundType: Int
    get() = getInt(BACKGROUND_TYPE_KEY, 0)
    set(value) = edit {
        putInt(BACKGROUND_TYPE_KEY, value)
    }

const val BACKGROUND_COLOR_KEY = "background_color"

var SharedPreferences.backgroundColor: Int
    get() = getInt(BACKGROUND_COLOR_KEY, -1)
    set(value) = edit {
        putInt(BACKGROUND_COLOR_KEY, value)
    }

const val BACKGROUND_IMAGE_URI_KEY = "background_image_uri"

var SharedPreferences.backgroundImageUri: String?
    get() = getString(BACKGROUND_IMAGE_URI_KEY, null)
    set(value) = edit {
        putString(BACKGROUND_IMAGE_URI_KEY, value)
    }

const val APP_CARD_SIZE_KEY = "app_card_size"

var SharedPreferences.appCardSize: Int
    get() = getInt(APP_CARD_SIZE_KEY, 100)
    set(value) = edit {
        putInt(APP_CARD_SIZE_KEY, value)
    }

const val FAVORITE_CARD_SIZE_KEY = "favorite_card_size"

var SharedPreferences.favoriteCardSize: Int
    get() = getInt(FAVORITE_CARD_SIZE_KEY, 100)
    set(value) = edit {
        putInt(FAVORITE_CARD_SIZE_KEY, value)
    }

const val WATCH_NEXT_CARD_SIZE_KEY = "watch_next_card_size"

var SharedPreferences.watchNextCardSize: Int
    get() = getInt(WATCH_NEXT_CARD_SIZE_KEY, 100)
    set(value) = edit {
        putInt(WATCH_NEXT_CARD_SIZE_KEY, value)
    }

const val ALL_APPS_GRID_KEY = "all_apps_grid"

var SharedPreferences.allAppsGrid: Boolean
    get() = getBoolean(ALL_APPS_GRID_KEY, false)
    set(value) = edit {
        putBoolean(ALL_APPS_GRID_KEY, value)
    }

const val ALL_APPS_GRID_COLUMNS_KEY = "all_apps_grid_columns"

var SharedPreferences.allAppsGridColumns: Int
    get() = getInt(ALL_APPS_GRID_COLUMNS_KEY, 4)
    set(value) = edit {
        putInt(ALL_APPS_GRID_COLUMNS_KEY, value)
    }
