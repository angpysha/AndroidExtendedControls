package io.github.angpysha.extendedcontrolslibrary

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.resources.R
import androidx.core.widget.TextViewCompat

class ExtendedTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle) : androidx.appcompat.widget.AppCompatEditText(context,attrs,defStyleAttr)
{

    var isNumeric: Boolean
    get() = isNumeric
    set(value) {isNumeric = value}

    init {
        attrs?.let {
          //  val isNumeric = it.getAttributeBooleanValue(R.styleable.ExtendedTextView_isNumeric,false)
        }
    }
}