package com.mapbox.search.common

/**
 * This annotation marks the Search SDK API that is available for selected customers only.
 * By default, API annotated with `@RestrictedMapboxSearchAPI` returns null or empty/stub data.
 * Contact our team, if you're interested in restricted API usage.
 *
 * Any usage of a declaration annotated with `@RestrictedMapboxSearchAPI` must be accepted
 * either by annotating that usage with the [OptIn] annotation,
 * e.g. `@OptIn(RestrictedMapboxSearchAPI::class)`, or by using the compiler argument
 * `-Xopt-in=com.mapbox.search.common.RestrictedMapboxSearchAPI`.
 */
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
public annotation class RestrictedMapboxSearchAPI
