/*
 * SPDX-FileCopyrightText: 2024 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.content.Context
import android.view.ViewGroup
import org.lineageos.tv.launcher.utils.AppManager
import org.lineageos.tv.launcher.view.AddFavoriteCard

class AllAppsFavoritesAdapter(context: Context) : TvAdapter<AddFavoriteCard>(context) {
    private var favoritePackageNames = AppManager.getFavoriteApps(context)

    override fun handleClick(card: AddFavoriteCard) {
        if (favoritePackageNames.contains(card.packageName)) {
            AppManager.removeFavoriteApp(context, card.packageName)
            card.setActionAdd()
        } else {
            AppManager.addFavoriteApp(context, card.packageName)
            card.setActionRemove()
        }

        favoritePackageNames = AppManager.getFavoriteApps(context)
    }

    override fun onBindViewHolder(viewHolder: TvAdapter<AddFavoriteCard>.ViewHolder, i: Int) {
        val card = viewHolder.itemView as AddFavoriteCard
        card.setCardInfo(launchablesList[i])

        if (favoritePackageNames.contains(launchablesList[i].packageName)) {
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
