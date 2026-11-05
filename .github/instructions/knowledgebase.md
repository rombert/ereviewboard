# Knowledgebase maintenance

The [root agent entry](../../AGENTS.md) routes readers; the [root index](../index.md) mirrors reactor membership from `pom.xml`; [architecture](../architecture.md) owns cross-module runtime relationships. Every reactor module has an `overview.md` for responsibility and integration boundaries, and an `index.md` for links to its authoritative source and descriptors. Do not copy entire class lists, manifests, or build configurations into docs.

When a module is added or removed, update the root index and architecture table along with that module's pages. When behavior changes, update its overview and the most specific workflow or instruction only if needed. Prefer relative Markdown links to checked-in sources. Validate that all 15 current reactor modules have both pages and all local links resolve. Treat compiled `bin/`, Maven `target/`, and checked-out external sites as generated or external, not KB sources.
