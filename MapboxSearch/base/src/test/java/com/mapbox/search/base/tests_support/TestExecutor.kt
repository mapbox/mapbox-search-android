package com.mapbox.search.base.tests_support

import java.util.concurrent.Executor

internal class TestExecutor : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }
}
