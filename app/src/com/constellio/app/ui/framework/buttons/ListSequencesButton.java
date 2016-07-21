package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.pages.management.sequence.ListSequencesViewImpl;
import com.vaadin.ui.Component;

public class ListSequencesButton extends WindowButton {
	
	private String recordId;

	public ListSequencesButton(String recordId, String caption) {
		super(caption, caption);
		this.recordId = recordId;
	}

	@Override
	protected Component buildWindowContent() {
		return new ListSequencesViewImpl(recordId);
	}

}
