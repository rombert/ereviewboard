# p2 update site

Packaging: `eclipse-update-site`. `site.xml` catalogs the base connector feature and optional Subclipse, EGit and CVS integration features in two categories. The module POM enables Tycho `archiveSite`; the build output is under `target/site/`. `associate-sites.xml` is copied during manual publication.

The [copy_to_pages.sh](../../org.review_board.ereviewboard.update/copy_to_pages.sh) script publishes to a sibling pages checkout and replaces its previous plugins/features; see [release workflow](../workflows/release.md) for cautions. This module owns distribution metadata, not runtime connector behavior.
