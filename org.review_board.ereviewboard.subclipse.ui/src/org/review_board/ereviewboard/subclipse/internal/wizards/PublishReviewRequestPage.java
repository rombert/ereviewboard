/*******************************************************************************
 * Copyright (c) 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.subclipse.internal.wizards;

import java.util.*;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.*;
import org.eclipse.jface.layout.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.review_board.ereviewboard.core.model.*;
import org.review_board.ereviewboard.ui.util.KeyStrokeAutoCompleteField;

/**
 * @author Robert Munteanu
 *
 */
class PublishReviewRequestPage extends WizardPage {
    private static final String PRINTABLE_USER_NAME_SEP = " # ";
	private KeyStrokeAutoCompleteField _toUserComboAutoCompleteField;
    private KeyStrokeAutoCompleteField _toGroupComboAutoCompleteField;
    private final ReviewRequest reviewRequest = new ReviewRequest();
    
    private final CreateReviewRequestWizardContext _context;
	private final IProject _project;

    public PublishReviewRequestPage(CreateReviewRequestWizardContext context, IProject project) {
        super("Publish review request", "Publish review request", null);
        
        setMessage("Fill in the review request details. Description, summary and a target person or a target group are required.", IMessageProvider.INFORMATION);
        
        _project = project;
        _context = context;
        
        
    }

    public void createControl(Composite parent) {

        Composite layout = new Composite(parent, SWT.NONE);
        
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(layout);
        
        newLabel(layout, "Summary:");
        
        final Text summary = newText(layout);
        summary.addModifyListener(new ModifyListener() {
            
            public void modifyText(ModifyEvent e) {
            
               reviewRequest.setSummary(summary.getText());
               
               getContainer().updateButtons();
            }
        });

        newLabel(layout, "Bugs closed:");
        
        final Text bugsClosed = newText(layout);
        bugsClosed.addModifyListener(new ModifyListener() {
            
            public void modifyText(ModifyEvent e) {
                
                reviewRequest.setBugsClosed(Collections.singletonList(bugsClosed.getText()));
                
                getContainer().updateButtons();
            }
        });
        
        newLabel(layout, "Branch:");
        
        final Text branch = newText(layout);
        // we initialize the branch name if we can
		BranchInformationFinder branchFinder = BranchInformationFinderFactory.finderFor(_project);
		branch.setText(branchFinder.getBranchName());
		
        branch.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                reviewRequest.setBranch(branch.getText());
                getContainer().updateButtons();
            }
        });
        
        
        newLabel(layout, "Description:");
        
        final Text description = newMultilineText(layout);
        
        description.addModifyListener(new ModifyListener() {
            
            public void modifyText(ModifyEvent e) {
            
               reviewRequest.setDescription(description.getText());
               
               getContainer().updateButtons();
            }
        });

        newLabel(layout, "Testing done:");
        
        final Text testingDone = newMultilineText(layout);
        
        testingDone.addModifyListener(new ModifyListener() {
            
            public void modifyText(ModifyEvent e) {
            
                reviewRequest.setTestingDone(testingDone.getText());
                
                getContainer().updateButtons();
            }
        });
        
        newLabel(layout, "Target user:");
        
        final Text toUserText = newText(layout);
        
        _toUserComboAutoCompleteField = KeyStrokeAutoCompleteField.withCtrlSpace(toUserText, new TextContentAdapter(), new String[] {});
        
        toUserText.addModifyListener(new ModifyListener() {
            
            public void modifyText(ModifyEvent e) {
            
            	String userName = extractUsernameFromPrintableUserName(toUserText.getText());
                reviewRequest.setTargetPeople(Collections.singletonList(userName));
                
                getContainer().updateButtons();
            }
        });

        newLabel(layout, "Target group:");
        
        final Text toGroupText = newText(layout);
        
        _toGroupComboAutoCompleteField = KeyStrokeAutoCompleteField.withCtrlSpace(toGroupText, new TextContentAdapter(), new String[] {});
        
        toGroupText.addModifyListener(new ModifyListener() {
            
            public void modifyText(ModifyEvent e) {
                
                reviewRequest.setTargetGroups(Collections.singletonList(toGroupText.getText()));
                
                getContainer().updateButtons();
            }
        });
        
        setControl(layout);
    }

    private void newLabel(Composite layout, String text) {

        Label descriptionLabel = new Label(layout, SWT.NONE );
        descriptionLabel.setText(text);
        GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(descriptionLabel);
    }
    
    private Text newText(Composite layout) {

        final Text toUserText = new Text(layout, SWT.BORDER);
        GridDataFactory.swtDefaults().hint(PostReviewRequestWizard.TEXT_WIDTH, SWT.DEFAULT).applyTo(toUserText);
        return toUserText;
    }
    
    private Text newMultilineText(Composite layout) {

        final Text description = new Text(layout, SWT.MULTI| SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        GridDataFactory.swtDefaults().hint(PostReviewRequestWizard.TEXT_WIDTH, 60).applyTo(description);
        return description;
    }
    
    @Override
    public boolean isPageComplete() {
    
        return super.isPageComplete() && checkValid();
    }
    
    private boolean checkValid() {

        if (reviewRequest.getSummary() == null || reviewRequest.getSummary().length() == 0 ) {
            return false;
        }
        
        if ( reviewRequest.getDescription() == null || reviewRequest.getDescription().length() == 0 ) {
            return false;
        }
        
        if ( reviewRequest.getTargetGroups().isEmpty() && reviewRequest.getTargetPeople().isEmpty()) {
            return false;
        }
        
        return true;
            
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
            usernames.add(getPrintableUsername(user));

        return usernames.toArray(new String[usernames.size()]);
    }

	private String getPrintableUsername(User user) {
		String userFullName = user.getFullName();
		if (userFullName.trim().length() > 0) {
			return user.getUsername() + PRINTABLE_USER_NAME_SEP + userFullName;
		}
		return user.getUsername();
	}
	
	private String extractUsernameFromPrintableUserName(String printableName) {
		if (printableName == null) {
			return null;
		}
		
		int idxSep = printableName.indexOf(PRINTABLE_USER_NAME_SEP);
		if (idxSep == -1) {
			return printableName;
		}
		
		return printableName.substring(0, idxSep);
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
