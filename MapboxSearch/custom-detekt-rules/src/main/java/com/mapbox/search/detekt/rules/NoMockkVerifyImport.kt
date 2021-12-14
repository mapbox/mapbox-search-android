package com.mapbox.search.detekt.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtImportDirective

class NoMockkVerifyImport(config: Config = Config.empty) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName, Severity.Style,
        "Don't import $FORBIDDEN_IMPORT",
        Debt.FIVE_MINS
    )

    override fun visitImportDirective(importDirective: KtImportDirective) {
        val import = importDirective.importPath?.pathStr

        if (import?.contains(FORBIDDEN_IMPORT) == true) {
            report(
                CodeSmell(
                    issue, Entity.from(importDirective),
                    "Don't import '$FORBIDDEN_IMPORT', use Verify from Test DSL"
                )
            )
        }
    }

    private companion object {
        const val FORBIDDEN_IMPORT = "io.mockk.verify"
    }
}
