# Agent entry point

This repository is an Eclipse/Mylyn Review Board connector built as a Maven/Tycho reactor. Start at the [knowledgebase index](.github/index.md) for module navigation and the [architecture](.github/architecture.md) for cross-module behavior. Follow the [repository instructions](.github/copilot-instructions.md) when changing code.

The root [POM](pom.xml) is the source of truth for reactor membership and build configuration. The [target platform](ereviewboard.target) defines the Eclipse dependencies; each module's POM, manifest, and extension descriptors define its own build and runtime contracts. If documentation disagrees with those files, verify the implementation and update the knowledgebase rather than copying the stale claim.
