package org.review_board.ereviewboard.subclipse.internal.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.User;

/**
 * @author Robert Munteanu
 *
 */
class PublishReviewRequestPage extends WizardPage {

    private AutoCompleteField _toUserComboAutoCompleteField;
    private AutoCompleteField _toGroupComboAutoCompleteField;
    
    private final ReviewRequest reviewRequest = new ReviewRequest();
    
    private final CreateReviewRequestWizardContext _context;

    public PublishReviewRequestPage(CreateReviewRequestWizardContext context) {

        super("Publish review request");
        
        _context = context;
    }

    @Override
    public void createControl(Composite parent) {

        Composite layout = new Composite(parent, SWT.NONE);
        
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(layout);
        
        Label summaryLabel = new Label(layout, SWT.NONE);
        summaryLabel.setText("Summary");
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(summaryLabel);
        
        final Text summary = new Text(layout, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.swtDefaults().hint(300, SWT.DEFAULT).applyTo(summary);
        summary.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
            
               reviewRequest.setSummary(summary.getText());
            }
        });

        Label bugsClosedLabel = new Label(layout, SWT.NONE);
        bugsClosedLabel.setText("Bugs closed");
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(bugsClosedLabel);
        
        final Text bugsClosed = new Text(layout, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.swtDefaults().hint(300, SWT.DEFAULT).applyTo(bugsClosed);
        bugsClosed.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
                
                reviewRequest.setBugsClosed(Collections.singletonList(bugsClosed.getText()));
            }
        });
        
        Label branchLabel = new Label(layout, SWT.NONE);
        branchLabel.setText("Branch");
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(branchLabel);
        
        final Text branch = new Text(layout, SWT.SINGLE | SWT.BORDER);
        GridDataFactory.swtDefaults().hint(300, SWT.DEFAULT).applyTo(branch);
        branch.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
                
                reviewRequest.setBranch(branch.getText());
            }
        });
        
        Label descriptionLabel = new Label(layout, SWT.NONE );
        descriptionLabel.setText("Description");
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(descriptionLabel);
        
        final Text description = new Text(layout, SWT.MULTI| SWT.BORDER);
        GridDataFactory.swtDefaults().hint(300, 50).applyTo(description);
        
        description.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
            
               reviewRequest.setDescription(description.getText());
            }
        });

        Label testingDoneLabel = new Label(layout, SWT.NONE);
        testingDoneLabel.setText("Testing done");
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(testingDoneLabel);
        
        final Text testingDone = new Text(layout, SWT.MULTI| SWT.BORDER);
        GridDataFactory.swtDefaults().hint(300, 50).applyTo(testingDone);
        
        testingDone.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
            
                reviewRequest.setTestingDone(testingDone.getText());
            }
        });
        
        Label toUserLabel = new Label(layout, SWT.NONE);
        toUserLabel.setText("To user");
        
        final Text toUserText = new Text(layout, SWT.BORDER);
        GridDataFactory.swtDefaults().hint(300, SWT.DEFAULT).applyTo(toUserText);
        
        _toUserComboAutoCompleteField = new AutoCompleteField(toUserText, new TextContentAdapter(), new String[] {});
        
        toUserText.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
            
                reviewRequest.setTargetPeople(Collections.singletonList(toUserText.getText()));
            }
        });

        Label toGroupLabel = new Label(layout, SWT.NONE);
        toGroupLabel.setText("To group");
        
        final Text toGroupText = new Text(layout, SWT.BORDER);
        GridDataFactory.swtDefaults().hint(300, SWT.DEFAULT).applyTo(toGroupText);
        
        _toGroupComboAutoCompleteField = new AutoCompleteField(toGroupText, new TextContentAdapter(), new String[] {});
        
        toGroupText.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
                
                reviewRequest.setTargetGroups(Collections.singletonList(toGroupText.getText()));
            }
        });
        
        setControl(layout);
    }
    
    @Override
    public void setVisible(boolean visible) {
    
        if ( visible) {
            _toUserComboAutoCompleteField.setProposals(getUsernames());
            _toGroupComboAutoCompleteField.setProposals(getGroupNames());
        }
        
        super.setVisible(visible);
    }
    
    private String[] getUsernames() {

        List<String> usernames = new ArrayList<String>();
        for ( User user : _context.getReviewboardClient().getClientData().getUsers() )
            usernames.add(user.getUsername());

        return usernames.toArray(new String[usernames.size()]);
    }
    
    private String[] getGroupNames() {

        List<String> groupNames = new ArrayList<String>();
        for ( ReviewGroup group : _context.getReviewboardClient().getClientData().getGroups() )
            groupNames.add(group.getName());

        return groupNames.toArray(new String[groupNames.size()]);
    }

    public ReviewRequest getReviewRequest() {

        return reviewRequest;
    }
}
