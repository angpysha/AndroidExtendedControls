package io.github.angpysha.extendedcontrolslibrary

import io.github.angpysha.extendedcontrolslibrary.Models.CurvedBarMenuItem

interface IMenuItemClickListener {
    fun onMenuItemClick(item: CurvedBarMenuItem, index: Int)
}