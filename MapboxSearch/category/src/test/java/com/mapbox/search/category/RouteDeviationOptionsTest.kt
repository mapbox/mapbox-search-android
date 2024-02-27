package com.mapbox.search.category

import com.mapbox.search.category.RouteDeviationOptions.Time
import com.mapbox.search.common.tests.ToStringVerifier
import nl.jqno.equalsverifier.EqualsVerifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

internal class RouteDeviationOptionsTest {

    @Test
    fun `equals() and hashCode() functions are correct`() {
        EqualsVerifier.forClass(RouteDeviationOptions.SarType::class.java)
            .verify()

        EqualsVerifier.forClass(Time::class.java)
            .verify()
    }

    @Test
    fun `toString() function is correct`() {
        ToStringVerifier(
            clazz = RouteDeviationOptions.SarType::class,
            includeAllProperties = false
        ).verify()

        ToStringVerifier(
            clazz = Time::class,
            includeAllProperties = false
        ).verify()
    }

    @Test
    fun `Check time deviation to minutes conversion`() {
        assertEquals(1.0, Time(60_000_000_000, TimeUnit.NANOSECONDS).timeDeviationMinutes)
        assertEquals(1.0, Time(60_000_000, TimeUnit.MICROSECONDS).timeDeviationMinutes)
        assertEquals(1.0, Time(60_000, TimeUnit.MILLISECONDS).timeDeviationMinutes)
        assertEquals(3.0, Time(180, TimeUnit.SECONDS).timeDeviationMinutes)
        assertEquals(15.0, Time(15, TimeUnit.MINUTES).timeDeviationMinutes)
        assertEquals(180.0, Time(3, TimeUnit.HOURS).timeDeviationMinutes)
        assertEquals(4320.0, Time(3, TimeUnit.DAYS).timeDeviationMinutes)
    }
}
