# Connector UI

Packaging: `eclipse-plugin`. `plugin.xml` registers the core/UI Mylyn repository connectors, repository migrator, editor page factory, and the built-in publish/discard review actions. Repository/query/new task wizards live under `ui/wizard/`; the editor page, diff section, compare input and review behavior live under `ui/editor/`. `internal/actions/` handles review publishing and draft removal.

The UI declares `taskDiffActions` and `scmFileContentsLocator` extension points. `ReviewboardDiffPart` discovers actions and locators, and opens an Eclipse compare editor using base contents supplied by an installed SCM integration. Subclipse, EGit and CVS UI bundles contribute locators; Subclipse also contributes actions. See [extension contracts](../instructions/extensions.md) before modifying plug-in wiring. UI depends on connector core and Mylyn UI/reviews bundles; exact dependencies and exported/friend packages are in its manifest.
