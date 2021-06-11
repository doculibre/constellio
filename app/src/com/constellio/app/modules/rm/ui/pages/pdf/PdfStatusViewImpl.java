package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusDataProvider;
import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusTable;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.DownloadLink;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.components.viewers.document.DocumentViewer;
import com.constellio.app.ui.pages.base.BaseViewImpl;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Resource;
import com.vaadin.server.StreamResource;
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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class PdfStatusViewImpl extends BaseViewImpl implements PdfStatusView {

	public static final String WIDTH = "100%";
	public static final String PROGRESS_LABEL_HEIGHT = "40px";
	private final String pdfFileName;
	private final PdfStatusViewPresenter presenter;
	private final boolean asPdfA;

	private File consolidatePdfFile;
	private boolean finished;
	private boolean errorOccurred;

	private List<PdfGenerationCompletedListener> listeners = new ArrayList<>();

	private String globalProgressMessage;
	private I18NHorizontalLayout progressLayout;
	private Label progressLabel;
	private Button viewConsolidatePdfButton;

	private VerticalLayout mainComponent;

	public PdfStatusViewImpl(String pdfFileName, List<String> documentIds, boolean withMetadata, boolean asPdfA) {
		setWidth(WIDTH);

		this.pdfFileName = pdfFileName;
		this.presenter = new PdfStatusViewPresenter(this, pdfFileName, documentIds, withMetadata, asPdfA);
		this.asPdfA = asPdfA;
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

	@Override
	public void firePdfGenerationCompleted(File consolidatePdfFile, final boolean errorOccurred) {
		this.finished = true;
		this.errorOccurred = errorOccurred;
		this.consolidatePdfFile = consolidatePdfFile;

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

	private Resource getPdfDocumentResource() {
		return new StreamResource(new StreamResource.StreamSource() {
			@Override
			public InputStream getStream() {
				return presenter.newConsolidatedPdfInputStream();
			}
		}, pdfFileName);
	}

	protected Layout createPdfGenerationCompletedLayout() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		//        layout.setSpacing(true);

		DownloadLink downloadLink = new DownloadLink(getPdfDocumentResource(),
				$(String.format("PdfStatusView.download%sFile", asPdfA ? "PdfA" : "Pdf")));

		layout.addComponent(downloadLink);
		layout.setExpandRatio(downloadLink, 1);

		Label label = new Label($("PdfStatusView.documentInTemporaryZone"));
		label.setContentMode(ContentMode.HTML);

		layout.addComponent(label);
		layout.setExpandRatio(label, 1);

		if (consolidatePdfFile != null) {
			DocumentViewer documentViewer = new DocumentViewer(consolidatePdfFile);
			layout.addComponent(documentViewer);
			layout.setComponentAlignment(documentViewer, Alignment.TOP_CENTER);
		} else {
			downloadLink.setVisible(false);
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