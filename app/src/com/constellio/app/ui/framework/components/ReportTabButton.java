package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.report.ReportGeneratorButton;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.Report.ListPrintableReportViewImpl;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.ui.pages.search.AdvancedSearchPresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.utils.ReportGeneratorUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.vaadin.data.Property;
import com.vaadin.ui.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ReportTabButton extends WindowButton {
    private VerticalLayout mainLayout, PDFTabLayout;
    private TabSheet tabSheet;
    private AdvancedSearchView view;
    private List<RecordVO> recordVOList;
    MetadataSchemaCounter occurence;
    private List<PrintableReport> printableReportList;
    private ComboBox reportComboBox;
    private PrintableReportListPossibleType selectedReporType;
    private String selectedSchemaType;

    public ReportTabButton(String caption, String windowCaption) {
        super(caption, windowCaption);
        recordVOList = new ArrayList<>();
    }

    public ReportTabButton(String caption, String windowCaption, AdvancedSearchView view) {
        this(caption, windowCaption);
        this.view = view;
    }

    public ReportTabButton setRecordVoList(RecordVO... recordVOS) {
        if(recordVOS.length > 0) {
            recordVOList.addAll(asList(recordVOS));
        }
        return this;
    }

    public ReportTabButton addRecordToVoList(RecordVO recordVO) {
        recordVOList.add(recordVO);
        return this;
    }

    @Override
    protected Component buildWindowContent() {
        mainLayout = new VerticalLayout();

        tabSheet = new TabSheet();
        tabSheet.addTab(createExcelTab(), $("ReportTabButton.ExcelReport"));
        tabSheet.addTab(createPDFTab(), $("ReportTabButton.PDFReport"));
        mainLayout.addComponent(tabSheet);
        return mainLayout;
    }

    private Component createExcelTab() {
        return new ReportSelector(new AdvancedSearchPresenter(view));
    }

    private Component createPDFTab() {
        PDFTabLayout = new VerticalLayout();
        occurence = getAllMetadataSchemas();
        PDFTabLayout.addComponent(createDefaultSelectComboBox());
        PDFTabLayout.addComponent(createCustomSelectComboBox());
        PDFTabLayout.addComponent(createReportSelectorComboBox());
        PDFTabLayout.addComponent(createButtonLayout());
        return PDFTabLayout;
    }

    private MetadataSchemaCounter getAllMetadataSchemas() {
        MetadataSchemaCounter metadataSchemaCounter = new MetadataSchemaCounter();
        List<MetadataSchemaVO> allSchemaList = getSchemaFromRecordVO();
        for (MetadataSchemaVO metadataSchemaVO : allSchemaList) {
            metadataSchemaCounter.addOrIncrementDefaultSchema(PrintableReportListPossibleType.getValue(metadataSchemaVO.getTypeCode().toUpperCase()));
            metadataSchemaCounter.addOrIncrementCustomSchema(metadataSchemaVO);
        }
        return metadataSchemaCounter;
    }

    private List<MetadataSchemaVO> getSchemaFromRecordVO() {
        List<MetadataSchemaVO> metadataSchemaVOList = new ArrayList<>();
        for (RecordVO recordVO : recordVOList) {
            metadataSchemaVOList.add(recordVO.getSchema());
        }
        return metadataSchemaVOList;
    }

    private Component createDefaultSelectComboBox() {
        if (occurence.getNumberOfDefaultSchemaOccurence() <= 1) {
            Iterator<PrintableReportListPossibleType> setIterator =  occurence.getAllDefaultMetadataSchemaOccurence().keySet().iterator();
            selectedReporType = setIterator.next();
            return new HorizontalLayout();
        }

        ComboBox defaultElementSelected = new ComboBox();
        for (PrintableReportListPossibleType printableReportListPossibleType : occurence.getAllDefaultMetadataSchemaOccurence().keySet()) {
            defaultElementSelected.addItem(printableReportListPossibleType);
            defaultElementSelected.setItemCaption(printableReportListPossibleType, printableReportListPossibleType.getLabel());
        }
        defaultElementSelected.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                selectedReporType = ((PrintableReportListPossibleType) event.getProperty().getValue());
                fillComboBox(reportComboBox);
            }
        });
        return defaultElementSelected;
    }

    private Component createCustomSelectComboBox() {
        if (occurence.getNumberOfCustomSchemaOccurence() <= 1) {
            Iterator<MetadataSchemaVO> setIterator =  occurence.getAllCustomMetadataSchemaOccurence().keySet().iterator();
            selectedSchemaType = setIterator.next().getCode();
            return new HorizontalLayout();
        }

        ComboBox customElementSelected = new ComboBox();
        for (MetadataSchemaVO metadataSchemaVO : occurence.getAllCustomMetadataSchemaOccurence().keySet()) {
            customElementSelected.addItem(metadataSchemaVO);
            customElementSelected.setItemCaption(metadataSchemaVO, metadataSchemaVO.getLabel());
        }

        customElementSelected.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                selectedSchemaType = ((MetadataSchemaVO) event.getProperty().getValue()).getCode();
                fillComboBox(reportComboBox);
            }
        });
        return customElementSelected;
    }

    private Component createReportSelectorComboBox() {
        reportComboBox = new ComboBox();
        fillComboBox(reportComboBox);
        return reportComboBox;
    }

    private Button createButtonLayout() {
        Button button = new Button($("LabelsButton.generate"));
        button.addStyleName(WindowButton.STYLE_NAME);
        button.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                getWindow().setContent(ReportGeneratorUtils.saveButtonClick(view.getConstellioFactories().getAppLayerFactory(), view.getCollection(), selectedSchemaType, (PrintableReportTemplate) reportComboBox.getValue(), 1, getIdsFromRecordVO()));
            }
        });
        return button;
    }

    private List<String> getIdsFromRecordVO() {
        List<String> ids = new ArrayList<>();
        for(RecordVO recordVO : recordVOList) {
            ids.add(recordVO.getId());
        }
        return ids;
    }

    private ComboBox fillComboBox(ComboBox comboBox) {
        comboBox.removeAllItems();
        List<PrintableReportTemplate> printableReportTemplateList = ReportGeneratorUtils.getPrintableReportTemplate(view.getConstellioFactories().getAppLayerFactory(), view.getCollection(), selectedSchemaType, selectedReporType);
        if(printableReportTemplateList.isEmpty()) {
            showNoDefinedReportTemplateForConditionError();
        } else {
            for (PrintableReportTemplate printableReport : printableReportTemplateList) {
                comboBox.addItem(printableReport);
                comboBox.setItemCaption(printableReport, printableReport.getTitle());
            }
        }
        return comboBox;
    }

    private void showNoDefinedReportTemplateForConditionError() {
        //TODO remove tab
        tabSheet.setSelectedTab(0);
        view.showErrorMessage($("ReportTabButton.noReportTemplateForCondition"));
    }

    private class MetadataSchemaCounter {
        Map<PrintableReportListPossibleType, Integer> defaultMetadataOccurence;
        Map<MetadataSchemaVO, Integer> customMetadataOccurence;
        public MetadataSchemaCounter() {
            defaultMetadataOccurence = new HashMap<>();
            customMetadataOccurence = new HashMap<>();
        }

        void addDefaultSchema(PrintableReportListPossibleType metadataSchemaVO) {
            defaultMetadataOccurence.put(metadataSchemaVO, 1);
        }

        void addCustomSchema(MetadataSchemaVO metadataSchemaVO) {
            customMetadataOccurence.put(metadataSchemaVO, 1);
        }

        void incrementDefaultSchema(PrintableReportListPossibleType metadataSchemaVO) {
            this.defaultMetadataOccurence.put(metadataSchemaVO, getDefaultMetadataSchemaOccurence(metadataSchemaVO) + 1);
        }

        void incrementCustomSchema(MetadataSchemaVO metadataSchemaVO) {
            this.customMetadataOccurence.put(metadataSchemaVO, getCustomMetadataSchemaOccurence(metadataSchemaVO) + 1);
        }

        int getDefaultMetadataSchemaOccurence(PrintableReportListPossibleType metadataSchemaVO) {
            return this.defaultMetadataOccurence.get(metadataSchemaVO);
        }

        int getCustomMetadataSchemaOccurence(MetadataSchemaVO metadataSchemaVO) {
            return this.customMetadataOccurence.get(metadataSchemaVO);
        }

        Map<PrintableReportListPossibleType, Integer> getAllDefaultMetadataSchemaOccurence() {
            return this.defaultMetadataOccurence;
        }

        Map<MetadataSchemaVO, Integer> getAllCustomMetadataSchemaOccurence() {
            return this.customMetadataOccurence;
        }

        void addOrIncrementDefaultSchema(PrintableReportListPossibleType metadataSchemaVO) {
            if (this.doesDefaultMetadataSchemaContains(metadataSchemaVO)) {
                this.incrementDefaultSchema(metadataSchemaVO);
            } else {
                this.addDefaultSchema(metadataSchemaVO);
            }
        }

        void addOrIncrementCustomSchema(MetadataSchemaVO metadataSchemaVO) {
            if (this.doesCustomMetadataSchemaContains(metadataSchemaVO)) {
                this.incrementCustomSchema(metadataSchemaVO);
            } else {
                this.addCustomSchema(metadataSchemaVO);
            }
        }

        boolean doesDefaultMetadataSchemaContains(PrintableReportListPossibleType metadataSchemaVO) {
            return this.defaultMetadataOccurence.keySet().contains(metadataSchemaVO);
        }

        boolean doesCustomMetadataSchemaContains(MetadataSchemaVO metadataSchemaVO) {
            return this.customMetadataOccurence.keySet().contains(metadataSchemaVO);
        }

        int getNumberOfCustomSchemaOccurence() {
            return this.customMetadataOccurence.size();
        }

        int getNumberOfDefaultSchemaOccurence() {
            return this.defaultMetadataOccurence.size();
        }
    }
}
