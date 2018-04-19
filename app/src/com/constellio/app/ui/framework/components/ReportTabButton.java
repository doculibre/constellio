package com.constellio.app.ui.framework.components;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.reports.model.search.UnsupportedReportException;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.buttons.report.ReportGeneratorButton;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.Report.ListPrintableReportViewImpl;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.ui.pages.search.AdvancedSearchPresenter;
import com.constellio.app.ui.pages.search.AdvancedSearchView;
import com.constellio.app.utils.ReportGeneratorUtils;
import com.constellio.model.entities.records.Content;
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
    private List<PrintableReport> printableReportList;
    private ComboBox reportComboBox, customElementSelected, defaultElementSelected;
    private PrintableReportListPossibleType selectedReporType;
    private MetadataSchemaVO selectedSchema;
    private boolean noExcelButton = false, noPDFButton = false, removePrintableTab = false, removeExcelTab = false;
    private AppLayerFactory factory;
    private String collection;
    private TextField numberOfCopies;
    private TabSheet.Tab excelTab, pdfTab, errorTab;
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
        if(buttonPresenter.isNeedToRemovePDFTab() || reportComboBox.getContainerDataSource().size() == 0) {
            pdfTab.setVisible(false);
        }

        if(buttonPresenter.isNeedToRemoveExcelTab()) {
            excelTab.setVisible(false);
        }

        if(!pdfTab.isVisible() && !excelTab.isVisible()){
            errorTab.setVisible(true);
        } else {
            errorTab.setVisible(false);
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

        errorTab = tabSheet.addTab(createErrorTab(), $("ReportTabButton.ShowError"));

        mainLayout.addComponent(tabSheet);
        return mainLayout;
    }

    private VerticalLayout createErrorTab(){
        VerticalLayout verticalLayout = new VerticalLayout();

        Label label = new Label($("ReportTabButton.noReportTemplateForCondition"));

        verticalLayout.addComponent(label);

        return verticalLayout;
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
            if(newReportPresenter.getSupportedReports().isEmpty()) {
                buttonPresenter.setNeedToRemoveExcelTab(true);
            }
            verticalLayout.addComponent(new ReportSelector(newReportPresenter));
        } catch (UnsupportedReportException unsupportedReport) {
            view.showErrorMessage($("ReportTabButton.noExcelReport"));
        }
        return verticalLayout;
    }

    private VerticalLayout createPDFTab() {
        PDFTabLayout = new VerticalLayout();
        PDFTabLayout.addComponent(createDefaultSelectComboBox());
        PDFTabLayout.addComponent(createCustomSelectComboBox());
        PDFTabLayout.addComponent(createReportSelectorComboBox());
        PDFTabLayout.addComponent(createButtonLayout());
        PDFTabLayout.setSpacing(true);
        return PDFTabLayout;
    }

    private Component createDefaultSelectComboBox() {
        defaultElementSelected = new BaseComboBox();
        List<PrintableReportListPossibleType> values = buttonPresenter.getAllGeneralSchema();
        if (values.size() == 1) {
            selectedReporType = values.get(0);
            return new HorizontalLayout();
        }

        for (PrintableReportListPossibleType printableReportListPossibleType : values) {
            defaultElementSelected.addItem(printableReportListPossibleType);
            defaultElementSelected.setItemCaption(printableReportListPossibleType, buttonPresenter.getLabelForSchemaType(printableReportListPossibleType.getSchemaType()));
            if (defaultElementSelected.getValue() == null) {
                defaultElementSelected.setValue(printableReportListPossibleType);
                selectedReporType = printableReportListPossibleType;
            }
        }

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
                selectedReporType = (PrintableReportListPossibleType) defaultElementSelected.getValue();
                updateCustomSchemaValues();
            }
        });
        defaultElementSelected.setNullSelectionAllowed(false);
        defaultElementSelected.setCaption($("ReportTabButton.selectDefaultReportType"));
        defaultElementSelected.setWidth("100%");
        return defaultElementSelected;
    }

    private Component createCustomSelectComboBox() {
        customElementSelected = new BaseComboBox();

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
                selectedSchema = (MetadataSchemaVO) customElementSelected.getValue();
                updateAvailableReportForCurrentCustomSchema();
            }
        });
        customElementSelected.setCaption($("ReportTabButton.selectCustomReportSchema"));
        customElementSelected.setWidth("100%");
        customElementSelected.setNullSelectionAllowed(false);
        updateCustomSchemaValues();
        return customElementSelected;
    }

    private Component createReportSelectorComboBox() {
        reportComboBox = new BaseComboBox();

        reportComboBox.setCaption($("ReportTabButton.selectTemplate"));
        reportComboBox.setWidth("100%");
        reportComboBox.addValidator(new Validator() {
            @Override
            public void validate(Object value) throws InvalidValueException {
                if (value == null) {
                    throw new InvalidValueException($("ReportTabButton.invalidChoosenReport"));
                }
            }
        });
        updateAvailableReportForCurrentCustomSchema();
        reportComboBox.setNullSelectionAllowed(false);
        return reportComboBox;
    }

    private Button createButtonLayout() {
        Button button = new Button($("LabelsButton.generate"));
        button.addStyleName(WindowButton.STYLE_NAME);
        button.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                RecordVO recordVO = (RecordVO) reportComboBox.getValue();

                PrintableReportTemplate template = new PrintableReportTemplate(recordVO.getId(), recordVO.getTitle(), buttonPresenter.getReportContent(recordVO));
                getWindow().setContent(ReportGeneratorUtils.saveButtonClick(factory, collection, selectedSchema.getTypeCode(), template, 1, buttonPresenter.getRecordVOIdFilteredList(selectedSchema), view.getSessionContext().getCurrentLocale()));
            }
        });
        return button;
    }


    private void updateCustomSchemaValues(){
        if(customElementSelected != null) {
            customElementSelected.removeAllItems();
            customElementSelected.setVisible(true);
            List<MetadataSchemaVO> allCustomSchemaForCurrentGeneralSchema = buttonPresenter.getAllCustomSchema(selectedReporType);
            if(allCustomSchemaForCurrentGeneralSchema.size() == 1) {
                customElementSelected.setVisible(false);
                selectedSchema = allCustomSchemaForCurrentGeneralSchema.get(0);
            }
            for(MetadataSchemaVO metadataSchemaVO : allCustomSchemaForCurrentGeneralSchema) {
                customElementSelected.addItem(metadataSchemaVO);
                customElementSelected.setItemCaption(metadataSchemaVO, metadataSchemaVO.getLabel());
            }
            if(!allCustomSchemaForCurrentGeneralSchema.isEmpty()) {
                customElementSelected.setValue(allCustomSchemaForCurrentGeneralSchema.get(0));
            }
        }
    }

    private void updateAvailableReportForCurrentCustomSchema(){
        if(reportComboBox != null && selectedSchema != null)  {
            reportComboBox.removeAllItems();
            List<RecordVO> currentAvailableReport =  buttonPresenter.getAllAvailableReport(selectedSchema);
            reportComboBox.setEnabled(true);
            if(currentAvailableReport.isEmpty()) {
                //TODO SHOW ERROR
            } else if( currentAvailableReport.size() == 1) {
                reportComboBox.addItem(currentAvailableReport.get(0));
                reportComboBox.setItemCaption(currentAvailableReport.get(0), currentAvailableReport.get(0).getTitle());
                reportComboBox.setValue(currentAvailableReport.get(0));
                reportComboBox.setEnabled(false);
            } else {
                for(RecordVO recordVO : currentAvailableReport) {
                    reportComboBox.addItem(recordVO);
                    reportComboBox.setItemCaption(recordVO, recordVO.getTitle());
                }

                reportComboBox.setValue(currentAvailableReport.get(0));
            }
        }
    }







}
