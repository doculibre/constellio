package com.constellio.app.ui.framework.buttons.report;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.ui.components.Dimensionnable;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.JasperPdfGenerator;
import com.constellio.app.modules.rm.services.reports.ReportXMLGeneratorV2;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.*;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.omg.IOP.IOR;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

/**
 * Created by Marco on 2017-06-29.
 */
public class LabelButtonV2 extends WindowButton {
    //Window property
    @PropertyId("startPosition")
    private ComboBox startPositionField;
    @PropertyId("labelConfigurations")
    private ComboBox formatField;
    @PropertyId("numberOfCopies")
    private TextField copiesField;

    //app Core
    private AppLayerFactory factory;
    private String collection;
    private RMSchemasRecordsServices rm;
    private SearchServices searchServices;
    private ContentManager contentManager;
    private RecordServices recordServices;
    private IOServicesFactory ioServicesFactory;
    private RecordToVOBuilder recordToVOBuilder;

    //Factory
    private Factory<List<LabelTemplate>> customLabelTemplatesFactory;
    private Factory<List<LabelTemplate>> defaultLabelTemplatesFactory;

    private RecordVO[] elements;

    private List<? extends Dimensionnable> listOfAllTemplates;

    private HorizontalLayout startAndCopiesLayout;

    private String schemaType;

    public LabelButtonV2(String caption, String windowsCaption, Factory<List<LabelTemplate>> customLabelTemplatesFactory, Factory<List<LabelTemplate>> defaultLabelTemplatesFactory, AppLayerFactory factory, String collection) {
        super(caption, windowsCaption);
        this.factory = factory;
        this.collection = collection;
        this.rm = new RMSchemasRecordsServices(this.collection, factory);
        this.searchServices = factory.getModelLayerFactory().newSearchServices();
        this.contentManager = factory.getModelLayerFactory().getContentManager();
        this.recordServices = factory.getModelLayerFactory().newRecordServices();
        this.ioServicesFactory = factory.getModelLayerFactory().getIOServicesFactory();
        this.recordToVOBuilder = new RecordToVOBuilder();

        this.customLabelTemplatesFactory = customLabelTemplatesFactory;
        this.defaultLabelTemplatesFactory = defaultLabelTemplatesFactory;


    }

    public LabelButtonV2(String caption, String windowsCaption, Factory<List<LabelTemplate>> customLabelTemplatesFactory, Factory<List<LabelTemplate>> defaultLabelTemplatesFactory, AppLayerFactory factory, String collection, RecordVO... elements) {
        this(caption, windowsCaption, customLabelTemplatesFactory, defaultLabelTemplatesFactory, factory, collection);
        this.setElements(elements);
    }

    public LabelButtonV2 setElements(RecordVO... elements) {
        this.elements = elements;
        return this;
    }

    public LabelButtonV2 setSchemaType(String schemaType) {
        this.schemaType = schemaType;
        return this;
    }

    public LabelButtonV2 setElementsWithIds(List<String> ids, String schemaType, SessionContext sessionContext) {
        List<RecordVO> recordVOS = new ArrayList<>();
        for (String id : ids) {
            recordVOS.add(getRecordVoFromId(id, schemaType, sessionContext));
        }
        this.elements = recordVOS.toArray(new RecordVO[0]);
        return this;
    }

    private List<LabelTemplate> getDefaultTemplate() {
        return this.defaultLabelTemplatesFactory.get();
    }

    private List<LabelTemplate> getCustomTemplate() {
        return this.customLabelTemplatesFactory.get();
    }

    @Override
    protected Component buildWindowContent() {
        setupStartingPositionField();
        setupFormLayoutField();
        setupCopieFields();
        return new TemplateBaseForm<>(new LabelParametersVO(new LabelTemplate()), this, startPositionField, formatField, copiesField);
    }

    private void setupStartingPositionField() {
        this.getWindow().setResizable(true);

        startPositionField = new ComboBox($("LabelsButton.startPosition"));
        startPositionField.setNullSelectionAllowed(false);
        startPositionField.setRequired(true);
        //Custom user templates.
        List<? extends Dimensionnable> customTemplates = getCustomTemplate();
        //Create a list with all customUserTemplate as default value
        listOfAllTemplates = new ArrayList<>(customTemplates);
        List<? extends Dimensionnable> printableLabels = getCustomTemplateForCurrentType();
        Dimensionnable firstItem;
        if (!printableLabels.isEmpty()) {
            listOfAllTemplates = ListUtils.union(printableLabels, customTemplates);
            firstItem = printableLabels.get(0);
        } else {
            List<? extends Dimensionnable> defaultTemplates = getDefaultTemplate();
            listOfAllTemplates = ListUtils.union(defaultTemplates, customTemplates);
            firstItem = defaultTemplates.get(0);
        }

        //Calculate the starting position of the first templates.
        calculateStartingPosition(firstItem);
    }

