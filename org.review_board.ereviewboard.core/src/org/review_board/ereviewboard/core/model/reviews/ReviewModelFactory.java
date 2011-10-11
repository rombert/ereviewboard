package org.review_board.ereviewboard.core.model.reviews;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.reviews.core.model.*;
import org.eclipse.mylyn.reviews.internal.core.model.ReviewsFactory;
import org.review_board.ereviewboard.core.ReviewboardDiffMapper;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.model.DiffComment;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.User;

/**
 * Bridges between the <tt>eReviewBoard</tt> and the <tt>Mylyn Reviews</tt> model
 * 
 * @author Robert Munteanu
 *
 */
@SuppressWarnings("restriction")
public class ReviewModelFactory {
    
    private static final ReviewsFactory FACTORY = ReviewsFactory.eINSTANCE;
    private final ReviewboardClient _client;
    
    public ReviewModelFactory(ReviewboardClient client) {
        
        _client = client;
    }

    public List<IFileItem> createFileItems(String submitter, ReviewboardDiffMapper diffMapper) {
       
        List<FileDiff> fileDiffs = diffMapper.getFileDiffs();
        
        List<IFileItem> fileItems = new ArrayList<IFileItem>(fileDiffs.size());
        
        for ( FileDiff fileDiff : fileDiffs ) {
            
            IFileItem fileItem = FACTORY.createFileItem();
            fileItem.setAddedBy(createUser(submitter));
            fileItem.setName(fileDiff.getDestinationFile());
            fileItem.setId(String.valueOf(fileDiff.getId()));
            
            IFileRevision from = FACTORY.createFileRevision();
            from.setPath(fileDiff.getSourceFile());
            from.setRevision(fileDiff.getSourceRevision());
            
            IFileRevision to = FACTORY.createFileRevision();
            to.setPath(fileDiff.getDestinationFile());
            to.setRevision(fileDiff.getDestinationDetail());
            
            fileItem.setBase(from);
            fileItem.setTarget(to);
            
            fileItems.add(fileItem);
        }
        
        return fileItems;
    }
    
    public void appendComments(IFileRevision fileRevision, List<DiffComment> diffComments) {
        
        for ( DiffComment diffComment : diffComments ) {
            
            ILineRange line = FACTORY.createLineRange();
            line.setStart(diffComment.getFirstLine());
            line.setEnd(diffComment.getFirstLine() + diffComment.getNumLines());
            
            ILineLocation location = FACTORY.createLineLocation();
            location.getRanges().add(line);
            
            IUser author = createUser(diffComment.getUsername());
            
            IComment topicComment = FACTORY.createComment();
            topicComment.setAuthor(author);
            topicComment.setCreationDate(diffComment.getTimestamp());
            topicComment.setDescription(diffComment.getText());
            
            ITopic topic = FACTORY.createTopic();
            topic.setId(String.valueOf(diffComment.getId()));
            topic.setAuthor(author);
            topic.setCreationDate(diffComment.getTimestamp());
            topic.setLocation(location);
            topic.setItem(fileRevision);
            topic.setDraft(false);
            topic.setDescription(diffComment.getText());
            topic.getComments().add(topicComment);
        }
    }

    public IUser createUser(String username) {
        IUser author = FACTORY.createUser();
        
        User user = _client.getClientData().getUser(username);
        author.setId(username);
        if ( user != null && user.getFullName().length() > 0 )
            author.setDisplayName(user.getFullName());
        else
            author.setDisplayName(username);
        return author;
    }
}
