---
name: Release
about: This template should be used for release ticket.
labels: ''
assignees: ''
---

[Changelog](https://github.com/mapbox/mapbox-search-android/blob/develop/CHANGELOG.md)

### TODO list for making this happen
- [ ] Bump version name to release (without `-SNAPSHOT` postfix) in [CHANGELOG.md](https://github.com/mapbox/mapbox-search-android/blob/develop/CHANGELOG.md) and [gradle.properties](https://github.com/mapbox/mapbox-search-android/blob/develop/MapboxSearch/gradle.properties)
- [ ] Merge `develop` to `main`
- [ ] Create and tag release (this will trigger artifacts publishing to the SDK registry)
- [ ] Once release job completes on the Circle CI, go to [SDK registry repo](https://github.com/mapbox/api-downloads) and approve created by CI bot PR with updated configs.
- [ ] Create and tag release in [Search Demo App](https://github.com/mapbox/mapbox-search-android-examples), update code examples
- [ ] [Publish documentation](https://github.com/mapbox/mapbox-search-android#publishing-documentation)
- [ ] Update Search SDK in [1tap](https://github.com/mapbox/1tap-android)
- [ ] Bump version name to next `SNAPSHOT` in [CHANGELOG.md](https://github.com/mapbox/mapbox-search-android/blob/develop/CHANGELOG.md) and [gradle.properties](https://github.com/mapbox/mapbox-search-android/blob/develop/MapboxSearch/gradle.properties)
- [ ] Update [SDK Release Planning](https://docs.google.com/spreadsheets/d/1jCD7FRyp7YokFZ2R6i44E28BIzudRpqs86hwd4NkPE0/edit#gid=1201472177)
