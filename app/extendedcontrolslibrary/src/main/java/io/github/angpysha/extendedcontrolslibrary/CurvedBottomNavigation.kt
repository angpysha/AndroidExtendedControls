package io.github.angpysha.extendedcontrolslibrary

import android.animation.*
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.IdRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.doOnLayout
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
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
                //prevMenuIndex = index
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
        menuItemListener?.invoke(this!!.items!![index],index)
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
        if (diff==0)
            return
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
                    menuItemsViews?.forEach {
                        it.resetAnimation()
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {
                    super.onAnimationCancel(animation)
                    menuItemsViews?.forEach {
                        it.resetAnimation()
                    }
                }
            })

            addUpdateListener {
                val newOffset = getAnimatedValue(X_OFFSET)
                xOffset = newOffset as Float
                calculatePath(index)
                invalidate()
                val newXcenter = getAnimatedValue(X_CENTER)
                curXCenter = newXcenter as Float



                //hide and show menuitemanimation
                val currentTime = it.animatedFraction * animduration
                val itemWidth = width/itemsCount
                val fabradius = (fabSize/2)*1.5
                val centerX = itemWidth/2
                val lineToFab = centerX-fabradius

           //     val length = lineToFab+2*fabradius+lineToFab
                val length = 2*fabradius

                val curveBottomHalfTime = ((length*animperItemDuration)/itemWidth).toLong()
                var overcomeIndex = ((currentTime+animperItemDuration)/animperItemDuration).toInt()

//                if (overcomeIndex >= abs(selectedIndex-prevMenuIndex))
//                    return@addUpdateListener

                if (prevMenuIndex < selectedIndex) {
                    overcomeIndex+=prevMenuIndex
                    if (overcomeIndex>index)
                        return@addUpdateListener
                } else {
                    overcomeIndex = prevMenuIndex - overcomeIndex
                    if (overcomeIndex < index)
                        return@addUpdateListener
                }
              //  overcomeIndex = prevMenuIndex - overcomeIndex
                val sel = selectedIndex
             //   val iii = 0


                when {
                    overcomeIndex == index -> {
                        menuItemsViews[overcomeIndex].startHideAnimation((curveBottomHalfTime).toLong())
                        if (abs(prevMenuIndex-index) == 1) {
                            menuItemsViews[prevMenuIndex].startShowAnimation((animperItemDuration).toLong())
                        }

                    }

                    abs(overcomeIndex-prevMenuIndex) == 1 -> {
                        menuItemsViews[prevMenuIndex].startShowAnimation((animperItemDuration).toLong())
                        menuItemsViews[overcomeIndex].startIntermediateAnimation((1.5*animperItemDuration).toLong(),(curveBottomHalfTime).toLong())
                      //  menuItemsViews[overcomeIndex].visibility = View.INVISIBLE

                        // menuItemsViews[overcomeIndex].startIntermediateAnimation(animperItemDuration,curveBottomHalfTime)
                    }

                    else -> {
                    //    val iii =0
                        menuItemsViews[overcomeIndex].startIntermediateAnimation((1.5*animperItemDuration).toLong(),
                            (curveBottomHalfTime).toLong()
                        )
                     //  menuItemsViews[overcomeIndex].visibility = View.INVISIBLE
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

        curveOnePointOne = CurvePoint((curXCenter- fabradius*0.7).toFloat(),0+yOffset)
        curveOnePointTwo = CurvePoint((curXCenter - fabradius*0.8).toFloat(),
            (0.9*height).toFloat())
        curveOnePointTo = CurvePoint(curXCenter, (0.9*height).toFloat())
        curveTwoPointOne = CurvePoint((curXCenter+ fabradius*0.8).toFloat(),(0.9*height).toFloat())
        curveTwoPointTwo = CurvePoint((curXCenter + fabradius*0.7).toFloat(),0f+yOffset)
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

    private var menuItemListener : ((CurvedBarMenuItem,Int) -> Unit)? = null
    fun setOnMenuItemClickListener(listener: (CurvedBarMenuItem,Int) -> Unit) {
        this.menuItemListener = listener
    }

    // function to setup with navigation controller just like in BottomNavigationView
    fun setupWithNavController(navController: NavController) {
        // check for menu initialization
//        if (!isMenuInitialized) {
//            throw RuntimeException("initialize menu by calling setMenuItems() before setting up with NavController")
//        }

        this.navController = navController
        // the start destination and active index
        if (navController.graph.startDestination != items?.get(selectedIndex)?.destionantion?:0) {
            throw RuntimeException("startDestination in graph doesn't match the activeIndex set in setMenuItems()")
        }

        // initialize the menu
        setOnMenuItemClickListener { item, _ ->
            navigateToDestination(navController, item)
        }
        // setup destination change listener to properly sync the back button press
        navController.addOnDestinationChangedListener { _, destination, _ ->
            for (i in items?.indices!!) {
                if (matchDestination(destination, items!![i].destionantion)) {
               //     OnMenuItemClick(i)
                }
            }
        }
    }

    private lateinit var navController: NavController
    // source code referenced from the actual JetPack Navigation Component
    // refer to the original source code
    private fun navigateToDestination(navController: NavController, itemCbn: CurvedBarMenuItem) {
        if (itemCbn.destionantion == -1) {
            throw RuntimeException("please set a valid id, unable the navigation!")
        }
        val builder = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.nav_default_enter_anim)
            .setExitAnim(R.anim.nav_default_exit_anim)
            .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
            .setPopExitAnim(R.anim.nav_default_pop_exit_anim)


      //  val res = androidx.navigation.R.
        // pop to the navigation graph's start  destination
        builder.setPopUpTo(findStartDestination(navController.graph).id, false)
        val options = builder.build()
        try {
            navController.navigate(itemCbn.destionantion, null, options)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "unable to navigate!", e)
        }
    }

    // source code referenced from the actual JetPack Navigation Component
    // refer to the original source code
    private fun matchDestination(destination: NavDestination, @IdRes destinationId: Int): Boolean {
        var currentDestination = destination
        while (currentDestination.id != destinationId && currentDestination.parent != null) {
            currentDestination = currentDestination.parent!!
        }

        return currentDestination.id == destinationId
    }

    // source code referenced from the actual JetPack Navigation Component
    // refer to the original source code
    private fun findStartDestination(graph: NavGraph): NavDestination {
        var startDestination: NavDestination = graph
        while (startDestination is NavGraph) {
            startDestination = graph.findNode(graph.startDestination)!!
        }

        return startDestination
    }

    fun OnBackPressed() : Boolean {
        val currentNavDestination = navController.currentDestination?.id?:-1
        if (currentNavDestination != -1) {
           val cont =  items?.any { it.destionantion == currentNavDestination } ?: false

            return !cont
        }

        return true
    }

    private var viewPager: ViewPager? = null

    fun setupWithViewPager(viewPager: ViewPager) {
        this.viewPager = viewPager

        setOnMenuItemClickListener { curvedBarMenuItem, i ->
            viewPager.currentItem = i
        }

        viewPager.addOnPageChangeListener(object :
            ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {

            }
        })
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

        if (vectorDrawables?.get(selectedIndex) != null) {
            try {
                vectorDrawables[selectedIndex]?.setBounds((curXCenter - (fabSize/3).toFloat()).toInt(),
                    (curYCenter-(fabSize/3).toFloat()).toInt(),
                    (curXCenter +(fabSize/3).toFloat()).toInt(),
                    (curYCenter+(fabSize/3).toFloat()).toInt())
                vectorDrawables[selectedIndex]?.draw(canvas!!)
            } catch (ex: Exception) {
                val drawable = ResourcesCompat.getDrawable(resources, this.items!![selectedIndex].iconImageSource,context.theme)
                drawable?.setBounds((curXCenter - (fabSize/3).toFloat()).toInt(),
                    (curYCenter-(fabSize/3).toFloat()).toInt(),
                    (curXCenter + (fabSize/3).toFloat()).toInt(),
                    (curYCenter+(fabSize/3).toFloat()).toInt())
                drawable?.draw(canvas!!)
            }
        }



     //   canvas?.drawPath(path,navPaint)
        canvas?.drawPath(papp,navPaint)
    }
}



