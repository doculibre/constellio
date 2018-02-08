package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusMessageProvider;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ConsolidatedPDFWindow extends BaseWindow {
    private Map<String, Tab> pdfTabs = new HashMap<>();
    private Map<String, PdfStatusViewImpl> pdfTabPanels = new HashMap<>();

    private TabSheet tabSheet;

    private static ConcurrentHashMap<String, ConsolidatedPDFWindow> instance = new ConcurrentHashMap<>();

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

    private long id;

    private ConsolidatedPDFWindow() {
        super($("ConsolidatedPDFWindow.caption"));

        setId("ConsolidatedPDFWindowId");

        init();
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

    private void addTabSheet(String pdfFileName, List<String> documentIds, boolean withMetadata) {
        deiconify();

        if(pdfTabPanels.containsKey(pdfFileName)) {
            String baseName = FilenameUtils.getBaseName(pdfFileName);
            String extension = FilenameUtils.getExtension(pdfFileName);

            Set<String> keySet = pdfTabPanels.keySet();
            for (int i = 1; keySet.contains(pdfFileName = baseName + (i++) + "." + extension););
        }

        PdfStatusViewImpl panel = new PdfStatusViewImpl(pdfFileName, documentIds, withMetadata);
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

        UserVO user = ConstellioUI.getCurrent().getSessionContext().getCurrentUser();
        instance.remove(user.getUsername());
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

    public static void createPdf(String pdfName, List<String> documentIds, boolean withMetadata) {
        checkNotNull(StringUtils.trimToNull(pdfName), "PDF file name is mandatory");
        checkArgument(!CollectionUtils.isEmpty(documentIds), "Document ids is mandatory and must not be empty");

        UserVO user = ConstellioUI.getCurrent().getSessionContext().getCurrentUser();

        ConsolidatedPDFWindow window = instance.get(user.getUsername());
        if(window == null) {
            instance.put(user.getUsername(), window = new ConsolidatedPDFWindow());
        }

        window.addTabSheet(pdfName, documentIds, withMetadata);
        window.show();
    }
}