    private void setupFormLayoutField() {

        formatField = new ComboBox($("LabelsButton.labelFormat"));
        formatField.setRequired(true);

        setItemsForFormatFields(listOfAllTemplates);

        //We don't want to put too much stuff in the buildWindowContent. So we create a new class.
        formatField.addValueChangeListener(new TemplateValueChangeListener());
    }

    private void setupCopieFields() {

        copiesField = new TextField($("LabelsButton.numberOfCopies"));
        copiesField.setRequired(true);
        copiesField.setConverter(Integer.class);

        startAndCopiesLayout = new HorizontalLayout(startPositionField, copiesField);
        startAndCopiesLayout.setSpacing(true);
    }

    private List<? extends Dimensionnable> getCustomTemplateForCurrentType() {
        if(this.elements == null && this.schemaType == null) {
            return new ArrayList<>();
        }
        String schemaType = this.elements == null ? this.schemaType : this.elements[0].getSchema().getCode().split("_")[0];
        LogicalSearchCondition condition = from(rm.newPrintableLabel().getSchema()).where(rm.newPrintableLabel().getSchema().getMetadata(PrintableLabel.TYPE_LABEL)).isEqualTo(schemaType);
        return rm.wrapPrintableLabels(searchServices.search(new LogicalSearchQuery(condition)));
    }

    private void calculateStartingPosition(Dimensionnable template) {
        startPositionField.clear();
        for (int i = 1; i <= template.getDimension(); i++) {
            startPositionField.addItem(i);
        }
    }

    private List<String> getIdsFromElements() {
        List<String> ids = new ArrayList<>();
        for (RecordVO element : elements) {
            ids.add(element.getId());
        }
        return ids;
    }

