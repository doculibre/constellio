package com.constellio.app.modules.rm.ui.pages.pdf;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusTable;
import com.constellio.app.ui.entities.TemporaryRecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.LinkButton;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
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
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.ui.Button.ClickEvent;

public class PdfStatusViewImpl extends BaseViewImpl implements PdfStatusView {
	
    public static final String WIDTH = "100%";
    public static final String PROGRESS_LABEL_HEIGHT = "40px";
    private final String pdfFileName;
    private final PdfStatusViewPresenter presenter;

    private String documentPdfId;
    private boolean finished;
    private boolean errorOccurred;

    private List<PdfGenerationCompletedListener> listeners = new ArrayList<>();
    
    private String globalProgressMessage;
    private I18NHorizontalLayout progressLayout;
    private Label progressLabel;
    private Button viewConsolidatePdfButton;
    
    private VerticalLayout mainComponent;

    public PdfStatusViewImpl(String pdfFileName, List<String> documentIds, boolean withMetadata) {
        setWidth(WIDTH);

        this.pdfFileName = pdfFileName;
        this.presenter = new PdfStatusViewPresenter(this, pdfFileName, documentIds, withMetadata);
        this.finished = false;
        
        globalProgressMessage = $("PdfStatusView.generationProgress", 0, 0);
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

        progressLayout = new I18NHorizontalLayout();
        progressLayout.setWidth("100%");
        progressLayout.setSpacing(true);
        
        progressLabel = new Label(globalProgressMessage);
        progressLabel.setCaptionAsHtml(true);
        progressLabel.setHeight(PROGRESS_LABEL_HEIGHT);

        viewConsolidatePdfButton = new BaseButton($("PdfStatusView.viewPdfFile")) {
			@Override
			protected void buttonClick(ClickEvent event) {
				setMainComponentContent(true);
			}
		};
		viewConsolidatePdfButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
		viewConsolidatePdfButton.setVisible(errorOccurred);
        
        progressLayout.addComponents(progressLabel, viewConsolidatePdfButton);
        progressLayout.setComponentAlignment(viewConsolidatePdfButton, Alignment.TOP_RIGHT);

        layout.addComponent(progressLayout);
        layout.setExpandRatio(progressLayout, 1);

        Table table = new PdfStatusTable(getPdfFileName(), getDataProvider());
        table.setWidth("100%");
        layout.addComponent(table);

        return layout;
    }

    public void firePdfGenerationCompleted(String documentId, final boolean errorOccurred) {
        this.documentPdfId = documentId;
        this.finished = true;
        this.errorOccurred = errorOccurred;

        VaadinSession.getCurrent().access(new Runnable() {
            @Override
            public void run() {
            	enter(null);

                for (PdfGenerationCompletedListener listener : listeners) {
                    listener.firePdfGenerationCompleted(PdfStatusViewImpl.this, errorOccurred);
                }
            }
        });
    }

    @Override
    public void notifyGlobalProgressMessage(final String message) {
        VaadinSession.getCurrent().access(new Runnable() {
            @Override
            public void run() {
            	globalProgressMessage = message;
                progressLabel.setValue(message);
            }
        });
    }

    protected Layout createPdfGenerationCompletedLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
//        layout.setSpacing(true);
        
        Button download = new LinkButton($("PdfStatusView.downloadPdfFile")) {
            @SuppressWarnings("deprecation")
			@Override
            protected void buttonClick(ClickEvent event) {
                Page.getCurrent().open(presenter.getPdfDocumentResource(documentPdfId), null, false);
            }
        };

        layout.addComponent(download);
        layout.setExpandRatio(download, 1);

        Label label = new Label($("PdfStatusView.documentInTemporaryZone"));
        label.setContentMode(ContentMode.HTML);

        layout.addComponent(label);
        layout.setExpandRatio(label, 1);

        if (documentPdfId != null) {
            TemporaryRecordVO temporaryRecordVO = presenter.getPdfDocumentVO(documentPdfId);
            if (temporaryRecordVO != null) {
                ContentViewer contentViewer = new ContentViewer(temporaryRecordVO, TemporaryRecord.CONTENT, temporaryRecordVO.getContent());
                layout.addComponent(contentViewer);
                layout.setComponentAlignment(contentViewer, Alignment.TOP_CENTER);
            }
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
    	mainComponent = new VerticalLayout();
    	mainComponent.setSizeFull();
    	setMainComponentContent(false);
        return mainComponent;
    }
    
    private void setMainComponentContent(boolean loadViewerIfErrors) {
    	Component mainComponentContent;
        if (!isFinished() || (errorOccurred && !loadViewerIfErrors)) {
        	mainComponentContent = createPdfTableProgressLayout();
        } else {
        	mainComponentContent = createPdfGenerationCompletedLayout();
        }
        mainComponent.removeAllComponents();
        mainComponent.addComponent(mainComponentContent);
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
        public void firePdfGenerationCompleted(PdfStatusViewImpl panel, boolean errorOccurred);
    }
}