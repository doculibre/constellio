package com.constellio.app.ui.framework.buttons;

import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.viewers.pdftron.PdfTronViewer;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import static com.constellio.app.ui.i18n.i18n.$;

public class PdfTronContentVersionWindow extends BaseWindow {

	private String recordId;
	private ContentVersionVO contentVersion;
	private String metadataCode;
	private String pdfTronKey;
	private boolean isReadonly;

	public PdfTronContentVersionWindow(String recordId, ContentVersionVO contentVersion, String metadataCode,
									   boolean isReadonly, String pdfTronKey) {
		super($("contentVersionWindowButton.caption"));
		this.setWidth("800px");
		this.setHeight("700px");
		this.setModal(true);

		this.metadataCode = metadataCode;
		this.contentVersion = contentVersion;
		this.recordId = recordId;
		this.pdfTronKey = pdfTronKey;

		this.setContent(buildWindowContent());

		this.isReadonly = isReadonly;
		addStyleName(ValoTheme.BUTTON_BORDERLESS);
		addStyleName(ValoTheme.BUTTON_LINK);
	}


	protected Component buildWindowContent() {
		VerticalLayout vlayoutMain = new VerticalLayout();

		PdfTronViewer pdfTronViewer = new PdfTronViewer(this.recordId, this.contentVersion, this.metadataCode, isReadonly, pdfTronKey);
		pdfTronViewer.setHeight("100%");

		vlayoutMain.addComponent(pdfTronViewer);
		vlayoutMain.setHeight("100%");

		return vlayoutMain;
	}
}
