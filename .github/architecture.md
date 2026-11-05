# Architecture

eReviewBoard is an Eclipse/Mylyn connector for Review Board. The [root reactor](../pom.xml) builds OSGi plug-ins, feature groups, test fragments and a p2 update site with Tycho 0.21.0. [ereviewboard.target](../ereviewboard.target) pins Eclipse, Mylyn, SCM and Orbit units; its legacy HTTP repositories may require an environment able to access those archives. Runtime dependencies are defined by each bundle manifest, not by Maven POM dependency lists.

## Runtime path

1. The [connector UI](org.review_board.ereviewboard.ui/overview.md) registers the Mylyn repository connector, settings wizard, editor page, and diff actions in its plugin.xml.
2. The [connector core](org.review_board.ereviewboard.core/overview.md) creates/updates Mylyn task data from Review Board requests, reviews, comments, diffs and screenshots. Its client builds REST queries, performs HTTP requests, parses JSON, and maps results to domain objects and task attributes.
3. The UI diff editor uses the core `SCMFileContentsLocator` contract to locate base file contents for compare views. The UI's `scmFileContentsLocator` and `taskDiffActions` extension points allow optional SCM bundles to contribute behavior without hard dependencies from the connector to a specific SCM.
4. [Subclipse](org.review_board.ereviewboard.subclipse.core/overview.md), [EGit](org.review_board.ereviewboard.egit.core/overview.md), and [CVS](org.review_board.ereviewboard.cvs.core/overview.md) implement base-file locators. Their UI bundles register the locators; Subclipse UI also supplies create/update/apply diff actions and SVN diff generation.

## Module ownership

| Group                       | Bundle or feature overviews                                                                                                                                                                                                                                        |
| --------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Base distribution           | [feature](org.review_board.ereviewboard/overview.md), [core](org.review_board.ereviewboard.core/overview.md), [UI](org.review_board.ereviewboard.ui/overview.md)                                                                                                   |
| Subclipse integration       | [feature](org.review_board.ereviewboard.subclipse/overview.md), [core](org.review_board.ereviewboard.subclipse.core/overview.md), [UI](org.review_board.ereviewboard.subclipse.ui/overview.md), [tests](org.review_board.ereviewboard.subclipse.tests/overview.md) |
| EGit integration            | [feature](org.review_board.ereviewboard.egit/overview.md), [core](org.review_board.ereviewboard.egit.core/overview.md), [UI](org.review_board.ereviewboard.egit.ui/overview.md)                                                                                    |
| CVS integration             | [feature](org.review_board.ereviewboard.cvs/overview.md), [core](org.review_board.ereviewboard.cvs.core/overview.md), [UI](org.review_board.ereviewboard.cvs.ui/overview.md)                                                                                       |
| Verification and publishing | [connector tests](org.review_board.ereviewboard.tests/overview.md), [update site](org.review_board.ereviewboard.update/overview.md)                                                                                                                                |

The feature modules package the base connector and optional integrations; the update site publishes those features. Tests are OSGi fragments attached to the corresponding host bundles. For file-level entry points and source-of-truth descriptors, follow each [module index](index.md).