    private void setItemsForFormatFields(List<? extends Dimensionnable> listOfAllTemplates) {
        for (Dimensionnable template : listOfAllTemplates) {
            formatField.addItem(template);
            //Check the captions of the current template.
            String itemCaption = template instanceof PrintableLabel ? ((PrintableLabel) template).getTitle() : $(((LabelTemplate) template).getName());
            formatField.setItemCaption(template, itemCaption);
        }

        if (listOfAllTemplates.size() > 0) {
            formatField.select(listOfAllTemplates.get(0));
        }
        formatField.setPageLength(listOfAllTemplates.size());
        formatField.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT);
        formatField.setNullSelectionAllowed(false);
        formatField.setValue(listOfAllTemplates.get(0));
    }

    public NewReportWriterFactory<LabelsReportParameters> getLabelsReportFactory() {
        final AppLayerCollectionExtensions extensions = this.factory.getExtensions().forCollection(collection);
        final RMModuleExtensions rmModuleExtensions = extensions.forModule(ConstellioRMModule.ID);
        return rmModuleExtensions.getReportBuilderFactories().labelsBuilderFactory.getValue();
    }

    private RecordVO getRecordVoFromId(String id, String schemaType, SessionContext sessionContext) {
        MetadataSchemasManager metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
        LogicalSearchCondition condition = from(metadataSchemasManager.getSchemaTypes(collection).getSchemaType(schemaType)).where(Schemas.IDENTIFIER).isEqualTo(id);
        return this.recordToVOBuilder.build(searchServices.searchSingleResult(condition), RecordVO.VIEW_MODE.DISPLAY, sessionContext);

    }

    private class TemplateValueChangeListener implements Property.ValueChangeListener {

        @Override
        public void valueChange(Property.ValueChangeEvent event) {
            Dimensionnable selectedTemplate = (Dimensionnable) event.getProperty().getValue();
            Integer previousStartPosition = (Integer) startPositionField.getValue();
            calculateStartingPosition(selectedTemplate);

            if (previousStartPosition != null && previousStartPosition <= selectedTemplate.getDimension()) {
                startPositionField.setValue(previousStartPosition);
            } else if (selectedTemplate.getDimension() > 0) {
                startPositionField.setValue(1);
            }
        }
    }

    private class TemplateBaseForm<T extends LabelParametersVO> extends BaseForm<T> {

        public TemplateBaseForm(T viewObject, Serializable objectWithMemberFields, Field<?>... fields) {
            super(viewObject, objectWithMemberFields, fields);
        }

        @Override
        protected void addFieldToLayout(Field<?> field, VerticalLayout fieldLayout) {
            if (field == startPositionField) {
                fieldLayout.addComponent(startAndCopiesLayout);
            } else if (field != copiesField) {
                super.addFieldToLayout(field, fieldLayout);
            }
        }

        @Override
        protected void saveButtonClick(T viewObject) throws ValidationException {
            VerticalLayout preview = null;
            Dimensionnable selectedTemplate = (Dimensionnable) formatField.getValue();
            try {
                if (selectedTemplate instanceof PrintableLabel) {
                    preview = generateLabelFromPrintableLabel(selectedTemplate);
                } else if (selectedTemplate instanceof LabelTemplate) {
                    preview = generateLabelFromLabelTemplate(viewObject);
                }

                getWindow().setContent(preview);
                getWindow().setHeight("90%");
                getWindow().center();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void cancelButtonClick(T viewObject) {
            getWindow().close();
        }

        private VerticalLayout generateLabelFromPrintableLabel(Dimensionnable selectedTemplate) throws Exception {
            VerticalLayout layout = null;
            if (validateInputs(selectedTemplate)) {
                ReportXMLGeneratorV2 reportXMLGeneratorV2 = new ReportXMLGeneratorV2(collection, factory).setStartingPosition((Integer) startPositionField.getValue())
                        .setNumberOfCopies(Integer.parseInt(copiesField.getValue().trim())).setElements(getRecordFromElements(elements));
                PrintableLabel selectedTemplateAsPrintableLabel = ((PrintableLabel) selectedTemplate);
                JasperPdfGenerator jasperPdfGenerator = new JasperPdfGenerator(reportXMLGeneratorV2);
                Content content = selectedTemplateAsPrintableLabel.get(PrintableLabel.JASPERFILE);
                InputStream inputStream = contentManager.getContentInputStream(content.getCurrentVersion().getHash(), content.getId());
                File jasperFile = ioServicesFactory.newIOServices().newTemporaryFile("jasper.jasper");
                try {
                    FileUtils.copyInputStreamToFile(inputStream, jasperFile);
                    String titleOfthePdfFile = ReportXMLGeneratorV2.escapeForXmlTag(selectedTemplateAsPrintableLabel.getTitle()) + ".pdf";
                    File generatedPdfFile = jasperPdfGenerator.createPDFFromXmlAndJasperFile(jasperFile, titleOfthePdfFile);
                    layout = new LabelViewer(generatedPdfFile, titleOfthePdfFile);
                } finally {
                    ioServicesFactory.newIOServices().deleteQuietly(jasperFile);
                    ioServicesFactory.newIOServices().closeQuietly(inputStream);
                }
            }
            return layout;
        }

        private Record[] getRecordFromElements(RecordVO... recordVOS) {
            List<Record> recordList = new ArrayList<>();
            for (RecordVO recordVO : recordVOS) {
                recordList.add(recordServices.getDocumentById(recordVO.getId()));
            }
            return recordList.toArray(new Record[0]);
        }

        private VerticalLayout generateLabelFromLabelTemplate(T parametersVO) {
            LabelTemplate labelTemplate = formatField.getValue() != null ? (LabelTemplate) formatField.getValue() : new LabelTemplate();
            LabelsReportParameters params = new LabelsReportParameters(getIdsFromElements(), labelTemplate,
                    parametersVO.getStartPosition(), parametersVO.getNumberOfCopies());
            if (getLabelsReportFactory() != null) {
                ReportWriter writer = getLabelsReportFactory().getReportBuilder(params);
                return new ReportViewer(writer, getLabelsReportFactory().getFilename(params));
            }
            return null;
        }

        private boolean validateInputs(Dimensionnable selectedTemplates) {
            if ((Integer) startPositionField.getValue() > selectedTemplates.getDimension()) {
                //show error
                $("ButtonLabel.error.posisbiggerthansize");
                return false;
            }
            int numberOfCopies = Integer.parseInt(copiesField.getValue());
            if (numberOfCopies >= 1000 && numberOfCopies < 0) {
                showErrorMessage($("LabelsButton.cannotPrint1000Labels"));
                return false;
            }
            return true;
        }

        @Override
        protected String getSaveButtonCaption() {
            return $("LabelsButton.generate");
        }

    }
}
