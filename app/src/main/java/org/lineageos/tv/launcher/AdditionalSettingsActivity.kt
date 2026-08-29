/*
 * SPDX-FileCopyrightText: 2024-2025 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.card.MaterialCardView
import org.lineageos.tv.launcher.ext.externalLauncherPkg

class AdditionalSettingsActivity : ModalActivity(R.layout.activity_additional_settings) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = findViewById<LinearLayout>(R.id.external_launcher_container)!!
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val current = prefs.externalLauncherPkg
        val marginDp = (16 * resources.displayMetrics.density).toInt()
        val marginTopDp = (8 * resources.displayMetrics.density).toInt()

        val homeLaunchers = queryHomeLaunchers()

        for (resolveInfo in homeLaunchers) {
            val activityInfo = resolveInfo.activityInfo ?: continue
            val pkg = activityInfo.packageName ?: continue

            val card = MaterialCardView(this).apply {
                cardElevation = 0f
                isFocusable = true
                setContentDescription(resolveInfo.loadLabel(packageManager))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(marginDp, marginTopDp, marginDp, 0)
                }
            }

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(marginDp, marginDp, marginDp, marginDp)
            }

            val icon = ImageView(this).apply {
                val iconDrawable = try {
                    packageManager.getApplicationIcon(pkg)
                } catch (e: Exception) {
                    ContextCompat.getDrawable(this@AdditionalSettingsActivity, android.R.drawable.sym_def_app_icon)
                }
                setImageDrawable(iconDrawable)
                layoutParams = LinearLayout.LayoutParams(
                    (24 * resources.displayMetrics.density).toInt(),
                    (24 * resources.displayMetrics.density).toInt()
                )
            }

            val label = TextView(this).apply {
                text = resolveInfo.loadLabel(packageManager)
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginStart = (16 * resources.displayMetrics.density).toInt()
                }
            }

            val check = TextView(this).apply {
                text = if (pkg == current) "●" else ""
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            row.addView(icon)
            row.addView(label)
            row.addView(check)
            card.addView(row)

            card.setOnClickListener {
                prefs.externalLauncherPkg = pkg
                launchExternalLauncher(pkg)
                finish()
            }

            root.addView(card)
        }

        // Option to clear (use Catapult by default)
        val clearCard = MaterialCardView(this).apply {
            cardElevation = 0f
            isFocusable = true
            setContentDescription(getString(R.string.external_launcher_none))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(marginDp, marginTopDp, marginDp, 0)
            }
        }

        val clearRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(marginDp, marginDp, marginDp, marginDp)
        }

        val clearText = TextView(this).apply {
            text = getString(R.string.external_launcher_none)
            textSize = 18f
        }
        clearRow.addView(clearText)
        clearCard.addView(clearRow)

        clearCard.setOnClickListener {
            prefs.externalLauncherPkg = null
            finish()
        }

        root.addView(clearCard)

        if (homeLaunchers.isEmpty()) {
            findViewById<TextView>(R.id.external_launcher_hint)?.text =
                getString(R.string.external_launcher_not_found)
        }
    }

    private fun launchExternalLauncher(pkg: String) {
        val intent = packageManager.getLaunchIntentForPackage(pkg) ?: return
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun queryHomeLaunchers(): List<android.content.pm.ResolveInfo> {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        return packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .filter { it.activityInfo?.packageName != packageName }
    }
}
