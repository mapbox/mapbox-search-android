package com.mapbox.search.ui.utils

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler

internal class SearchSdkVerticalChangeHandler : AnimatorChangeHandler {

    /**
     * Background resource that will be applied to view during animation. Used for rendering optimization
     * purpose to reduce overdraw.
     */
    @DrawableRes
    val animationBackgroundRes: Int?

    constructor() : super() {
        animationBackgroundRes = null
    }
    constructor(@DrawableRes animationBackgroundRes: Int?) : super() {
        this.animationBackgroundRes = animationBackgroundRes
    }
    constructor(removesFromViewOnPush: Boolean, @DrawableRes animationBackgroundRes: Int? = null) : super(removesFromViewOnPush) {
        this.animationBackgroundRes = animationBackgroundRes
    }
    constructor(duration: Long, @DrawableRes animationBackgroundRes: Int? = null) : super(duration) {
        this.animationBackgroundRes = animationBackgroundRes
    }

    constructor(duration: Long, removesFromViewOnPush: Boolean, @DrawableRes animationBackgroundRes: Int? = null) : super(duration, removesFromViewOnPush) {
        this.animationBackgroundRes = animationBackgroundRes
    }

    @NonNull
    override fun getAnimator(
        @NonNull container: ViewGroup,
        @Nullable from: View?,
        @Nullable to: View?,
        isPush: Boolean,
        toAddedToContainer: Boolean
    ): Animator {
        val animator = AnimatorSet()
        val viewAnimators: MutableList<Animator> = ArrayList()
        if (isPush && to != null) {
            viewAnimators.add(ObjectAnimator.ofFloat(to, View.TRANSLATION_Y, to.height.toFloat(), 0f))
            animationBackgroundRes?.let {
                viewAnimators.add(ObjectAnimator.ofObject(BackgroundEvaluator(to, it), Unit))
            }
        } else if (!isPush && from != null) {
            viewAnimators.add(ObjectAnimator.ofFloat(from, View.TRANSLATION_Y, from.height.toFloat()))
            animationBackgroundRes?.let {
                viewAnimators.add(ObjectAnimator.ofObject(BackgroundEvaluator(from, it), Unit))
            }
        }
        animator.playTogether(viewAnimators)
        return animator
    }

    override fun resetFromView(@NonNull from: View) {}

    @NonNull
    override fun copy(): ControllerChangeHandler {
        return SearchSdkVerticalChangeHandler(animationDuration, removesFromViewOnPush(), animationBackgroundRes)
    }

    private class BackgroundEvaluator(
        private val view: View,
        @DrawableRes private val animationBackgroundRes: Int,
    ) : TypeEvaluator<Unit> {
        override fun evaluate(fraction: Float, startValue: Unit?, endValue: Unit?) {
            if (fraction < 1.0f) {
                view.setBackgroundResource(animationBackgroundRes)
            } else {
                view.setBackgroundResource(0)
            }
        }
    }
}
