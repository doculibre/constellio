package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.vaadin.event.UIEvents.PollEvent;
import com.vaadin.event.UIEvents.PollListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ConsolidatedPdfWindow extends BaseWindow implements PollListener {

	private static final String ATTRIBUTE_KEY = ConsolidatedPdfWindow.class.getName();

	public static final String WIDTH = "100%";
	private Map<String, Tab> pdfTabs = new HashMap<>();
	private Map<String, PdfStatusViewImpl> pdfTabPanels = new HashMap<>();

	private TabSheet tabSheet;

	private Button closeWindowButton;
	private Button minimizeWindowButton;

	public ConsolidatedPdfWindow() {
		super($("ConsolidatedPDFWindow.caption"));
		setId("ConsolidatedPDFWindowId");
		init();
	}

	private void init() {
		VerticalLayout verticalLayout = new VerticalLayout();
		verticalLayout.setSpacing(true);
		verticalLayout.addStyleName("consolidated-pdf-window-content");
		verticalLayout.addComponent(tabSheet = new TabSheet());

		tabSheet.setWidth("100%");

		HorizontalLayout buttonsLayout = new I18NHorizontalLayout();
		buttonsLayout.setSpacing(true);
		buttonsLayout.setWidth(WIDTH);

		minimizeWindowButton = new Button($("ConsolidatedPDFWindow.minimize"));
		minimizeWindowButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				minimize();
			}
		});

		closeWindowButton = new Button($("ConsolidatedPDFWindow.close"));
		closeWindowButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(Button.ClickEvent event) {
				close();
			}
		});
		closeWindowButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
		closeWindowButton.setVisible(false);

		buttonsLayout.addComponent(closeWindowButton);
		buttonsLayout.addComponent(minimizeWindowButton);
		buttonsLayout.setComponentAlignment(closeWindowButton, Alignment.TOP_CENTER);
		buttonsLayout.setComponentAlignment(minimizeWindowButton, Alignment.TOP_CENTER);

		verticalLayout.addComponent(buttonsLayout);
		verticalLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);
		verticalLayout.setExpandRatio(tabSheet, 1);

		setContent(verticalLayout);

		setHeight("90%");
		setWidth("60%");
		setModal(false);
		setClosable(false);
	}

	private void addTabSheet(String pdfFileName, List<String> documentIds, boolean withMetadata, boolean asPdfA) {
		restoreMinimized();

		if (pdfTabPanels.containsKey(pdfFileName)) {
			String baseName = FilenameUtils.getBaseName(pdfFileName);
			String extension = FilenameUtils.getExtension(pdfFileName);

			Set<String> keySet = pdfTabPanels.keySet();
			for (int i = 1; keySet.contains(pdfFileName = baseName + (i++) + "." + extension); ) {
				;
			}
		}

		PdfStatusViewImpl panel = new PdfStatusViewImpl(pdfFileName, documentIds, withMetadata, asPdfA);
		panel.addPdfGenerationCompletedListener(new PdfStatusViewImpl.PdfGenerationCompletedListener() {
			@Override
			public void firePdfGenerationCompleted(PdfStatusViewImpl panel, boolean errorOccurred) {
				checkAllGenerationStatus();
				restoreMinimized();
				Tab pdfTab = pdfTabs.get(panel.getPdfFileName());
				tabSheet.setSelectedTab(pdfTab);
				//                setContent(getContent());
			}
		});

		Tab tab = tabSheet.addTab(panel, pdfFileName);

		pdfTabs.put(pdfFileName, tab);
		pdfTabPanels.put(pdfFileName, panel);

		tabSheet.setSelectedTab(tab);
	}

	public static ConsolidatedPdfWindow getInstance() {
		ConsolidatedPdfWindow instance = null;
		for (Window uiWindow : UI.getCurrent().getWindows()) {
			if (uiWindow instanceof ConsolidatedPdfWindow) {
				instance = (ConsolidatedPdfWindow) uiWindow;
				break;
			}
		}

		if (instance == null) {
			instance = ConstellioUI.getCurrent().getAttribute(ATTRIBUTE_KEY);
			if (instance == null) {
				instance = new ConsolidatedPdfWindow();
				ConstellioUI.getCurrent().setAttribute(ATTRIBUTE_KEY, instance);
			}
			UI.getCurrent().addWindow(instance);
		}
		return instance;
	}

	@Override
	public void close() {
		ConstellioUI.getCurrent().setAttribute(ATTRIBUTE_KEY, null);
		super.close();
	}

	public static void ensurePresentIfRunningAndNotAdded() {
		if (ConstellioUI.getCurrent() != null) {
			ConsolidatedPdfWindow contextInstance = ConstellioUI.getCurrent().getAttribute(ATTRIBUTE_KEY);
			if (contextInstance != null) {
				ConsolidatedPdfWindow uiInstance = null;
				for (Window uiWindow : UI.getCurrent().getWindows()) {
					if (uiWindow instanceof ConsolidatedPdfWindow) {
						uiInstance = (ConsolidatedPdfWindow) uiWindow;
						break;
					}
				}
				if (uiInstance == null) {
					UI.getCurrent().addWindow(contextInstance);
					contextInstance.minimize();
				}
			}
		}
	}

	protected void checkAllGenerationStatus() {
		for (PdfStatusViewImpl panel : pdfTabPanels.values()) {
			if (!panel.isFinished()) {
				setModal(false);
				minimizeWindowButton.setVisible(true);
				closeWindowButton.setVisible(false);
				setClosable(false);
				return;
			}
		}

		setModal(true);
		minimizeWindowButton.setVisible(false);
		closeWindowButton.setVisible(true);
		setClosable(true);
	}

	private void show() {
		if (!isAttached()) {
			setClosable(false);
			UI.getCurrent().addWindow(this);
		}
	}

	public void createPdf(String pdfName, List<String> documentIds, boolean withMetadata, boolean asPdfA) {
		checkNotNull(StringUtils.trimToNull(pdfName), "PDF file name is mandatory");
		checkArgument(!CollectionUtils.isEmpty(documentIds), "Document ids is mandatory and must not be empty");
		addTabSheet(pdfName, documentIds, withMetadata, asPdfA);
		show();
	}

	@Override
	public void poll(PollEvent event) {
	}

}
