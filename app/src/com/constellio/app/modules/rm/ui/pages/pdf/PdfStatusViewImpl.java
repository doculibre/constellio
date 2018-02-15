package com.constellio.app.modules.rm.ui.pages.pdf;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusTable;
import com.constellio.app.ui.entities.TemporaryRecordVO;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.viewers.ContentViewer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;

public class PdfStatusViewImpl extends BaseViewImpl implements PdfStatusView {
	
    public static final String WIDTH = "100%";
    public static final String PROGRESS_LABEL_HEIGHT = "40px";
    private final String pdfFileName;
    private final PdfStatusViewPresenter presenter;

    private String documentPdfId;
    private boolean finished;

    private List<PdfGenerationCompletedListener> listeners = new ArrayList<>();
    private Label progressLabel;

    public PdfStatusViewImpl(String pdfFileName, List<String> documentIds, boolean withMetadata) {
        setWidth(WIDTH);

        this.pdfFileName = pdfFileName;
        progressLabel = new Label($("PdfStatusViewImpl.generationProgress", getPdfFileName()));
        this.presenter = new PdfStatusViewPresenter(this, pdfFileName, documentIds, withMetadata);
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
        layout.setSizeFull();
        layout.setSpacing(true);

        progressLabel.setHeight(PROGRESS_LABEL_HEIGHT);

        layout.addComponent(progressLabel);
        layout.setExpandRatio(progressLabel, 1);

        Table table = new PdfStatusTable(getPdfFileName(), getDataProvider());
        table.setWidth("100%");
        layout.addComponent(table);

        return layout;
    }

    public void firePdfGenerationCompleted(String documentId) {
        this.documentPdfId = documentId;
        this.finished = true;

        VaadinSession.getCurrent().access(new Runnable() {
            @Override
            public void run() {
                enter(null);

                for (PdfGenerationCompletedListener listener : listeners) {
                    listener.firePdfGenerationCompleted(PdfStatusViewImpl.this);
                }
            }
        });
    }

    @Override
    public void notifyGlobalProgressMessage(final String message) {
        VaadinSession.getCurrent().access(new Runnable() {
            @Override
            public void run() {
                progressLabel.setValue(message);
            }
        });
    }

    protected Layout createPdfGenerationCompletedLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);

        Button download = new LinkButton($("PdfStatusViewImpl.downloadPdfFile")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                Page.getCurrent().open(presenter.getPdfDocumentResource(documentPdfId), null, false);
            }
        };

        layout.addComponent(download);
        layout.setExpandRatio(download, 1);

        Label label = new Label($("PdfStatusViewImpl.documentInTemporaryZone"));
        label.setContentMode(ContentMode.HTML);

        layout.addComponent(label);
        layout.setExpandRatio(label, 1);

        if (documentPdfId != null) {
            TemporaryRecordVO temporaryRecordVO = presenter.getPdfDocumentVO(documentPdfId);
            ContentViewer contentViewer = new ContentViewer(temporaryRecordVO, TemporaryRecord.CONTENT, temporaryRecordVO.getContent());
            layout.addComponent(contentViewer);
            layout.setComponentAlignment(contentViewer, Alignment.TOP_CENTER);
        }

        return layout;
    }

    @Override
	protected boolean isFullWidthIfActionMenuAbsent() {
    	return true;
	}

	public boolean isFinished() {
        return finished;
    }

    @Override
    protected Component buildMainComponent(ViewChangeListener.ViewChangeEvent event) {
        if (!isFinished()) {
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