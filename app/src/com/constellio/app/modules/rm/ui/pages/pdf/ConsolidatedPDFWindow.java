package com.constellio.app.modules.rm.ui.pages.pdf;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ConsolidatedPDFWindow extends BaseWindow {
    public static final String WIDTH = "100%";
    private Map<String, Tab> pdfTabs = new HashMap<>();
    private Map<String, PdfStatusViewImpl> pdfTabPanels = new HashMap<>();

    private TabSheet tabSheet;

    private boolean iconified;
    private float height;
    private Unit heightUnits;
    private float width;
    private Unit widthUnits;
    private Integer zIndex;
    private int positionX;
    private int positionY;
    private Button closeWindowButton;
    private Button minimizeWindowButton;

    public ConsolidatedPDFWindow() {
        super($("ConsolidatedPDFWindow.caption"));

        setId("ConsolidatedPDFWindowId");

        init();
    }

    private void init() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addStyleName("consolidated-pdf-window-content");
        verticalLayout.setSpacing(true);
        verticalLayout.addComponent(tabSheet = new TabSheet());

        tabSheet.setWidth("100%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        horizontalLayout.setWidth(WIDTH);

        minimizeWindowButton = new Button($("ConsolidatedPDFWindow.minimize"));
        minimizeWindowButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                iconify();
            }
        });

        closeWindowButton = new Button($("ConsolidatedPDFWindow.close"));
        closeWindowButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        closeWindowButton.setVisible(false);

        horizontalLayout.addComponent(closeWindowButton);
        horizontalLayout.addComponent(minimizeWindowButton);
        horizontalLayout.setComponentAlignment(closeWindowButton, Alignment.TOP_CENTER);
        horizontalLayout.setComponentAlignment(minimizeWindowButton, Alignment.TOP_CENTER);

        verticalLayout.addComponent(horizontalLayout);

        setContent(verticalLayout);

        setWidth("60%");
        setModal(false);
        setClosable(false);
    }
    
    private void adjustCloseable() {
    	
    }

	private void addTabSheet(String pdfFileName, List<String> documentIds, boolean withMetadata) {
        deiconify();

        if (pdfTabPanels.containsKey(pdfFileName)) {
            String baseName = FilenameUtils.getBaseName(pdfFileName);
            String extension = FilenameUtils.getExtension(pdfFileName);

            Set<String> keySet = pdfTabPanels.keySet();
            for (int i = 1; keySet.contains(pdfFileName = baseName + (i++) + "." + extension););
        }

        PdfStatusViewImpl panel = new PdfStatusViewImpl(pdfFileName, documentIds, withMetadata);
        panel.addPdfGenerationCompletedListener(new PdfStatusViewImpl.PdfGenerationCompletedListener() {
            @Override
            public void firePdfGenerationCompleted(PdfStatusViewImpl panel) {
                tabSheet.setSelectedTab(pdfTabs.get(panel.getPdfFileName()));
                checkAllGenerationStatus();
                deiconify();
            }
        });

        Tab tab = tabSheet.addTab(panel, pdfFileName);

        pdfTabs.put(pdfFileName, tab);
        pdfTabPanels.put(pdfFileName, panel);

        tabSheet.setSelectedTab(tab);
    }
    
    public static ConsolidatedPDFWindow getInstance() {
		ConsolidatedPDFWindow instance = null;
		for (Window uiWindow : UI.getCurrent().getWindows()) {
			if (uiWindow instanceof ConsolidatedPDFWindow) {
				instance = (ConsolidatedPDFWindow) uiWindow;
				break;
			}
		}
		
		if (instance == null) {
			String key = ConsolidatedPDFWindow.class.getName();
			instance = ConstellioUI.getCurrentSessionContext().getAttribute(key);
			if (instance == null) {
				instance = new ConsolidatedPDFWindow();
				ConstellioUI.getCurrentSessionContext().setAttribute(key, instance);
			}
			UI.getCurrent().addWindow(instance);
		} 
		return instance;
    }

    protected void checkAllGenerationStatus() {
        for (PdfStatusViewImpl panel: pdfTabPanels.values()) {
            if (!panel.isFinished()) {
                return;
            }
        }

        setModal(true);

        minimizeWindowButton.setVisible(false);
        closeWindowButton.setVisible(true);
        setClosable(true);
    }

    public boolean isIconified() {
        return iconified;
    }

    private final MouseEvents.ClickListener iconListener = new MouseEvents.ClickListener() {
        @Override
        public void click(MouseEvents.ClickEvent event) {
            deiconify();
        }
    };

    private void iconify() {
        if (!isIconified()) {
            height = getHeight();
            heightUnits = getHeightUnits();

            width = getWidth();
            widthUnits = getWidthUnits();

            zIndex = getZIndex();

            positionX = getPositionX();
            positionY = getPositionY();

            setHeight("36px");
            setWidth("281px");

            Page page = Page.getCurrent();
            int browserWindowHeight = page.getBrowserWindowHeight();
            int browserWindowWidth = page.getBrowserWindowWidth();

            int posX = browserWindowWidth - 281 - 20;
            int posY = browserWindowHeight - 36 - 20;

            setPositionX(posX);
            setPositionY(posY);

            setResizable(false);
            addClickListener(iconListener);
        }

        iconified = true;
    }

    private void deiconify() {
        if(isIconified()) {
            setPosition(positionX, positionY);
            setZIndex(zIndex);

            setHeight(height, heightUnits);
            setWidth(width, widthUnits);

            removeClickListener(iconListener);
            setResizable(true);
        }

        iconified = false;
    }

    private void show() {
        if (!isAttached()) {
        	setClosable(false);
            UI.getCurrent().addWindow(this);
        }
    }

    public void createPdf(String pdfName, List<String> documentIds, boolean withMetadata) {
        checkNotNull(StringUtils.trimToNull(pdfName), "PDF file name is mandatory");
        checkArgument(!CollectionUtils.isEmpty(documentIds), "Document ids is mandatory and must not be empty");
        addTabSheet(pdfName, documentIds, withMetadata);
        show();
    }
}
