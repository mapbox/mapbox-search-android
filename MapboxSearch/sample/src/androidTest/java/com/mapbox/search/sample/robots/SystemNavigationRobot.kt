package com.mapbox.search.sample.robots

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.mapbox.search.sample.Constants.DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS

fun systemNavigation(block: SystemNavigationRobot.() -> Unit) {
    SystemNavigationRobot().apply { block() }
}

@RobotDsl
class SystemNavigationRobot {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)

    fun back() {
        device.pressBack()
        Thread.sleep(DOUBLE_ANDROID_ANIMATION_DELAY_MILLIS)
        instrumentation.waitForIdleSync()
    }
}
