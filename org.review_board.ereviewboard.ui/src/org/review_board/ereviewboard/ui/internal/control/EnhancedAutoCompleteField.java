package org.review_board.ereviewboard.ui.internal.control;

/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Robert Munteanu - adapted for ReviewBoard usage
 *******************************************************************************/

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Text;
import org.review_board.ereviewboard.ui.util.UiUtils;

/**
 * The <tt>LabeledAutoCompleteField</tt> is an enhanced {@link AutoCompleteField}
 * 
 * <p>This class brings two enhancements: activation of completion on M1+Space, and
 * display of enhanced labels for proposals.</p>
 */
public class EnhancedAutoCompleteField {
    
    private static final char[] COMPLETION_CHARS = createCompletionChars();

    private static char[] createCompletionChars() {

        int start = '0';
        int stop = 'z';
        char[] chars = new char[stop - start + 1];
        for (int i = 0; i < chars.length; i++)
            chars[i] = (char) (i + start);

        return chars;
    }

    
    private final SimpleContentProposalProvider proposalProvider;
    private final ContentProposalAdapter adapter;

    public EnhancedAutoCompleteField(Text control, Proposal[] proposals) {
        
        KeyStroke activationKeyStroke;
        try {
            activationKeyStroke = KeyStroke.getInstance(IKeyLookup.M1_NAME + "+" + IKeyLookup.SPACE_NAME);
        } catch ( ParseException e ) {
            activationKeyStroke = null;
        }
        
        proposalProvider = new SimpleContentProposalProvider(proposals);
        adapter = new ContentProposalAdapter(control, new TextContentAdapter(),
                proposalProvider, activationKeyStroke, COMPLETION_CHARS);
        adapter.setPropagateKeys(true);
        adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        
        UiUtils.installContentAssistControlDecoration(control);
    }
    

    public void setProposals(List<Proposal> proposals) {
        
        proposalProvider.setProposals(proposals);
    }
    
    /**
     * SimpleContentProposalProvider is a class designed to map a static list of
     * Strings to content proposals.
     * 
     * @see IContentProposalProvider
     * @since 3.2
     * 
     */
    static class SimpleContentProposalProvider implements IContentProposalProvider {

        /*
         * The proposals provided.
         */
        private Proposal[] proposals;

        /**
         * Construct a SimpleContentProposalProvider whose content proposals are
         * always the specified array of Objects.
         * 
         * @param proposals
         *            the array of Strings to be returned whenever proposals are
         *            requested.
         */
        public SimpleContentProposalProvider(Proposal[] proposals) {
            this.proposals = proposals;
        }

        public void setProposals(List<Proposal> proposals) {
            this.proposals = proposals.toArray(new Proposal[proposals.size()]);
        }

        /**
         * Return an array of Objects representing the valid content proposals for a
         * field. 
         * 
         * @param contents
         *            the current contents of the field (only consulted if filtering
         *            is set to <code>true</code>)
         * @param position
         *            the current cursor position within the field (ignored)
         * @return the array of Objects that represent valid proposals for the field
         *         given its current content.
         */
        public IContentProposal[] getProposals(String contents, int position) {
            List<IContentProposal> list = new ArrayList<IContentProposal>();
            for ( Proposal proposal : proposals ) {
                if (proposal.getValue().length() >= contents.length()
                        && proposal.getValue().substring(0, contents.length())
                                .equalsIgnoreCase(contents)) {
                    list.add(makeContentProposal(proposal));
                }
            }
            return list.toArray(new IContentProposal[list .size()]);
        }

        private IContentProposal makeContentProposal(final Proposal proposal) {
            return new IContentProposal() {
                public String getContent() {
                    return proposal.getValue();
                }

                public String getDescription() {
                    return null;
                }

                public String getLabel() {
                    
                    if ( proposal.getLabel() == null || proposal.getLabel().length() == 0 )
                        return getContent();
                    
                    return proposal.getValue() + " [" + proposal.getLabel()+"]";
                }

                public int getCursorPosition() {
                    return proposal.getLabel().length();
                }
            };
        }
    }
}
