package org.review_board.ereviewboard.ui.util;

import org.eclipse.jface.bindings.keys.*;
import org.eclipse.jface.fieldassist.*;
import org.eclipse.swt.widgets.Control;

/**
 * Extension of {@link AutoCompleteField} that handles a KeyStroke to open the proposal list of items. 
 * @author Matthieu BROUILLARD [matthieu.brouillard@agfa.com] - ID AGFA: AWXGX
 */
public class KeyStrokeAutoCompleteField {
    /**
     * Builds an AutoCompleteField with an activation KeyStoke binded to "Ctrl+Space"
     * @param control the control for which autocomplete is desired. May not be <code>null</code>.
     * @param controlContentAdapter the <code>IControlContentAdapter</code> used to obtain and  update the control's contents. May not be <code>null</code>.
     * @param proposals the array of Strings representing valid content proposals for the field.
     * @return the AutoCompleteField created
     */
    public static KeyStrokeAutoCompleteField withCtrlSpace(Control control, IControlContentAdapter controlContentAdapter, String[] proposals) {
        KeyStroke ctrlSpace = null;
        try {
            ctrlSpace = KeyStroke.getInstance("Ctrl+Space");
        } catch (ParseException e) {
            throw new IllegalStateException("a standard 'Ctrl+Space' key combination could not be created", e);
        }
        
        return new KeyStrokeAutoCompleteField(control, controlContentAdapter, proposals, ctrlSpace);
    }
    
    /**
     * Construct an AutoComplete field on the specified control, whose
     * completions are characterized by the specified array of Strings.
     * 
     * @param control the control for which autocomplete is desired. May not be <code>null</code>.
     * @param controlContentAdapter the <code>IControlContentAdapter</code> used to obtain and  update the control's contents. May not be <code>null</code>.
     * @param proposals the array of Strings representing valid content proposals for the field.
     */
    public KeyStrokeAutoCompleteField(Control control, IControlContentAdapter controlContentAdapter, String[] proposals, KeyStroke activationKeyStoke) {
        _proposalProvider = new SimpleContentProposalProvider(proposals);
        _proposalProvider.setFiltering(true);
        _adapter = new ContentProposalAdapter(control, controlContentAdapter, _proposalProvider, activationKeyStoke, ASCII_CHARS);
        _adapter.setPropagateKeys(true);
        _adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
    }

    /**
     * Set the Strings to be used as content proposals.
     * 
     * @param proposals
     *            the array of Strings to be used as proposals.
     */
    public void setProposals(String[] proposals) {
        _proposalProvider.setProposals(proposals);
    }
    
    private static char[] createASCIITableAsCharArray() {
        int start = 32;
        int stop = 255;
        int length = stop - start + 1;
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (char) (i + start);
        }
        return chars;
    }


    private SimpleContentProposalProvider _proposalProvider;
    private ContentProposalAdapter _adapter;
    private static final char[] ASCII_CHARS  = createASCIITableAsCharArray();
}
