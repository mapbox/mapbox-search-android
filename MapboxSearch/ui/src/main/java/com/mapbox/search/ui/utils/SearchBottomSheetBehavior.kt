package com.mapbox.search.ui.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.concurrent.CopyOnWriteArrayList

internal class SearchBottomSheetBehavior<V : View> : BottomSheetBehavior<V> {

    var draggingAllowed: Boolean = true

    private var currentStateChangedFromUser = false

    private val onStateChangedListeners = CopyOnWriteArrayList<OnStateChangedListener>()

    constructor() : super()
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        addBottomSheetCallback(object : BottomSheetBehaviorCallbackAdapter() {
            override fun onStateChanged(bottomSheet: View, @BottomSheetBehavior.State newState: Int) {
                currentStateChangedFromUser = when (newState) {
                    STATE_DRAGGING -> true
                    STATE_HIDDEN, STATE_COLLAPSED, STATE_HALF_EXPANDED, STATE_EXPANDED -> false
                    STATE_SETTLING -> {
                        // don't change
                        currentStateChangedFromUser
                    }
                    else -> {
                        // shouldn't be called
                        currentStateChangedFromUser
                    }
                }
                notifyListeners(newState, currentStateChangedFromUser)
            }
        })
    }

    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: V,
        event: MotionEvent
    ): Boolean {
        return draggingAllowed && super.onInterceptTouchEvent(parent, child, event)
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        return draggingAllowed && super.onTouchEvent(parent, child, event)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return draggingAllowed && super.onStartNestedScroll(
            coordinatorLayout,
            child,
            directTargetChild,
            target,
            axes,
            type
        )
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        if (draggingAllowed) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        }
    }

    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        type: Int
    ) {
        if (draggingAllowed) {
            super.onStopNestedScroll(coordinatorLayout, child, target, type)
        }
    }

    override fun onNestedPreFling(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return if (draggingAllowed) {
            super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY)
        } else {
            false
        }
    }

    private fun notifyListeners(@BottomSheetBehavior.State state: Int, fromUser: Boolean) {
        onStateChangedListeners.forEach { it.onStateChanged(state, fromUser) }
    }

    fun addOnStateChangedListener(listener: OnStateChangedListener) {
        onStateChangedListeners.add(listener)
    }

    fun removeOnStateChangedListener(listener: OnStateChangedListener) {
        onStateChangedListeners.remove(listener)
    }

    fun interface OnStateChangedListener {
        fun onStateChanged(@BottomSheetBehavior.State newState: Int, fromUser: Boolean)
    }
}
