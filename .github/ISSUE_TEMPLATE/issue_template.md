---
name: Bug
about: This template should be used for reporting bugs and defects.
labels: 'bug'
assignees: ''
---

## Environment

- Search SDK Component (check at least one)
    - [ ] Core Search SDK (`SearchEngine` class, and etc.)
    - [ ] Offline Search SDK (`OfflineSearchEngine` class, and etc.)
    - [ ] Address Autofill SDK
    - [ ] Search UI SDK
- Android OS version:
- Devices affected:
- Search SDK Version:
- Included in project Maps SDK, Nav SDK versions if available:

## Code examples
<!--- 

Include code with 
  - SDK initialization
  - Search requests
  - Search options (proximity point, countries, languages, etc.)
  - Code snippets that cause crash
  - etc. 

Make sure you don't expose secret tokens and other sensitive information.

For example:

val searchEngine = SearchEngine.createSearchEngineWithBuiltInDataProviders(
  apiType = ApiType.GEOCODING,
  settings = SearchEngineSettings(myToken)
)

val searchRequestTask = searchEngine.search(
  "Paris Eiffel Tower",
  SearchOptions(limit = 5),
  searchCallback
)

or 

val addressAutofill = AddressAutofill.create(
  accessToken = myToken,
)

val response = addressAutofill.suggestions(
  query = query,
  options = AddressAutofillOptions(countries = listOf(AddressAutofillOptions.Country("us")))
)
--->

## Observed behavior and steps

<!--- Please include as much evidence as possible (traces, videos, screenshots etc） --->

## Expected behavior

<!--- Please include the expected behavior and any resources supporting this expected behavior. --->
