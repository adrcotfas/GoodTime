/**
 *     Goodtime Productivity
 *     Copyright (C) 2025 Adrian Cotfas
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.apps.adrcotfas.goodtime.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.apps.adrcotfas.goodtime.R

data class OnboardingPage(
    @StringRes val title: Int,
    @StringRes val description: Int,
    @DrawableRes val image: Int,
) {
    companion object {
        val pages = listOf(
            OnboardingPage(
                title = R.string.intro_title_1,
                description = R.string.intro_description_1,
                image = R.drawable.intro1,
            ),
            OnboardingPage(
                title = R.string.intro_title_2,
                description = R.string.intro_description_2,
                image = R.drawable.intro2,
            ),
            OnboardingPage(
                title = R.string.intro_title_3,
                description = R.string.intro_description_3,
                image = R.drawable.intro3,
            ),
        )
    }
}