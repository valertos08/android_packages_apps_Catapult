/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.flow

import android.content.Context
import androidx.core.os.bundleOf
import androidx.tvprovider.media.tv.PreviewChannel
import androidx.tvprovider.media.tv.TvContractCompat
import kotlinx.coroutines.flow.map
import org.lineageos.tv.launcher.ext.mapEachRow
import org.lineageos.tv.launcher.ext.queryFlow

@Suppress("RestrictedApi")
class PreviewChannelsFlow(private val context: Context) : QueryFlow<PreviewChannel> {
    override fun flowCursor() = context.contentResolver.queryFlow(
        TvContractCompat.Channels.CONTENT_URI,
        PreviewChannel.Columns.PROJECTION,
        bundleOf(),
    )

    override fun flowData() = flowCursor().mapEachRow { it, _ ->
        it.takeUnless {
            // TODO: Google bug, to report to them... https://nekobin.com/rolulodudo
            it.getString(PreviewChannel.Columns.COL_APP_LINK_INTENT_URI).isNullOrEmpty()
        }?.let {
            PreviewChannel.fromCursor(it)
        }
    }.map { it.filterNotNull() }
}
