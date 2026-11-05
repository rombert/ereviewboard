# Subclipse test fragment

Packaging: `eclipse-test-plugin`; OSGi fragment host: `org.review_board.ereviewboard.subclipse.ui`. `DiffCreatorTest` exercises SVN-compatible patch generation in the Subclipse UI code. The fragment requires JUnit, Subclipse client adapter, SVNKit and Hamcrest bundles. Run with the matching Tycho target platform, not as an isolated jar.

See [Subclipse UI](../org.review_board.ereviewboard.subclipse.ui/overview.md) for the code under test and [build and test](../workflows/build-and-test.md) for commands.
