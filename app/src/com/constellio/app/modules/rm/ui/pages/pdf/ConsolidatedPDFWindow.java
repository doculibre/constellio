package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusMessageProvider;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.google.common.base.Preconditions.checkNotNull;

public class ConsolidatedPDFWindow extends BaseWindow {
    private Map<String, Tab> pdfTabs = new HashMap<>();
    private Map<String, PdfStatusViewImpl> pdfTabPanels = new HashMap<>();

    private TabSheet tabSheet;

    private static ConsolidatedPDFWindow instance;

    private boolean iconified;
    private float height;
    private Unit heightUnits;
    private float width;
    private Unit widthUnits;
    private Integer zIndex;
    private int positionX;
    private int positionY;
    private Button fermerLaFenetre;
    private Button minimiserLaFenetre;

    private ConsolidatedPDFWindow(String pdfName) {
        super($("ConsolidatedPDFWindow.caption"));

        setId("ConsolidatedPDFWindowId");

        init();

        addTabSheet(pdfName);

        iconified = false;
    }

    private void init() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.addComponent(tabSheet = new TabSheet());

        tabSheet.setWidth("100%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(true);
        horizontalLayout.setWidth("100%");

        minimiserLaFenetre = new Button("Minimiser la fenêtre");
        minimiserLaFenetre.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                iconify();
            }
        });

        fermerLaFenetre = new Button("Fermer la fenêtre");
        fermerLaFenetre.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });
        fermerLaFenetre.setVisible(false);

        horizontalLayout.addComponent(fermerLaFenetre);
        horizontalLayout.addComponent(minimiserLaFenetre);
        horizontalLayout.setComponentAlignment(fermerLaFenetre, Alignment.TOP_CENTER);
        horizontalLayout.setComponentAlignment(minimiserLaFenetre, Alignment.TOP_CENTER);

        verticalLayout.addComponent(horizontalLayout);

        setContent(verticalLayout);

        setWidth("60%");
        setModal(false);
        setClosable(false);
    }

    public void addTabSheet(String pdfFileName) {
        deiconify();

        if(pdfTabPanels.containsKey(pdfFileName)) {
            String baseName = FilenameUtils.getBaseName(pdfFileName);
            String extension = FilenameUtils.getExtension(pdfFileName);

            Set<String> keySet = pdfTabPanels.keySet();
            for (int i = 1; keySet.contains(pdfFileName = baseName + (i++) + "." + extension););
        }

        PdfStatusViewImpl panel = new PdfStatusViewImpl(pdfFileName);
        panel.addPdfGenerationCompletedListener(new PdfStatusViewImpl.PdfGenerationCompletedListener() {
            @Override
            public void firePdfGenerationCompleted(PdfStatusViewImpl panel) {
                // TODO : Handle the event
                tabSheet.setSelectedTab(pdfTabs.get(panel.getPdfFileName()));

                checkAllGenerationStatus();
                deiconify();
            }
        });

        Tab tab = tabSheet.addTab(panel, pdfFileName);

        pdfTabs.put(pdfFileName, tab);
        pdfTabPanels.put(pdfFileName, panel);

        tabSheet.setSelectedTab(tab);

        //TODO : code to be removed
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add("Fichier "+i+": en cours");
        }
        ((PdfStatusMessageProvider)panel.getDataProvider()).setMessages(list);
        // End TODO
    }

    protected void checkAllGenerationStatus() {
        for (PdfStatusViewImpl panel: pdfTabPanels.values()) {
            if(!panel.isFinished()) {
                return;
            }
        }

        setModal(true);

        minimiserLaFenetre.setVisible(false);
        fermerLaFenetre.setVisible(true);

        // TODO: should be thread safe
        instance = null;
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
        if(!isIconified()) {
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
        if(!isAttached()) {
            UI.getCurrent().addWindow(this);
        }
    }

    // TODO: check for thread safe
    public static synchronized void createPdf(String pdfName) {
        checkNotNull(StringUtils.trimToNull(pdfName), "PDF file name is mandatory");

        //TODO Active comments

        if(instance == null) {
            instance = new ConsolidatedPDFWindow(pdfName);
        } else {
            instance.addTabSheet(pdfName);
        }

        instance.show();
    }
}
