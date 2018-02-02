package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.modules.rm.ui.pages.pdf.table.PdfStatusMessageProvider;
import com.constellio.app.ui.framework.components.BaseWindow;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.google.common.base.Preconditions.checkNotNull;

public class ConsolidatedPDFWindow extends BaseWindow {
    private Map<String, Tab> pdfTabs = new HashMap<>();

    private TabSheet tabSheet;

    private static ConsolidatedPDFWindow instance;

    private ConsolidatedPDFWindow(String pdfName) {
        super($("ConsolidatedPDFWindow.caption"));

        setId("ConsolidatedPDFWindowId");

        init();

        addTabSheet(pdfName);
    }

    private void init() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(true);
        verticalLayout.addComponent(tabSheet = new TabSheet());

        tabSheet.setWidth("100%");

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth("100%");

        Button minimiserLaFenetre = new Button("Minimiser la fenêtre");
        minimiserLaFenetre.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        horizontalLayout.addComponent(minimiserLaFenetre);
        horizontalLayout.setComponentAlignment(minimiserLaFenetre, Alignment.TOP_CENTER);

        verticalLayout.addComponent(horizontalLayout);

        setContent(verticalLayout);

        setWidth("60%");
        setModal(false);
        setClosable(false);


    }

    public void addTabSheet(String pdfFileName) {
        if(pdfTabs.containsKey(pdfFileName)) {
            String baseName = FilenameUtils.getBaseName(pdfFileName);
            String extension = FilenameUtils.getExtension(pdfFileName);

            Set<String> keySet = pdfTabs.keySet();
            for (int i = 1; keySet.contains(pdfFileName = baseName + (i++) + "." + extension););
        }

        PdfStatusViewImpl panel = new PdfStatusViewImpl(pdfFileName);
        panel.addPdfGenerationCompletedListener(new PdfStatusViewImpl.PdfGenerationCompletedListener() {
            @Override
            public void firePdfGenerationCompleted() {
                // TODO : Handle the event
            }
        });

        Tab tab = tabSheet.addTab(panel, pdfFileName);
        pdfTabs.put(pdfFileName, tab);

        tabSheet.setSelectedTab(tab);

        //TODO : code to be removed
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add("Fichier "+i+": en cours");
        }
        ((PdfStatusMessageProvider)panel.getDataProvider()).setMessages(list);
        // End TODO
    }

    private void show() {
        if(!isAttached()) {
            UI.getCurrent().addWindow(this);
        }
    }

    public static synchronized void createPdf(String pdfName) {
        checkNotNull(StringUtils.trimToNull(pdfName), "PDF file name is mandatory");

        //TODO Active comments

        /*if(instance == null) {*/
            instance = new ConsolidatedPDFWindow(pdfName);
        /*} else {
            instance.addTabSheet(pdfName);
        }*/

        instance.show();
    }
}
