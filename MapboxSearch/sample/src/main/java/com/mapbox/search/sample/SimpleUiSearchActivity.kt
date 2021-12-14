package com.mapbox.search.sample

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.mapbox.search.ResponseInfo
import com.mapbox.search.record.HistoryRecord
import com.mapbox.search.result.SearchResult
import com.mapbox.search.result.SearchSuggestion
import com.mapbox.search.ui.view.CommonSearchViewConfiguration
import com.mapbox.search.ui.view.DistanceUnitType
import com.mapbox.search.ui.view.SearchResultsView

class SimpleUiSearchActivity : AppCompatActivity() {

    private lateinit var searchResultsView: SearchResultsView
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_ui)

        searchResultsView = findViewById<SearchResultsView>(R.id.search_results_view).apply {
            initialize(CommonSearchViewConfiguration(DistanceUnitType.IMPERIAL))
            isVisible = false
        }

        searchResultsView.addSearchListener(object : SearchResultsView.SearchListener {
            override fun onSearchResult(searchResult: SearchResult, responseInfo: ResponseInfo) {
                Toast.makeText(applicationContext, "Search result: $searchResult", Toast.LENGTH_SHORT).show()
            }

            override fun onHistoryItemClicked(historyRecord: HistoryRecord) {
                if (::searchView.isInitialized) {
                    searchView.setQuery(historyRecord.name, true)
                }
            }

            override fun onPopulateQueryClicked(suggestion: SearchSuggestion, responseInfo: ResponseInfo) {
                if (::searchView.isInitialized) {
                    searchView.setQuery(suggestion.name, true)
                }
            }

            override fun onFeedbackClicked(responseInfo: ResponseInfo) {
                // Not implemented
            }
        })

        findViewById<Toolbar>(R.id.toolbar).apply {
            title = getString(R.string.simple_ui_toolbar_title)
            setSupportActionBar(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.simple_ui_activity_options_menu, menu)

        val searchActionView = menu.findItem(R.id.action_search)
        searchActionView.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchResultsView.isVisible = true
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchResultsView.isVisible = false
                return true
            }
        })

        searchView = searchActionView.actionView as SearchView
        searchView.queryHint = getString(R.string.query_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                searchResultsView.search(newText)
                return false
            }
        })
        return true
    }
}
