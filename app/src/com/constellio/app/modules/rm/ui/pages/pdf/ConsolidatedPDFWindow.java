package com.constellio.app.modules.rm.ui.pages.pdf;

import com.constellio.app.ui.framework.components.BaseWindow;
import com.constellio.app.ui.i18n.i18n;
import com.google.common.base.Preconditions;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        verticalLayout.addComponent(tabSheet = new TabSheet());
        setContent(verticalLayout);

        setWidth("60%");
        setHeight("500px");
        setModal(true);

        UI.getCurrent().addWindow(this);
    }

    public void addTabSheet(String pdfFileName) {
        VerticalLayout verticalLayout = new VerticalLayout();

        if(pdfTabs.containsKey(pdfFileName)) {
            String baseName = FilenameUtils.getBaseName(pdfFileName);
            String extension = FilenameUtils.getExtension(pdfFileName);

            Set<String> keySet = pdfTabs.keySet();
            for (int i = 1; keySet.contains(pdfFileName = baseName + (i++) + "." + extension););
        }

        Tab tab = tabSheet.addTab(verticalLayout, pdfFileName);
        pdfTabs.put(pdfFileName, tab);

        tabSheet.setSelectedTab(tab);
    }

    private void show() {
        UI.getCurrent().addWindow(this);
    }

    public static synchronized void createPdf(String pdfName) {
        checkNotNull(StringUtils.trimToNull(pdfName), "PDF file name is mandatory");

        if(instance == null) {
            instance = new ConsolidatedPDFWindow(pdfName);
        } else {
            instance.addTabSheet(pdfName);
        }

        instance.show();
    }
}
