# Trace a review request

1. Start at the [Mylyn registration](../../org.review_board.ereviewboard.ui/plugin.xml), then follow `ReviewboardRepositoryConnector.getTaskData` in [core](../org.review_board.ereviewboard.core/index.md).
2. Follow `ReviewboardClientManager` into `RestfulReviewboardClient`, `ReviewboardHttpClient`, `ReviewboardQueryBuilder` and `RestfulReviewboardReader` for HTTP requests and JSON parsing. The domain objects live in `core/model/`; attribute, comment and diff mappers turn them into Mylyn task data.
3. Follow `ReviewRequestEditorPage` and `ReviewboardDiffPart` in the [UI](../org.review_board.ereviewboard.ui/index.md) for rendering and compare behavior. Check [core tests](../org.review_board.ereviewboard.tests/index.md) for query, parsing and mapping examples.

For remote API changes, inspect both parsing and task-data mapping; a correct response parser alone does not guarantee that the editor can display the new attribute.
