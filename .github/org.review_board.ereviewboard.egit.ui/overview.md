# EGit UI

Packaging: `eclipse-plugin`. `plugin.xml` registers the EGit core locator under the connector UI `scmFileContentsLocator` extension point. This allows Review Board file diffs to use a Git base revision in Eclipse compare. The bundle requires connector core/UI and EGit core, and is delivered in the [EGit feature](../org.review_board.ereviewboard.egit/overview.md).

Unlike Subclipse UI, this module has no checked-in Java wizard or diff action implementation; registration and bundle activation are its principal responsibilities. Inspect [plugin.xml](../../org.review_board.ereviewboard.egit.ui/plugin.xml) and its manifest for the current contract.
