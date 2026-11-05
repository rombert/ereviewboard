# Java and Eclipse style

The checked-in manifests declare `J2SE-1.5`; use Java 5-compatible language constructs, generics and explicit types as the surrounding source does. Preserve existing naming, indentation, formatting and license headers in touched files. No modernization or bulk reformatting is implied by documentation changes.

Mylyn task attributes are mapped in the [core connector and mappers](../org.review_board.ereviewboard.core/index.md), network failures are represented by core exception types, and Eclipse-bound code reports through plug-in logging/status APIs. Keep UI dependencies in UI bundles and SCM-specific APIs in their optional integrations. When changing a bundle API or runtime dependency, review its `MANIFEST.MF`, exported packages and any applicable extension schema; when changing feature contents, review the feature descriptor and update site. See [extension contracts](../instructions/extensions.md) for contribution rules.
