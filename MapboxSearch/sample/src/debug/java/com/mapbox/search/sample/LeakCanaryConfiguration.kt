package com.mapbox.search.sample

import leakcanary.LeakCanary
import shark.AndroidReferenceMatchers
import shark.IgnoredReferenceMatcher
import shark.ReferencePattern.InstanceFieldPattern
import shark.ReferencePattern.StaticFieldPattern

internal object LeakCanaryConfiguration {
    fun apply() {
        // Known android leaks that can be fixed only on client's side
        LeakCanary.config = LeakCanary.config.copy(
            referenceMatchers = AndroidReferenceMatchers.appDefaults +
                    IgnoredReferenceMatcher(
                        StaticFieldPattern(
                            "android.view.inputmethod.InputMethodManager",
                            "sInstance"
                        )
                    ) +
                    IgnoredReferenceMatcher(
                        InstanceFieldPattern(
                            "android.view.inputmethod.InputMethodManager$1",
                            "this$0"
                        )
                    ) +
                    IgnoredReferenceMatcher(
                        StaticFieldPattern(
                            "android.view.inputmethod.InputMethodManager",
                            "sInstanceMap"
                        )
                    ) +
                    IgnoredReferenceMatcher(
                        InstanceFieldPattern(
                            "android.app.Activity$1",
                            "this$0"
                        )
                    ) +
                    IgnoredReferenceMatcher(
                        InstanceFieldPattern(
                            "android.view.inputmethod.InputMethodManager\$ControlledInputConnectionWrapper",
                            "mParentInputMethodManager"
                        )
                    ) +
                    IgnoredReferenceMatcher(
                        InstanceFieldPattern(
                            "android.view.inputmethod.InputMethodManager\$ControlledInputConnectionWrapper",
                            "mInputConnection"
                        )
                    )
        )
    }
}
