package com.mapbox.search.detekt

import com.mapbox.search.detekt.rules.NoMockkVerifyImport
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class CustomRuleSetProvider : RuleSetProvider {

    override val ruleSetId: String = "custom-rules"

    override fun instance(config: Config) = RuleSet(
        ruleSetId,
        listOf(NoMockkVerifyImport(config))
    )
}
