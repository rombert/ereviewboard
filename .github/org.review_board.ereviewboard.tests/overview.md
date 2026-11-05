# Connector test fragment

Packaging: `eclipse-test-plugin`; OSGi fragment host: `org.review_board.ereviewboard.core`. JUnit tests under `src/` cover core client pagination, queries, REST reader/client data, repository/model behavior, utility methods, attribute mapping and comment mapping. The module includes `testdata/` fixtures. Run it in the Tycho/Eclipse test runtime; it is not a standalone application.

Tests track the [connector core](../org.review_board.ereviewboard.core/overview.md). For exact host and test dependencies consult its manifest; for suite discovery and execution see [build and test](../workflows/build-and-test.md).
