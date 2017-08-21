package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.reports.model.search.UnsupportedReport;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
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
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.server.Page;
import com.vaadin.ui.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ReportTabButton extends WindowButton {
    private VerticalLayout mainLayout, PDFTabLayout;
    private TabSheet tabSheet;
    private BaseView view;
    private List<RecordVO> recordVOList;
    MetadataSchemaCounter occurence;
    private List<PrintableReport> printableReportList;
    private ComboBox reportComboBox, customElementSelected;
    private PrintableReportListPossibleType selectedReporType;
    private String selectedSchemaType;
    private boolean noExcelButton = false, noPDFButton = false;
    private AppLayerFactory factory;
    private String collection;
    private TextField numberOfCopies;

    public ReportTabButton(String caption, String windowCaption, BaseView view) {
        this(caption, windowCaption, view.getConstellioFactories().getAppLayerFactory(), view.getCollection(), false, false);
        this.view = view;
    }

    public ReportTabButton(String caption, String windowCaption, BaseView view, boolean noExcelButton) {
        this(caption, windowCaption, view.getConstellioFactories().getAppLayerFactory(), view.getCollection(), noExcelButton, false);
        this.view = view;
    }

    public ReportTabButton(String caption, String windowCaption, AppLayerFactory appLayerFactory, String collection, boolean noExcelButton){
        this(caption, windowCaption, appLayerFactory, collection, noExcelButton, false);
    }

    public ReportTabButton(String caption, String windowCaption, BaseView view, boolean noExcelButton, boolean noPDFButton) {
        this(caption, windowCaption, view.getConstellioFactories().getAppLayerFactory(), view.getCollection(), noExcelButton, noPDFButton);
        this.view = view;
    }
    public ReportTabButton(String caption, String windowCaption, AppLayerFactory appLayerFactory, String collection, boolean noExcelButton, boolean noPDFButton) {
        super(caption, windowCaption);
        this.factory = appLayerFactory;
        this.collection = collection;
        this.noExcelButton = noExcelButton;
        this.noPDFButton = noPDFButton;
        recordVOList = new ArrayList<>();
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
        if(!this.noExcelButton) {
            tabSheet.addTab(createExcelTab(), $("ReportTabButton.ExcelReport"));
        }
        if(!this.noPDFButton){
            tabSheet.addTab(createPDFTab(), $("ReportTabButton.PDFReport"));
        }
        mainLayout.addComponent(tabSheet);
        return mainLayout;
    }

    private Component createExcelTab() {
        VerticalLayout verticalLayout = new VerticalLayout();
        try{
            AdvancedSearchPresenter presenter = new AdvancedSearchPresenter((AdvancedSearchView) view);
            presenter.setSchemaType(((AdvancedSearchView) view).getSchemaType());
            verticalLayout.addComponent(new ReportSelector(presenter));
        }catch (UnsupportedReport unsupportedReport ){
            view.showErrorMessage($("ReportTabButton.noExcelReport"));
        }
        return verticalLayout;
    }

    private Component createPDFTab() {
        PDFTabLayout = new VerticalLayout();
        occurence = getAllMetadataSchemas();
        PDFTabLayout.addComponent(createDefaultSelectComboBox());
        PDFTabLayout.addComponent(createCustomSelectComboBox());
        PDFTabLayout.addComponent(createReportSelectorComboBox());
        PDFTabLayout.addComponent(createButtonLayout());
        PDFTabLayout.setSpacing(true);
        return PDFTabLayout;
    }

    private MetadataSchemaCounter getAllMetadataSchemas() {
        MetadataSchemaCounter metadataSchemaCounter = new MetadataSchemaCounter();
        List<MetadataSchemaVO> allSchemaList = getSchemaFromRecordVO();
        for (MetadataSchemaVO metadataSchemaVO : allSchemaList) {
            metadataSchemaCounter.addOrIncrementDefaultSchema(PrintableReportListPossibleType.getValueFromSchemaType(metadataSchemaVO.getTypeCode()));
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
            if(setIterator.hasNext()) {
                selectedReporType = setIterator.next();
            }
            return new HorizontalLayout();
        }

        final ComboBox defaultElementSelected = new ComboBox();
        for (PrintableReportListPossibleType printableReportListPossibleType : occurence.getAllDefaultMetadataSchemaOccurence().keySet()) {
            defaultElementSelected.addItem(printableReportListPossibleType);
            defaultElementSelected.setItemCaption(printableReportListPossibleType, printableReportListPossibleType.getLabel());
            if(defaultElementSelected.getValue() == null) {
                defaultElementSelected.setValue(printableReportListPossibleType);
            }
        }
//        defaultElementSelected.setNullSelectionAllowed(false);
        defaultElementSelected.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws InvalidValueException {
                if(value == null) {
                    throw new InvalidValueException($("ReportTabButton.invalidReportType"));
                }
            }
        });
        defaultElementSelected.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                selectedReporType = ((PrintableReportListPossibleType) event.getProperty().getValue());
                if(fillSchemaCombobox(customElementSelected) == null) {
                    customElementSelected.setVisible(false);
                    customElementSelected.setEnabled(false);
                    Iterator<MetadataSchemaVO> setIterator =  occurence.getAllCustomMetadataSchemaOccurence().keySet().iterator();
                    do{
                        selectedSchemaType = setIterator.next().getCode();
                    }while(setIterator.hasNext() && !selectedSchemaType.contains(selectedReporType.getSchemaType()));

                } else {
                    customElementSelected.setVisible(true);
                    customElementSelected.setEnabled(true);
                    selectedSchemaType = null;
                    reportComboBox.setValue(null);

                }
                if(selectedSchemaType != null && selectedReporType != null) {
                    reportComboBox = fillTemplateComboBox(reportComboBox);
                }
            }
        });
        defaultElementSelected.setCaption($("ReportTabButton.selectDefaultReportType"));
        defaultElementSelected.setWidth("100%");
        return defaultElementSelected;
    }

    private Component createCustomSelectComboBox() {
        if (occurence.getNumberOfCustomSchemaOccurence() <= 1) {
            Iterator<MetadataSchemaVO> setIterator =  occurence.getAllCustomMetadataSchemaOccurence().keySet().iterator();
            selectedSchemaType = setIterator.next().getCode();
            return new HorizontalLayout();
        }

        customElementSelected = new ComboBox();
        this.fillSchemaCombobox(customElementSelected);
//        customElementSelected.setNullSelectionAllowed(false);
        customElementSelected.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws InvalidValueException {
                if(value == null) {
                    throw new InvalidValueException($("ReportTabButton.invalidRecordSchema"));
                }
            }
        });
        customElementSelected.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if(event.getProperty() != null && event.getProperty().getValue() != null) {
                    selectedSchemaType = ((MetadataSchemaVO) event.getProperty().getValue()).getCode();
                    if(selectedSchemaType != null && selectedReporType != null) {
                        reportComboBox = fillTemplateComboBox(reportComboBox);
                    }
                }
            }
        });
        customElementSelected.setCaption($("ReportTabButton.selectCustomReportSchema"));
        customElementSelected.setWidth("100%");
        return customElementSelected;
    }

    private Component createReportSelectorComboBox() {
        reportComboBox = new ComboBox();
        if(selectedSchemaType != null && selectedReporType != null) {
            reportComboBox = fillTemplateComboBox(reportComboBox);
        }
        reportComboBox.setCaption($("ReportTabButton.selectTemplate"));
        reportComboBox.setWidth("100%");
//        reportComboBox.setNullSelectionAllowed(false);
        reportComboBox.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws InvalidValueException {
                if(value == null) {
                    throw new InvalidValueException($("ReportTabButton.invalidChoosenReport"));
                }
            }
        });
        return reportComboBox;
    }

    private Component createNumberOfCopiesField() {
        numberOfCopies = new TextField();
        numberOfCopies.setConverter(Integer.class);
        numberOfCopies.setValue("1");
        numberOfCopies.setCaption($("ReportTabButton.inputNumberOfCopies"));
        numberOfCopies.setWidth("100%");
        return numberOfCopies;
    }

    private Button createButtonLayout() {
        Button button = new Button($("LabelsButton.generate"));
        button.addStyleName(WindowButton.STYLE_NAME);
        button.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if(selectedSchemaType.contains("_")) {
                    selectedSchemaType = selectedSchemaType.split("_")[0];
//                    MetadataSchemasManager metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
//                    metadataSchemasManager.getSchemaTypes(collection).getSchema(selectedSchemaType);
                }
                getWindow().setContent(ReportGeneratorUtils.saveButtonClick(factory, collection, selectedSchemaType, (PrintableReportTemplate) reportComboBox.getValue(), 1, getIdsFromRecordVO()));
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

    private ComboBox fillTemplateComboBox(ComboBox comboBox) {
        comboBox.removeAllItems();
        List<PrintableReportTemplate> printableReportTemplateList = ReportGeneratorUtils.getPrintableReportTemplate(factory, collection, selectedSchemaType, selectedReporType);
        if(printableReportTemplateList.isEmpty()) {
            showNoDefinedReportTemplateForConditionError();
        } else {
            if(printableReportTemplateList.size() == 1) {
                PrintableReportTemplate onlyTemplate = printableReportTemplateList.get(0);
                comboBox.addItem(onlyTemplate);
                comboBox.setItemCaption(onlyTemplate, onlyTemplate.getTitle());
                comboBox.setValue(onlyTemplate);
                comboBox.setEnabled(false);
            } else {
                for (PrintableReportTemplate printableReport : printableReportTemplateList) {
                    comboBox.addItem(printableReport);
                    comboBox.setItemCaption(printableReport, printableReport.getTitle());
                    if(comboBox.getValue() == null) {
                        comboBox.setValue(printableReport);
                    }
                }
            }
        }
        return comboBox;
    }

    private ComboBox fillSchemaCombobox(ComboBox comboBox) {
        comboBox.removeAllItems();
        int compteur = 0;
        for (MetadataSchemaVO metadataSchemaVO : occurence.getAllCustomMetadataSchemaOccurence().keySet()) {
            //check if the schema of the record isn't the default one, if yes
            if(selectedReporType == null || (metadataSchemaVO.getTypeCode().equals(selectedReporType.getSchemaType()) && !metadataSchemaVO.getTypeCode().contains("_default"))) {
                comboBox.addItem(metadataSchemaVO);
                comboBox.setItemCaption(metadataSchemaVO, metadataSchemaVO.getLabel());
                if(compteur == 0) {
                    comboBox.setValue(metadataSchemaVO);
                }
                compteur++;
            }
        }
        if(compteur == 1) {
           return null;
        }
        return comboBox;
    }

    private void showNoDefinedReportTemplateForConditionError() {
        String errorMessage = $("ReportTabButton.noReportTemplateForCondition");
        //TODO remove tab
        if(view == null) {
            Notification notification = new Notification(errorMessage + "<br/><br/>" + $("clickToClose"), Notification.Type.WARNING_MESSAGE);
            notification.setHtmlContentAllowed(true);
            notification.show(Page.getCurrent());
        }
        tabSheet.setSelectedTab(0);
        if(view != null ) {
            view.showErrorMessage(errorMessage);
        }

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
