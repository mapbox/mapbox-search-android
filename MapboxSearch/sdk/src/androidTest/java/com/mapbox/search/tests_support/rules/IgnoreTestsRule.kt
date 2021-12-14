package com.mapbox.search.tests_support.rules

import org.junit.Assume
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

internal class IgnoreTestsRule(private val ignoreAllTests: Boolean) : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return IgnorableStatement(base, ignoreAllTests)
    }

    private inner class IgnorableStatement(private val base: Statement, private val ignoreAllTests: Boolean) : Statement() {
        override fun evaluate() {
            Assume.assumeTrue("Test ignored", !ignoreAllTests)
            base.evaluate()
        }
    }
}
