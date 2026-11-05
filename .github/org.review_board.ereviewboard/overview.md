# Base feature

Packaging: `eclipse-feature`. The installable base feature groups the connector core and UI bundles plus required libraries. Its feature descriptor declares Mylyn task, review and commons feature requirements; it does not implement runtime behavior. Optional SCM features depend on this feature. The [update site](../org.review_board.ereviewboard.update/overview.md) distributes it.

See [core](../org.review_board.ereviewboard.core/overview.md) for API/task synchronization and [UI](../org.review_board.ereviewboard.ui/overview.md) for Mylyn registration and editor behavior. Consult [feature.xml](../../org.review_board.ereviewboard/feature.xml) for exact dependencies, included plugins and version constraints.
