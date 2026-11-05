# EGit core

Packaging: `eclipse-plugin`. `EGitSCMFileContentsLocator` implements the connector's base-file content locator using EGit/JGit. The base connector does not reference EGit directly. The [EGit UI](../org.review_board.ereviewboard.egit.ui/overview.md) registers this implementation with the connector UI extension point. The core bundle depends on connector core plus EGit/JGit and exports internal packages to its UI friend; the manifest is authoritative for versions and exports.

See [extension instructions](../instructions/extensions.md) before modifying locator behavior or wiring.
