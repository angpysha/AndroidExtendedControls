package io.github.angpysha.extendedcontrolslibrary.Models

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes

data class CurvedBarMenuItem(

    val title: String,
    @DrawableRes
    val iconImageSource: Int,
    @DrawableRes
    val avdIconImageSource: Int,
    @IdRes
    val destionantion: Int = -1)