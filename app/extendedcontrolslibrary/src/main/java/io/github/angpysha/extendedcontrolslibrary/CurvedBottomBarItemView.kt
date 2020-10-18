package io.github.angpysha.extendedcontrolslibrary

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.res.ResourcesCompat
import io.github.angpysha.extendedcontrolslibrary.Models.CurvedBarMenuItem

class CurvedBottomBarItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context,attrs,defStyleAttr) {

   var itemview: View

    val itemTitle: TextView
    val itemImage: ImageView

    var isAnimating: Boolean = false
    constructor(context: Context,itemData: CurvedBarMenuItem?) : this(context) {
        itemTitle?.let {
            it.text = itemData?.title
        }

        itemImage?.let {
            itemData?.let {ii ->
            val drawable = ResourcesCompat.getDrawable(resources,ii.iconImageSource,context.theme)
            it.setImageDrawable(drawable)
            }
        }
    }

    init {
    //    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        this.itemview = LayoutInflater.from(context).inflate(R.layout.menu_item,this,true)

        itemTitle = itemview.findViewById(R.id.curveditem_textView)
        itemImage = itemview.findViewById(R.id.curveditem_imageView)
//        this.addView(itemview)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val height = resources.getDimensionPixelSize(R.dimen.curved_tabbar_item_height)
        val measureSpec = MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, measureSpec)
    }


    fun startShowAnimation(time: Long) {
        if (isAnimating)
            return

        val showAnimation = getShowAnimation(time)

        val set = AnimatorSet()
        set.play(showAnimation)
        set.start()
    }

    fun startHideAnimation(time: Long) {
        if (isAnimating)
            return

        val hideAnimation = getHideAnimation(time)

        val set = AnimatorSet()
        set.play(hideAnimation)
        set.start()
    }

    fun startIntermediateAnimation(duration: Long,
                                   offset: Long
    ){
        if(isAnimating)
            return

        val hideAnimation = getHideAnimation(offset)

        val time = duration - offset



        val showAnimation = getShowAnimation(time)
        showAnimation.startDelay = offset
        val set = AnimatorSet()
        set.playTogether(hideAnimation,showAnimation)
        set.start()
    }

    fun resetAnimation() {
        isAnimating = false
    }

    private fun getShowAnimation(time: Long) : ValueAnimator {
        val translation = PropertyValuesHolder.ofFloat("translationY",0.9f*height,0f)
        val alphaProps = PropertyValuesHolder.ofFloat("alpha",0f,1f)
        return ObjectAnimator.ofPropertyValuesHolder(this,
            alphaProps,
        translation).apply {
            duration = time
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)
                    isAnimating = true
                }
            })
        }
    }

    private fun getHideAnimation(time: Long): ValueAnimator {
        val props = PropertyValuesHolder.ofFloat("alpha",1f,0f)
        return ObjectAnimator.ofPropertyValuesHolder(this,
            props).apply {
                duration = time
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        super.onAnimationStart(animation)
                        isAnimating = true
                    }
                })
        }
    }


}