# Change an SCM integration

1. Identify the owning [SCM core and UI modules](../architecture.md). Core locators fetch base revisions; UI plugin descriptors register those locators. Subclipse UI also owns SVN diff actions and wizards.
2. Check the [extension schemas and contract](../instructions/extensions.md), the affected bundle manifest, and the optional feature descriptor before editing.
3. Verify with [build and test](../workflows/build-and-test.md); for SVN diff changes include the [Subclipse test fragment](../org.review_board.ereviewboard.subclipse.tests/index.md). Exercise the compare editor in Eclipse with the optional feature installed when changing locator behavior.
