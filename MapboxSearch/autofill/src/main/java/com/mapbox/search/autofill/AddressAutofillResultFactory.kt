package com.mapbox.search.autofill

import com.mapbox.bindgen.Expected
import com.mapbox.bindgen.ExpectedFactory
import com.mapbox.search.base.result.BaseSearchResult
import com.mapbox.search.base.result.BaseSearchSuggestion

internal class AddressAutofillResultFactory {

    fun createAddressAutofillResultOrNull(result: BaseSearchResult): Expected<Exception, AddressAutofillResult> {
        return result.toAddressAutofillResult()?.let {
            ExpectedFactory.createValue(it)
        } ?: ExpectedFactory.createError(Exception("Unable to create AddressAutofillResult from $result"))
    }

    private fun BaseSearchResult.toAddressAutofillResult(): AddressAutofillResult? {
        val autofillAddress = AddressComponents.fromCoreSdkAddress(address, metadata) ?: return null
        return AddressAutofillResult(
            coordinate = this.coordinate,
            address = autofillAddress
        )
    }

    fun createAddressAutofillSuggestions(baseSuggestions: List<BaseSearchSuggestion>): List<AddressAutofillSuggestion> =
        baseSuggestions.mapNotNull { suggestion -> suggestion.toAddressAutofillSuggestion() }

    private fun BaseSearchSuggestion.toAddressAutofillSuggestion(): AddressAutofillSuggestion? {
        // Filtering incomplete results
        val autofillAddress = AddressComponents.fromCoreSdkAddress(address, metadata) ?: return null

        return AddressAutofillSuggestion(
            name = name,
            formattedAddress = fullAddress ?: autofillAddress.formattedAddress(),
            address = autofillAddress,
            underlying = this
        )
    }
}
