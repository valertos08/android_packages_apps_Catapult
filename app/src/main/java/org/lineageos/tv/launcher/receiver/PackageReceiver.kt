/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PackageReceiver : BroadcastReceiver() {
    companion object {
        internal var onPackageInstalledCallback: (packageName: String) -> Unit = {}
        internal var onPackageUninstalledCallback: (packageName: String) -> Unit = {}
    }

    override fun onReceive(context: Context, intent: Intent) {
        val data = intent.data
        if (data == null || data.scheme != "package") {
            return
        }

        val packageName = data.schemeSpecificPart
        if (intent.action == Intent.ACTION_PACKAGE_ADDED) {
            onPackageInstalledCallback(packageName)
        } else if (intent.action == Intent.ACTION_PACKAGE_FULLY_REMOVED) {
            onPackageUninstalledCallback(packageName)
        }
    }
}
