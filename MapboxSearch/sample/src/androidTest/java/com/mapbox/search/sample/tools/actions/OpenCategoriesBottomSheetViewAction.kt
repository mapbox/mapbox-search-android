package com.mapbox.search.sample.tools.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.mapbox.search.CategorySearchOptions
import com.mapbox.search.ui.view.category.Category
import com.mapbox.search.ui.view.category.SearchCategoriesBottomSheetView
import org.hamcrest.Matcher

class OpenCategoriesBottomSheetViewAction(
    private val category: Category,
    private val searchOptions: CategorySearchOptions
) : ViewAction {

    override fun getDescription(): String {
        return "Open SearchCategoriesBottomSheetView"
    }

    override fun getConstraints(): Matcher<View> {
        return ViewMatchers.isAssignableFrom(SearchCategoriesBottomSheetView::class.java)
    }

    override fun perform(uiController: UiController?, view: View) {
        (view as SearchCategoriesBottomSheetView).open(category, searchOptions)
    }
}
