# CVS core

Packaging: `eclipse-plugin`. `CVSSCMFileContentsLocator` retrieves base-file contents using Eclipse Team CVS APIs and implements the core locator contract. The [CVS UI](../org.review_board.ereviewboard.cvs.ui/overview.md) registers this class through the connector UI extension point. The bundle requires the base connector and Eclipse Team/CVS bundles and exports internal packages to its UI friend; use its manifest for exact constraints.

Keep CVS APIs out of the base connector. See [extension instructions](../instructions/extensions.md) for the contract.
