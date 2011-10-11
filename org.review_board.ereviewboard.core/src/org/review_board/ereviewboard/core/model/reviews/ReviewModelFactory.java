package org.review_board.ereviewboard.core.model.reviews;

import java.util.ArrayList;
import java.util.List;

import org.review_board.ereviewboard.core.ReviewboardDiffMapper;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.eclipse.mylyn.reviews.core.model.IFileItem;
import org.eclipse.mylyn.reviews.core.model.IFileRevision;
import org.eclipse.mylyn.reviews.internal.core.model.ReviewsFactory;

/**
 * Bridges between the <tt>eReviewBoard</tt> and the <tt>Mylyn Reviews</tt> model
 * 
 * @author Robert Munteanu
 *
 */
@SuppressWarnings("restriction")
public class ReviewModelFactory {
    
    private static final ReviewsFactory FACTORY = ReviewsFactory.eINSTANCE;

    public List<IFileItem> createFileItems(ReviewboardDiffMapper diffMapper) {
       
        List<FileDiff> fileDiffs = diffMapper.getFileDiffs();
        
        List<IFileItem> fileItems = new ArrayList<IFileItem>(fileDiffs.size());
        
        for ( FileDiff fileDiff : fileDiffs ) {
            
            IFileItem fileItem = FACTORY.createFileItem();
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
}
