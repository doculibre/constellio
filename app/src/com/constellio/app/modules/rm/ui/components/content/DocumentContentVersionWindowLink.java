package com.constellio.app.modules.rm.ui.components.content;

import static com.constellio.app.ui.i18n.i18n.$;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

public class DocumentContentVersionWindowLink extends WindowButton {
	
	private RecordVO recordVO;
	
	private ContentVersionVO contentVersionVO;
	
	private VerticalLayout windowLayout;

	public DocumentContentVersionWindowLink(RecordVO recordVO, ContentVersionVO contentVersionVO, String caption) {
		super(caption, $("DocumentContentVersionWindow.windowTitle"));
		addStyleName(ValoTheme.BUTTON_LINK);
		
		this.recordVO = recordVO;
		this.contentVersionVO = contentVersionVO;
	}

	@Override
	protected Component buildWindowContent() {
		windowLayout = new DocumentContentVersionWindowImpl(recordVO, contentVersionVO);
		return windowLayout;
	}

}
