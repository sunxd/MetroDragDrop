package tian.net.dragdropdemo

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * <br/>
 * sunxd<br/>
 * sunxd14@gmail.com<br/>
 * 2018/1/17 下午12:31<br/>
 */
class MetroView: FrameLayout, IMetroView {

    var mMode = 0

    constructor(context: Context) : super(context) {
        init(context, null, 0)
    }

    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        init(context, attr, 0)
    }


    constructor(context: Context, attr: AttributeSet, attrType: Int) : super(context, attr, attrType) {
        init(context, attr, attrType)
    }

    private fun init(context: Context, attr: AttributeSet?, attrType: Int) {
        var typeArray = context.obtainStyledAttributes(attr, R.styleable.MetroLayout)
        mMode = typeArray.getInt(R.styleable.MetroLayout_mode, 0)
        typeArray.recycle()
    }


    override fun getMode(): Int {
        return mMode
    }
}