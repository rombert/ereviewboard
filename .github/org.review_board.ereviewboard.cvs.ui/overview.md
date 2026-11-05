# CVS UI

Packaging: `eclipse-plugin`. `plugin.xml` contributes the CVS core locator to the connector UI `scmFileContentsLocator` extension point, enabling compare views to resolve a CVS base file. This UI bundle requires the connector core/UI and CVS core bundles and is included in the [CVS feature](../org.review_board.ereviewboard.cvs/overview.md).

There are no checked-in CVS-specific wizard or diff-action Java classes in this module; registration and bundle activation are its principal responsibilities. The manifest and plugin descriptor define the current wiring.
