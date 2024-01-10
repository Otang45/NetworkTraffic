package otang.app.network.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.ColorInt
import com.google.android.material.R

object ColourUtils {

    @ColorInt
    fun getColorAttr(context: Context, colorRes: Int): Int {
        val typedValue = TypedValue()
        val theme: Resources.Theme = context.theme
        theme.resolveAttribute(colorRes, typedValue, true)
        return typedValue.data
    }

    @ColorInt
    fun getPrimary(context: Context): Int {
        return getColorAttr(context, R.attr.colorPrimary)
    }

    @ColorInt
    fun getSurface(context: Context): Int {
        return getColorAttr(context, R.attr.colorSurface)
    }

    @ColorInt
    fun getOnSurfaceInverse(context: Context): Int {
        return getColorAttr(context, R.attr.colorOnSurfaceInverse)
    }
}