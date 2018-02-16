package com.constellio.app.modules.rm.ui.pages.pdf;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.constellio.app.api.extensions.params.AvailableActionsParam;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;

public class ConsolidatedPdfButton extends WindowButton {
	
	private AvailableActionsParam param;

	public ConsolidatedPdfButton() {
		this(null);
	}

	public ConsolidatedPdfButton(AvailableActionsParam param) {
		super($("ConsolidatedPDFWindow.caption"), $("PdfFileNamePanel.caption"), WindowButton.WindowConfiguration.modalDialog("60%", "200px"));
		this.param = param;
		ConsolidatedPdfWindowImpl.ensurePresentIfRunningAndNotAdded();
	}
	
	public void setParams(AvailableActionsParam param) {
		this.param = param;
	}

	@Override
	protected Component buildWindowContent() {
        PdfFileNamePanel pdfPanel = new PdfFileNamePanel(getWindow());
        pdfPanel.addPdfFileNameListener(new PdfFileNamePanel.PdfFileNameListener() {
            @Override
            public void pdfFileNameFinished(PdfFileNamePanel.PdfInfos pdfInfos) {
                List<String> ids = param.getIds();
                if (!CollectionUtils.isEmpty(ids)) {
                	ConsolidatedPdfWindowImpl window = ConsolidatedPdfWindowImpl.getInstance();
                	window.createPdf(pdfInfos.getPdfFileName(), ids, pdfInfos.isIncludeMetadatas());
                } else {
                    showErrorMessage($("ConsolidatedPDFWindow.noDocumentSelectedForPdf"));
                }
            }

            @Override
            public void pdfFileNameCancelled() {
            }
        });
        return pdfPanel;
	}

    public void showErrorMessage(String errorMessage) {
        Notification notification = new Notification(errorMessage + "<br/><br/>" + $("clickToClose"), Notification.Type.WARNING_MESSAGE);
        notification.setHtmlContentAllowed(true);
        notification.show(Page.getCurrent());
    }

}
