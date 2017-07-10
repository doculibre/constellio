package com.constellio.app.ui.framework.buttons.report;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.reports.XmlReportGenerator;
import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

/**
 * Created by Marco on 2017-07-10.
 */
public class ReportGeneratorButton extends WindowButton{
    //Window property
    @PropertyId("printableReportItems")
    private ComboBox printableItemsFields;
    @PropertyId("numberOfCopies")
    private TextField copiesField;


    private AppLayerFactory factory;
    private String collection;
    private Factory<List<PrintableReportTemplate>> printableReportFactory;
    private RecordVO[] elements;

    private VerticalLayout layout;
    private Button generateButton, cancelButton;

    public ReportGeneratorButton(String caption, String windowCaption, Factory<List<PrintableReportTemplate>> printableReportFactory, AppLayerFactory factory, String collection) {
        super(caption, windowCaption);
        this.factory = factory;
        this.collection = collection;
        this.printableReportFactory = printableReportFactory;
    }

    public ReportGeneratorButton(String caption, String windowCaption, Factory<List<PrintableReportTemplate>> printableReportFactory, AppLayerFactory factory, String collection, RecordVO... elements) {
        this(caption, windowCaption, printableReportFactory, factory, collection);
        this.setElements(elements);
    }

    public void setElements(RecordVO... elements){
        this.elements = elements;
    }

    private List<PrintableReportTemplate> getPrintableReportTemplate() {
        return this.printableReportFactory.get();
    }

    @Override
    protected Component buildWindowContent() {
        this.getWindow().setResizable(true);
        layout = new VerticalLayout();
        setupCopieFields();
        setupPrintableReportTemplateSelection();
        return new ReportGeneratorButtonForm(new LabelParametersVO(new LabelTemplate()), this, copiesField, printableItemsFields);
    }

    private void setupPrintableReportTemplateSelection() {
        printableItemsFields = new ComboBox();
        for(PrintableReportTemplate printableReportTemplate : getPrintableReportTemplate()) {
            printableItemsFields.addItem(printableReportTemplate);
            printableItemsFields.setItemCaption(printableReportTemplate, printableReportTemplate.getTitle());
        }
    }

    private void setupCopieFields() {
        copiesField = new TextField($("LabelsButton.numberOfCopies"));
        copiesField.setRequired(true);
        copiesField.setConverter(Integer.class);
    }

    private void setupButtonLayout() {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        cancelButton = new BaseButton($("cancel")) {
            @Override
            protected void buttonClick(ClickEvent event) {
                getWindow().close();
            }
        };
    }

    private class ReportGeneratorButtonForm extends BaseForm<LabelParametersVO> {
        private ReportGeneratorButton parent;

        public ReportGeneratorButtonForm(LabelParametersVO viewObject, Serializable objectWithMemberFields, Field... fields) {
            super(viewObject, objectWithMemberFields, fields);
            parent = ReportGeneratorButton.this;
        }

        @Override
        protected void saveButtonClick(LabelParametersVO viewObject) throws ValidationException {
            XmlReportGeneratorParameters xmlGeneratorParameters =  new XmlReportGeneratorParameters((Integer) parent.copiesField.getConvertedValue());
            xmlGeneratorParameters.setElementWithIds(elements[0].getSchema().getCode(), Arrays.stream(elements).map(RecordVO::getId).collect(Collectors.toList()));
            XmlReportGenerator xmlReportGenerator = new XmlReportGenerator(parent.factory, parent.collection, xmlGeneratorParameters);
        }

        @Override
        protected void cancelButtonClick(LabelParametersVO viewObject) {

        }

        private Record[] getRecordFromRecordVO() {
            List<RecordVO> recordVOS = asList(parent.elements);
            MetadataSchemasManager metadataSchemasManager = parent.factory.getModelLayerFactory().getMetadataSchemasManager();
            SearchServices searchServices = parent.factory.getModelLayerFactory().newSearchServices();
            LogicalSearchCondition condition = from(metadataSchemasManager.getSchemaTypes(collection).getSchema(recordVOS.get(0).getSchema().getCode())).where(Schemas.IDENTIFIER).isIn();
            return searchServices.search(new LogicalSearchQuery(condition)).toArray(new Record[0]);
        }
    }
}
