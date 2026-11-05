# Versioning and publication

The [root POM](../../pom.xml) owns the Maven version and Tycho plugin version; module POMs inherit the parent version. The [target platform](../../ereviewboard.target) pins upstream dependencies. The [update-site module](../org.review_board.ereviewboard.update/overview.md) owns the p2 feature catalog and archived site output.

The checked-in [set-version.sh](../../set-version.sh) accepts one version argument and calls `tycho-versions-plugin:0.21.0:set-version` with `-Dtycho.mode=maven`; it then normalizes manifest line endings with `dos2unix` and edits `site.xml`. Inspect the resulting diff, especially feature versions and update-site URLs, before releasing. Do not assume the script alone publishes artifacts.

Build with `mvn verify` after version changes. The update module enables `archiveSite` in its POM. Its [copy_to_pages.sh](../../org.review_board.ereviewboard.update/copy_to_pages.sh) expects a sibling `../../ereviewboard-pages` checkout, copies `target/site/` to its `update/` directory, copies `associate-sites.xml`, and stages changes there; it removes prior plugin and feature files in that other checkout. Run it only when intentionally publishing and after inspecting paths. Signing is conditional on the `jarsigner.alias` property in the root POM; never store credentials in the repository.
