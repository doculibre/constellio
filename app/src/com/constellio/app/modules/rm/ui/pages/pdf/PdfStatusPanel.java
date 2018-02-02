package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusTable;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.viewers.ContentViewer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.util.List;

public class PdfStatusPanel<T> extends VerticalLayout {
    private final String pdfFileName;
    private final PdfStatusDataProvider<T> dataProvider;


    private boolean finished;

    public PdfStatusPanel(String pdfFileName, PdfStatusDataProvider<T> dataProvider) {
        setSpacing(true);
        setWidth("100%");

        this.pdfFileName = pdfFileName;
        this.dataProvider = dataProvider;
        this.finished = false;

        switchPdfTableProgress();
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public PdfStatusDataProvider<T> getDataProvider() {
        return dataProvider;
    }

    protected void switchPdfTableProgress() {
        Label label = new Label("Progression de la génération du fichier: " + getPdfFileName());
        label.setHeight("40px");

        addComponent(label);
        setExpandRatio(label, 1);

        Table table = new PdfStatusTable(getPdfFileName(), getDataProvider());
        table.setWidth("100%");
        addComponent(table);

        switchPdfGenerationCompleted(null);
    }

    public void switchPdfGenerationCompleted(DocumentVO documentVO) {
        if (isFinished()) return;

        finished = true;

        removeAllComponents();

        Button download = new LinkButton("Télécharger le document PDF") {
            @Override
            protected void buttonClick(ClickEvent event) {
                // TODO Download the document here
            }
        };

        addComponent(download);
        setExpandRatio(download, 1);

        Label label = new Label("Ce document se trouve dans les enregistrements temporaires.");
        label.setContentMode(ContentMode.HTML);

        addComponent(label);
        setExpandRatio(label, 1);

        if(documentVO != null) {
            // TODO Afficher Document PDF dans PDF.js
            ContentViewer contentViewer = new ContentViewer(documentVO, Document.CONTENT, documentVO.getContent());

            addComponent(contentViewer);

            // TODO How to solve this problem while the current window is a popup ??
            //if (popup) {
            // FIXME CSS bug when displayed in window, hiding for now.
            //contentViewer.setVisible(false);
            //}
        } else {
            addComponent(new CustomComponent());
        }
    }

    public boolean isFinished() {
        return finished;
    }
}