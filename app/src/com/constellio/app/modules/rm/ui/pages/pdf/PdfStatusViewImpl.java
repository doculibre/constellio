package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusTable;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.viewers.ContentViewer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;

import java.util.ArrayList;
import java.util.List;

public class PdfStatusViewImpl extends BaseViewImpl implements PdfStatusView {
    private final String pdfFileName;
    private final PdfStatusViewPresenter presenter;

    private DocumentVO documentVO;
    private boolean finished;

    private List<PdfGenerationCompletedListener> listeners = new ArrayList<>();

    public PdfStatusViewImpl(String pdfFileName) {
        setWidth("100%");

        this.pdfFileName = pdfFileName;
        this.presenter = new PdfStatusViewPresenter(this);
        this.finished = false;

        addComponent(buildMainComponent(null));
    }

    @Override
    protected boolean isBreadcrumbsVisible() {
        return false;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public PdfStatusDataProvider<?> getDataProvider() {
        return presenter.getDataProvider();
    }

    protected Layout createPdfTableProgressLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        // TODO Fix CSS bugs
        Label label = new Label("Progression de la génération du fichier: " + getPdfFileName());
        label.setHeight("40px");

        layout.addComponent(label);
        layout.setExpandRatio(label, 1);

        Table table = new PdfStatusTable(getPdfFileName(), getDataProvider());
        table.setWidth("100%");
        layout.addComponent(table);

        return layout;
    }

    public void firePdfGenerationCompleted(DocumentVO documentVO) {
        this.documentVO = documentVO;
        this.finished = true;

        VaadinSession.getCurrent().access(new Runnable() {
            @Override
            public void run() {
                enter(null);

                for (PdfGenerationCompletedListener listener: listeners) {
                    listener.firePdfGenerationCompleted(PdfStatusViewImpl.this);
                }
            }
        });
    }

    protected Layout createPdfGenerationCompletedLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        Button download = new LinkButton("Télécharger le document PDF") {
            @Override
            protected void buttonClick(ClickEvent event) {
                // TODO Download the document here
            }
        };

        layout.addComponent(download);
        layout.setExpandRatio(download, 1);

        Label label = new Label("Ce document se trouve dans les enregistrements temporaires.");
        label.setContentMode(ContentMode.HTML);

        layout.addComponent(label);
        layout.setExpandRatio(label, 1);

        if(documentVO != null) {
            // TODO Afficher Document PDF dans PDF.js
            ContentViewer contentViewer = new ContentViewer(documentVO, Document.CONTENT, documentVO.getContent());

            layout.addComponent(contentViewer);

            // TODO How to solve this problem while the current window is a popup ??
            //if (popup) {
            // FIXME CSS bug when displayed in window, hiding for now.
            //contentViewer.setVisible(false);
            //}
        }

        return layout;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        if(!isFinished()) {
            return createPdfTableProgressLayout();
        } else {
            return createPdfGenerationCompletedLayout();
        }
    }

    public void addPdfGenerationCompletedListener(PdfGenerationCompletedListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removePdfGenerationCompletedListener(PdfGenerationCompletedListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public interface PdfGenerationCompletedListener {
        public void firePdfGenerationCompleted(PdfStatusViewImpl panel);
    }
}