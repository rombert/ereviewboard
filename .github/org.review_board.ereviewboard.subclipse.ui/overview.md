# Subclipse UI

Packaging: `eclipse-plugin`. `plugin.xml` contributes an SVN-project context action to create a review request, diff actions to update/apply a review request, and the Subclipse base-file locator. `internal/wizards/` selects local SVN changes, constructs diffs, and handles creation/update flows; `DiffCreator` rewrites SVN diff paths and handles copied/moved files for Review Board compatibility. `internal/actions/` coordinates the commands and SVN-to-Review Board repository mapping.

This bundle depends on connector core/UI and Subclipse core; it is installed through the [Subclipse feature](../org.review_board.ereviewboard.subclipse/overview.md). The [Subclipse test fragment](../org.review_board.ereviewboard.subclipse.tests/overview.md) tests `DiffCreator`. Check [extension instructions](../instructions/extensions.md) before changing action registration.
