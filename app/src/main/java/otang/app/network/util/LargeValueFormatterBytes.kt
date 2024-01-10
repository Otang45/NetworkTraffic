package otang.app.network.util

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class LargeValueFormatterBytes : ValueFormatter() {
    private var mSuffix = arrayOf("", "K", "M", "G", "T")
    private var mMaxLength = 5
    private val mFormat: DecimalFormat = DecimalFormat("###E00")
    private var mText = ""

    override fun getFormattedValue(value: Float): String {
        return makePretty(value.toDouble()) + mText
    }

    /**
     * Formats each number properly. Special thanks to Roman Gromov
     * (https://github.com/romangromov) for this piece of code.
     */
    private fun makePretty(number: Double): String {
        var r = mFormat.format(number)
        val numericValue1 = Character.getNumericValue(r[r.length - 1])
        val numericValue2 = Character.getNumericValue(r[r.length - 2])
        val combined = Integer.valueOf(numericValue2.toString() + "" + numericValue1)
        r = r.replace("E[0-9][0-9]".toRegex(), mSuffix[combined / 3])
        while (r.length > mMaxLength || r.matches("[0-9]+\\.[a-z]".toRegex())) {
            r = r.substring(0, r.length - 2) + r.substring(r.length - 1)
        }
        return r
    }

}
