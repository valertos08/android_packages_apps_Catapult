/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.content.Context
import android.view.ViewGroup
import org.lineageos.tv.launcher.utils.AppManager
import org.lineageos.tv.launcher.view.AddFavoriteCard
import org.lineageos.tv.launcher.view.Card
import kotlin.reflect.safeCast

class AllAppsFavoritesAdapter(context: Context) : AppsAdapter(context) {
    private var favoritePackageNames = AppManager.getFavoriteApps(context)

    override fun handleClick(app: Card) {
        val addFavoriteCard = AddFavoriteCard::class.safeCast(app) ?: return

        if (favoritePackageNames.contains(app.packageName)) {
            AppManager.removeFavoriteApp(context, app.packageName)
            addFavoriteCard.setActionAdd()
        } else {
            AppManager.addFavoriteApp(context, app.packageName)
            addFavoriteCard.setActionRemove()
        }

        favoritePackageNames = AppManager.getFavoriteApps(context)
    }

    override fun onBindViewHolder(viewHolder: AppsAdapter.ViewHolder, i: Int) {
        val card = viewHolder.itemView as AddFavoriteCard
        card.setCardInfo(appsList[i])

        if (favoritePackageNames.contains(appsList[i].packageName)) {
            card.setActionRemove()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = AddFavoriteCard(parent.context)

        itemView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        return ViewHolder(itemView)
    }
}
