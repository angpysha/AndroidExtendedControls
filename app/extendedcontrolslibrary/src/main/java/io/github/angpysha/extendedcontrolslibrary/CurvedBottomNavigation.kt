package io.github.angpysha.extendedcontrolslibrary

import android.animation.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnLayout
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import io.github.angpysha.extendedcontrolslibrary.Models.CurvePoint
import io.github.angpysha.extendedcontrolslibrary.Models.CurvedBarMenuItem
import java.lang.Exception
import java.util.*
import kotlin.math.abs

class CurvedBottomNavigation @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val X_OFFSET = "X_OFFSET"
        private const val X_CENTER = "X_CENTER"
        private const val Y_CENTER = "Y_CENTER"
    }



    var xOffset: Float = 0f
    var yOffset: Float = (0.3f*height).toFloat()
    var items:Array<CurvedBarMenuItem>? = null
    var itemsCount: Int = 0
    var curYCenter = (0.4*height).toFloat()
    var defCentrY = (0.4*height).toFloat()

    private lateinit var curveOnePointOne: CurvePoint
    private lateinit var curveOnePointTwo: CurvePoint
    private lateinit var curveOnePointTo: CurvePoint

    private lateinit var curveTwoPointOne: CurvePoint
    private lateinit var curveTwoPointTwo: CurvePoint
    private lateinit var curveTwoPointTo : CurvePoint

    private var curXCenter: Float = -1f

    private lateinit var vectorDrawables: Array<AnimatedVectorDrawableCompat?>
    private val duration: Long = 500

    private val fabSize = resources.getDimensionPixelSize(R.dimen.curved_fab_size)



    var menuItemsViews: MutableList<CurvedBottomBarItemView> = LinkedList()

    private var prevMenuIndex: Int = 0
    private var selectedIndex: Int = 0

    var hasTitle: Boolean = true
        set(value) {
            field = value
        }

    init {

        context.theme.obtainStyledAttributes(attrs,R.styleable.CurvedBottomNavigation,0,0)
            .apply {
                try {
                    hasTitle = getBoolean(R.styleable.CurvedBottomNavigation_bne_hastitles,true)
                } finally {
                    recycle()
                }
            }

        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
    fun setMenuItems(items: Array<CurvedBarMenuItem>,indedx: Int) {
        this.items = items
        this.itemsCount = items.size
        val layout = LinearLayout(context)
        val itemWidth = width/items.count()
        items.forEachIndexed { index, curvedBarMenuItem ->
            val menuItem = CurvedBottomBarItemView(context,curvedBarMenuItem)
            val iheight = resources.getDimensionPixelSize(R.dimen.curved_tabbar_item_height)
            val layoutParams = LinearLayout.LayoutParams(0,iheight)

            menuItemsViews.add(menuItem)

            if (indedx == index)
                menuItem.visibility = View.INVISIBLE
            menuItem.setOnClickListener {
             //   menuItem.visibility = View.INVISIBLE
                OnMenuItemClick(index)
                prevMenuIndex = index
            }


            layoutParams.weight = 1f

            layout.addView(menuItem,layoutParams)
        }

        val bottomLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        resources.getDimensionPixelOffset(R.dimen.curved_tabbar_item_height),
        Gravity.BOTTOM
        )

        selectedIndex = indedx
        setupAVDs();

        addView(layout,bottomLayoutParams)
     //   val path = calculatePath(indedx)
        initializeItems(indedx)
        //curXCenter = getCurrentItemXCenter(selectedIndex)
        animateAVD(indedx)
    }

    private fun initializeItems(index: Int) {
        doOnLayout {
            curXCenter = getCurrentItemXCenter(index)
        }
    }

    private fun animateAVD(index: Int) {
        vectorDrawables?.get(index)?.let {
            it.callback = updateVectorDrawableCallback
            it.start()
        }
    }

    private fun setupAVDs() {
        val avdItems = Array(itemsCount) {
            val avd = AnimatedVectorDrawableCompat.create(context,
                items?.get(it)?.avdIconImageSource?:0)
            avd
        }
        vectorDrawables = avdItems

    }

    private fun OnMenuItemClick(index: Int) {
        prevMenuIndex = selectedIndex
        selectedIndex = index

        menuItemsViews.forEachIndexed { index, curvedBottomBarItemView ->
            if (index == prevMenuIndex)
                curvedBottomBarItemView.visibility = View.VISIBLE
        }

        val offset = index*(width/itemsCount).toFloat()

//        invalidate()
        animateItemSeletion(offset,width/itemsCount,index)
    }

    private fun getCurrentItemXCenter(index: Int) : Float {
        return (width/itemsCount/2).toFloat()+index*(width/itemsCount).toFloat()
    }

    private fun animateItemSeletion(offset: Float, itemWidth: Int, index: Int) {
         calculatePath(index)
        val newXCenter= getCurrentItemXCenter(index)
        val offsetPropertyHolder = PropertyValuesHolder.ofFloat(X_OFFSET,xOffset,offset)
        val yPositionAnimatorHolderHide = PropertyValuesHolder.ofFloat(Y_CENTER,curYCenter,(0.9*height + 0.4*height/2).toFloat())
        val yPositionAnimatorHolderShow = PropertyValuesHolder.ofFloat(Y_CENTER,curYCenter,defCentrY)

        val diff = abs(prevMenuIndex-index)
        val animPerItemDuration = duration/diff

        val xCenterPositionHolder = PropertyValuesHolder.ofFloat(X_CENTER,curXCenter,newXCenter)
        val curveAnimator = getBezierCurveAnimation(index,duration,animPerItemDuration,offsetPropertyHolder,xCenterPositionHolder)
        val hideAnimator = getFabHideAnimation(duration/2,yPositionAnimatorHolderHide)
        val showAnimation = getFabShowAnimation(duration/2,duration/2,index,yPositionAnimatorHolderShow)
        val anim = AnimatorSet()

        anim.playTogether(hideAnimator,curveAnimator,showAnimation)
        anim.interpolator = FastOutLinearInInterpolator()
        anim.start()
    }

    private fun getFabHideAnimation(
       aduration: Long,
        vararg animatroHolder: PropertyValuesHolder)
    : ValueAnimator {
        return ValueAnimator().apply {
            setValues(*animatroHolder)
            duration = aduration
            addUpdateListener {
                val newCenterY = getAnimatedValue(Y_CENTER) as Float
                curYCenter = newCenterY
                invalidate()



            }
        }
    }

    private val updateVectorDrawableCallback = object: Drawable.Callback  {
        override fun unscheduleDrawable(p0: Drawable, p1: Runnable) {

        }

        override fun invalidateDrawable(p0: Drawable) {
            this@CurvedBottomNavigation.invalidate()
        }

        override fun scheduleDrawable(p0: Drawable, p1: Runnable, p2: Long) {

        }

    }

    private fun  getFabShowAnimation(
        delay: Long,
        aduration: Long,
        index: Int,
        vararg animatroHolder: PropertyValuesHolder
    ) : ValueAnimator {
        return ValueAnimator().apply {
            setValues(*animatroHolder)
            duration = aduration

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    menuItemsViews[index].visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationStart(animation)
                    vectorDrawables?.get(index)?.let {
                        try {
                        it.callback = updateVectorDrawableCallback
                        it.start()
                        } catch (ex: Exception) {

                        }

                        finally {

                        }
                    }
                }
            })

            startDelay = delay
            addUpdateListener {
                val newCenterY = getAnimatedValue(Y_CENTER) as Float
                curYCenter = newCenterY
                invalidate()
            }
        }
    }

    private fun getBezierCurveAnimation(
        index:Int,
        animduration: Long,
        animperItemDuration: Long,
        vararg offsetValues: PropertyValuesHolder) : ValueAnimator {
        return ValueAnimator().apply {
            setValues(*offsetValues)
            duration = animduration

            addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)

                }

                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                }
            })

            addUpdateListener {
                val newOffset = getAnimatedValue(X_OFFSET)
                xOffset = newOffset as Float
                calculatePath(index)

                val newXcenter = getAnimatedValue(X_CENTER)
                curXCenter = newXcenter as Float

                invalidate()

                //hide and show menuitemanimation
                val currentTime = it.animatedFraction * animduration
                val curveBottomHalfTime = animduration * fabSize/width
                val currentIndex = ((currentTime+animperItemDuration)/animperItemDuration).toInt()

                when {
                    currentIndex == index -> {

                    }

                    else -> {
                        
                    }
                }

            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val height = paddingBottom + paddingTop + resources.getDimensionPixelSize(R.dimen.curved_tabbar_height)
        val measureSpec = MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY)
        curYCenter = (0.4*height).toFloat()
        defCentrY = (0.4*height).toFloat()
       // calculatePath(selectedIndex)
        //curXCenter = curveOnePointTo.x
        super.onMeasure(widthMeasureSpec, measureSpec)
    }




    private fun calculatePath(index: Int) : Path {
        val path = Path()

        val yOffset  = (0.3f*height)

        val itemWidth = (width/itemsCount).toFloat()

        val baseWidth = itemWidth/3

        val fabradius = (fabSize/2)*1.5
        val xLineLength = curXCenter- fabradius - xOffset
        val itemCurveBeign = xLineLength-xOffset

        curveOnePointOne = CurvePoint((curXCenter- fabradius*0.8).toFloat(),0+yOffset)
        curveOnePointTwo = CurvePoint((curXCenter - fabradius*0.7).toFloat(),
            (0.9*height).toFloat())
        curveOnePointTo = CurvePoint(curXCenter, (0.9*height).toFloat())
        curveTwoPointOne = CurvePoint((curXCenter+ fabradius*0.7).toFloat(),(0.9*height).toFloat())
        curveTwoPointTwo = CurvePoint((curXCenter + fabradius*0.8).toFloat(),0f+yOffset)
        curveTwoPointTo = CurvePoint((curXCenter+fabradius).toFloat(),0f+yOffset)

      //  val xLineLength = curXCenter- fabradius - xOffset
        path.moveTo(0f,height.toFloat())
        path.lineTo(0f,0f+yOffset)
        path.lineTo((xOffset+xLineLength).toFloat(),0f+yOffset)

        path.cubicTo(
            curveOnePointOne.x,curveOnePointOne.y,
            curveOnePointTwo.x,curveOnePointTwo.y,
            curveOnePointTo.x,curveOnePointTo.y
        )

        path.cubicTo(curveTwoPointOne.x,curveTwoPointOne.y,
        curveTwoPointTwo.x,curveTwoPointTwo.y,
        curveTwoPointTo.x,curveTwoPointTo.y)
    //    path.lineTo(curveOnePointTo.x,0f+yOffset)
        path.lineTo(width.toFloat(),0f+yOffset)
        path.lineTo(width.toFloat(),height.toFloat())
        path.lineTo(0f,height.toFloat())
        path.close()

        return path
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        yOffset = (0.3f*height)
        val iWidth = width/8


        val navPaint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.WHITE
            setShadowLayer(18f, 0f, 6f, Color.LTGRAY)
        }

        val circlePaint = Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            color = Color.WHITE
            setShadowLayer(15f, 0f, 6f, Color.LTGRAY)
        }

        val itemWidth = (width/itemsCount).toFloat()

        val baseWidth = itemWidth/3

        val papp = calculatePath(selectedIndex)
        canvas?.drawCircle(curXCenter,curYCenter, (fabSize/2).toFloat(),circlePaint)
//
//        if (vectorDrawables?.get(selectedIndex) != null) {
//            try {
//                vectorDrawables[selectedIndex]?.setBounds((baseWidth+xOffset+baseWidth - (0.4*height/2).toFloat()).toInt(),(curYCenter-(0.4*height/2).toFloat()).toInt(),
//                    (baseWidth+xOffset+baseWidth + (0.4*height/2).toFloat()).toInt(),(curYCenter+(0.4*height/2).toFloat()).toInt())
//                vectorDrawables[selectedIndex]?.draw(canvas!!)
//            } catch (ex: Exception) {
//                val drawable = ResourcesCompat.getDrawable(resources, this.items!![selectedIndex].iconImageSource,context.theme)
//                drawable?.setBounds((baseWidth+xOffset+baseWidth - (0.4*height/2).toFloat()).toInt(),(curYCenter-(0.4*height/2).toFloat()).toInt(),
//                    (baseWidth+xOffset+baseWidth + (0.4*height/2).toFloat()).toInt(),(curYCenter+(0.4*height/2).toFloat()).toInt())
//                drawable?.draw(canvas!!)
//            }
//        }



     //   canvas?.drawPath(path,navPaint)
        canvas?.drawPath(papp,navPaint)
    }
}



