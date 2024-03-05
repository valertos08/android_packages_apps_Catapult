/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.model

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

class AppInfo : Launchable {
    var mBanner: Drawable?
    private val mPackageManager: PackageManager = mContext.packageManager

    constructor(resolveInfo: ResolveInfo, context: Context) : super(
        resolveInfo.loadLabel(context.packageManager).toString(),
        resolveInfo.activityInfo.packageName,
        resolveInfo.loadIcon(context.packageManager),
        context
    ) {
        mBanner = resolveInfo.activityInfo.loadBanner(mPackageManager)
        if (mBanner == null) {
            mBanner = resolveInfo.activityInfo.applicationInfo.loadBanner(mPackageManager)
        }
    }

    constructor(app: ApplicationInfo, context: Context) : super(
        app.loadLabel(context.packageManager).toString(),
        app.packageName,
        app.loadIcon(context.packageManager),
        context
    ) {
        mBanner = app.loadBanner(mPackageManager)
    }

    override fun setIntent(): Intent? {
        return mPackageManager.getLaunchIntentForPackage(mPackageName)
    }
}
