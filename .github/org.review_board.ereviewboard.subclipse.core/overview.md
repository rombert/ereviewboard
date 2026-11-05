# Subclipse core

Packaging: `eclipse-plugin`. `SubclipseSCMFileContentsLocator` implements the connector's base-file locator using Subclipse/SVN APIs. It keeps SVN-specific retrieval out of the base connector; the [Subclipse UI](../org.review_board.ereviewboard.subclipse.ui/overview.md) registers the class through the UI locator extension point. The bundle exports internal packages to its UI friend and requires Subclipse core and the connector core. Its manifest also lists a self-requirement; consult the manifest before changing OSGi dependencies rather than assuming it is intentional.

This module does not own SVN diff generation or review request wizards; those live in Subclipse UI. See the [SCM extension contract](../instructions/extensions.md).
