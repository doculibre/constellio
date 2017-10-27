package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.reports.model.search.UnsupportedReportException;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.report.ReportGeneratorButton;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.Report.ListPrintableReportViewImpl;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.ui.pages.search.AdvancedSearchPresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.utils.ReportGeneratorUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.event.FieldEvents;
import com.vaadin.server.Page;
import com.vaadin.ui.*;

import java.util.*;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ReportTabButton extends WindowButton {
    private VerticalLayout mainLayout, PDFTabLayout;
    private TabSheet tabSheet;
    private BaseView view;
    private List<RecordVO> recordVOList;
    ReportTabButtonPresenter.MetadataSchemaCounter occurence;
    private List<PrintableReport> printableReportList;
    private ComboBox reportComboBox, customElementSelected, defaultElementSelected;
    private PrintableReportListPossibleType selectedReporType;
    private String selectedSchemaType;
    private boolean noExcelButton = false, noPDFButton = false, removePrintableTab = false, removeExcelTab = false;
    private AppLayerFactory factory;
    private String collection;
    private TextField numberOfCopies;
    private TabSheet.Tab excelTab, pdfTab;
    private NewReportPresenter viewPresenter;
    private List<PrintableReportTemplate> printableReportTemplateList;
    private ReportTabButtonPresenter buttonPresenter;
    private SessionContext sessionContext;

    public ReportTabButton(String caption, String windowCaption, BaseView view) {
        this(caption, windowCaption, view.getConstellioFactories().getAppLayerFactory(), view.getCollection(), false, false, null, view.getSessionContext());
        this.view = view;
    }

    public ReportTabButton(String caption, String windowCaption, BaseView view, boolean noExcelButton, boolean noPDFButton) {
        this(caption, windowCaption, view.getConstellioFactories().getAppLayerFactory(), view.getCollection(), noExcelButton, noPDFButton, null, view.getSessionContext());
        this.view = view;
    }

    public ReportTabButton(String caption, String windowCaption, BaseView view, boolean noExcelButton) {
        this(caption, windowCaption, view.getConstellioFactories().getAppLayerFactory(), view.getCollection(), noExcelButton, false, null, view.getSessionContext());
        this.view = view;
    }

    public ReportTabButton(String caption, String windowCaption, AppLayerFactory appLayerFactory, String collection, boolean noExcelButton, SessionContext sessionContext) {
        this(caption, windowCaption, appLayerFactory, collection, noExcelButton, false, null, sessionContext);
    }

    public ReportTabButton(String caption, String windowCaption, AppLayerFactory appLayerFactory, String collection, boolean noExcelButton, boolean noPDFButton, NewReportPresenter presenter, SessionContext sessionContext) {

        super(caption, windowCaption, new WindowConfiguration(true, true, "50%", "50%"));
        this.viewPresenter = presenter;
        this.factory = appLayerFactory;
        this.collection = collection;
        this.noExcelButton = noExcelButton;
        this.noPDFButton = noPDFButton;
        this.sessionContext = sessionContext;
        this.buttonPresenter = new ReportTabButtonPresenter(this);
    }
    public ReportTabButton setRecordVoList(RecordVO... recordVOS) {
        buttonPresenter.setRecordVoList(recordVOS);
        return this;
    }

    public ReportTabButton addRecordToVoList(RecordVO recordVO) {
        buttonPresenter.addRecordToVoList(recordVO);
        return this;
    }

    public String getCollection (){
        return collection;
    }

    public AppLayerFactory getFactory (){
        return this.factory;
    }

    public SessionContext getSessionContext() {
        return this.sessionContext;
    }

    @Override
    public void afterOpenModal() {
        if(buttonPresenter.isNeedToRemovePDFTab()) {
            pdfTab.setVisible(false);
        }

        if(buttonPresenter.isNeedToRemoveExcelTab()) {
            excelTab.setVisible(false);
        }

        if(this.removeExcelTab && this.removePrintableTab) {
            UI.getCurrent().removeWindow(super.getWindow());
            String errorMessage = $("ReportTabButton.noReportTemplateForCondition");
            Notification notification = new Notification(errorMessage + "<br/><br/>" + $("clickToClose"), Notification.Type.WARNING_MESSAGE);
            notification.setHtmlContentAllowed(true);
            notification.show(Page.getCurrent());
        }
    }

    @Override
    protected Component buildWindowContent() {
        mainLayout = new VerticalLayout();
        tabSheet = new TabSheet();
        if (!this.noExcelButton) {
            excelTab = tabSheet.addTab(createExcelTab(), $("ReportTabButton.ExcelReport"));
        }
        if (!this.noPDFButton) {
            pdfTab = tabSheet.addTab( createPDFTab(), $("ReportTabButton.PDFReport"));
        }
        mainLayout.addComponent(tabSheet);
        return mainLayout;
    }

    private VerticalLayout createExcelTab() {
        VerticalLayout verticalLayout = new VerticalLayout();
        try {
            NewReportPresenter newReportPresenter;
            if(viewPresenter == null) {
                AdvancedSearchPresenter Advancedpresenter = new AdvancedSearchPresenter((AdvancedSearchView) view);
                Advancedpresenter.setSchemaType(((AdvancedSearchView) view).getSchemaType());
                newReportPresenter = Advancedpresenter;
            } else {
                newReportPresenter = this.viewPresenter;
            }

            verticalLayout.addComponent(new ReportSelector(newReportPresenter));
            if(!verticalLayout.getComponent(0).isVisible()) {
                this.removeExcelTab = true;
            }
        } catch (UnsupportedReportException unsupportedReport) {
            view.showErrorMessage($("ReportTabButton.noExcelReport"));
        }
        return verticalLayout;
    }

    private VerticalLayout createPDFTab() {
        PDFTabLayout = new VerticalLayout();
        occurence = getAllMetadataSchemas();
        PDFTabLayout.addComponent(createDefaultSelectComboBox());
        PDFTabLayout.addComponent(createCustomSelectComboBox());
        PDFTabLayout.addComponent(createReportSelectorComboBox());
        PDFTabLayout.addComponent(createButtonLayout());
        PDFTabLayout.setSpacing(true);
        selectFirstDefaultReport();
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
        defaultElementSelected = new ComboBox();
        if (occurence.getNumberOfDefaultSchemaOccurence() == 1) {
            Iterator<PrintableReportListPossibleType> setIterator = occurence.getAllDefaultMetadataSchemaOccurence().keySet().iterator();
            if (setIterator.hasNext()) {
                selectedReporType = setIterator.next();
            }
            return new HorizontalLayout();
        }

        for (PrintableReportListPossibleType printableReportListPossibleType : occurence.getAllDefaultMetadataSchemaOccurence().keySet()) {
            defaultElementSelected.addItem(printableReportListPossibleType);
            defaultElementSelected.setItemCaption(printableReportListPossibleType, printableReportListPossibleType.getLabel());
            if (defaultElementSelected.getValue() == null) {
                defaultElementSelected.setValue(printableReportListPossibleType);
            }
        }
//        defaultElementSelected.setNullSelectionAllowed(false);
        defaultElementSelected.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws InvalidValueException {
                if (value == null) {
                    throw new InvalidValueException($("ReportTabButton.invalidReportType"));
                }
            }
        });
        defaultElementSelected.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                buttonPresenter.defaultElementSelectedValueChangeListener(event);
            }
        });
        defaultElementSelected.setCaption($("ReportTabButton.selectDefaultReportType"));
        defaultElementSelected.setWidth("100%");
        return defaultElementSelected;
    }

    private Component createCustomSelectComboBox() {
        customElementSelected = new ComboBox();
        if (occurence.getNumberOfCustomSchemaOccurence() == 1) {
            Iterator<MetadataSchemaVO> setIterator = occurence.getAllCustomMetadataSchemaOccurence().keySet().iterator();
            selectedSchemaType = setIterator.next().getCode();
            return new HorizontalLayout();
        }

        this.fillSchemaCombobox(customElementSelected);
//        customElementSelected.setNullSelectionAllowed(false);
        customElementSelected.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws InvalidValueException {
                if (value == null) {
                    throw new InvalidValueException($("ReportTabButton.invalidRecordSchema"));
                }
            }
        });
        customElementSelected.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                buttonPresenter.customElementSelectedValueChangeListener(event);
            }
        });
        customElementSelected.setCaption($("ReportTabButton.selectCustomReportSchema"));
        customElementSelected.setWidth("100%");
        return customElementSelected;
    }

    private Component createReportSelectorComboBox() {
        reportComboBox = new ComboBox();
        if (selectedSchemaType != null && selectedReporType != null) {
            reportComboBox = fillTemplateComboBox(reportComboBox);
        }
        reportComboBox.setCaption($("ReportTabButton.selectTemplate"));
        reportComboBox.setWidth("100%");
//        reportComboBox.setNullSelectionAllowed(false);
        reportComboBox.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws InvalidValueException {
                if (value == null) {
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
                if (selectedSchemaType.contains("_")) {
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
        for (RecordVO recordVO : recordVOList) {
            ids.add(recordVO.getId());
        }
        return ids;
    }

    private ComboBox fillTemplateComboBox(ComboBox comboBox) {
        comboBox.removeAllItems();
        printableReportTemplateList = ReportGeneratorUtils.getPrintableReportTemplate(factory, collection, selectedSchemaType, selectedReporType);
        if (!printableReportTemplateList.isEmpty())  {
            removePrintableTab = false;
            if (printableReportTemplateList.size() == 1) {
                PrintableReportTemplate onlyTemplate = printableReportTemplateList.get(0);
                comboBox.addItem(onlyTemplate);
                comboBox.setItemCaption(onlyTemplate, onlyTemplate.getTitle());
                comboBox.setValue(onlyTemplate);
                comboBox.setEnabled(false);
            } else {
                for (PrintableReportTemplate printableReport : printableReportTemplateList) {
                    comboBox.addItem(printableReport);
                    comboBox.setItemCaption(printableReport, printableReport.getTitle());
                    if (comboBox.getValue() == null) {
                        comboBox.setValue(printableReport);
                    }
                }
            }
        } else {
            removePrintableTab = true;
        }
        return comboBox;
    }

    private ComboBox fillSchemaCombobox(ComboBox comboBox) {
        comboBox.removeAllItems();
        int compteur = 0;
        for (MetadataSchemaVO metadataSchemaVO : occurence.getAllCustomMetadataSchemaOccurence().keySet()) {
            //check if the schema of the record isn't the default one, if yes
            if (selectedReporType == null || (metadataSchemaVO.getTypeCode().equals(selectedReporType.getSchemaType()) && !metadataSchemaVO.getTypeCode().contains("_default"))) {
                comboBox.addItem(metadataSchemaVO);
                comboBox.setItemCaption(metadataSchemaVO, metadataSchemaVO.getLabel());
                if (compteur == 0) {
                    comboBox.setValue(metadataSchemaVO);
                }
                compteur++;
            }
        }
        if (compteur == 1) {
            return null;
        }
        return comboBox;
    }

    private void selectFirstDefaultReport(){
        if(!defaultElementSelected.getItemIds().isEmpty()) {
            final PrintableReportListPossibleType firstDefaultItem = ((List<PrintableReportListPossibleType>)defaultElementSelected.getItemIds()).get(0);
            defaultElementSelected.setValue(firstDefaultItem);
            buttonPresenter.defaultElementSelectedValueChangeListener(new ReportTabButtonPresenter.customValueChangeEvent(firstDefaultItem));
        }
        if(!customElementSelected.getItemIds().isEmpty()) {
            final MetadataSchemaVO firstCustomItem =  ((List<MetadataSchemaVO>)customElementSelected.getItemIds()).get(0);
            buttonPresenter.customElementSelectedValueChangeListener(new ReportTabButtonPresenter.customValueChangeEvent(firstCustomItem));
        }
    }







}
