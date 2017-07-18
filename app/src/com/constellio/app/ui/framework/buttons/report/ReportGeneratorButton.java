package com.constellio.app.ui.framework.buttons.report;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.utils.ReportGeneratorUtils;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.reports.JasperPdfGenerator;
import com.constellio.app.modules.rm.services.reports.XmlReportGenerator;
import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.rm.wrappers.structures.AlertCode;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.BaseButton;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleView;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.CompositeLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.*;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

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
    private List<PrintableReportTemplate> printableReportFactory;
    private RecordVO[] elements;

    private VerticalLayout layout;
    private Button generateButton, cancelButton;
    private PrintableReportListPossibleView currentSchema;
    private BaseView view;

    public ReportGeneratorButton(String caption, String windowCaption, BaseView view, AppLayerFactory factory, String collection, PrintableReportListPossibleView currentSchema) {
        super(caption, windowCaption);
        this.factory = factory;
        this.collection = collection;
        this.currentSchema = currentSchema;
        this.view = view;
    }

    public ReportGeneratorButton(String caption, String windowCaption, BaseView view, AppLayerFactory factory, String collection, PrintableReportListPossibleView currentSchema, RecordVO... elements) {
        this(caption, windowCaption, view, factory, collection, currentSchema);
        this.setElements(elements);
    }

    public void setElements(RecordVO... elements){
        this.currentSchema = PrintableReportListPossibleView.getValue(elements[0].getSchema().getTypeCode().toUpperCase());
        this.elements = elements;
    }

    private MetadataSchema getSchemaFromRecords() {
        MetadataSchema metadataSchema = null;
        if(this.elements != null && this.elements.length >= 1) {
            metadataSchema = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection).getSchema(this.elements[0].getSchema().getCode());
        }
        return metadataSchema;
    }

	@Override
	protected Component buildWindowContent() {
		try {
			this.getWindow().setResizable(true);
			layout = new VerticalLayout();
			setupCopieFields();
			setupPrintableReportTemplateSelection();
			return new ReportGeneratorButtonForm(new LabelParametersVO(new LabelTemplate()), this, copiesField,
					printableItemsFields);
		} catch (Exception e) {
			this.view.showErrorMessage($("ReportGeneratorButton.noReportConfigured"));
		}
		return null;
	}

	private List<PrintableReportTemplate> getPrintableReportTemplate(){
		return ReportGeneratorUtils.getPrintableReportTemplate(factory, collection, getSchemaFromRecords().getCode(), currentSchema);
	}private void setupPrintableReportTemplateSelection()
			throws Exception {
		printableItemsFields = new ComboBox();
		List<PrintableReportTemplate> printableReportTemplateList = getPrintableReportTemplate();
		if (printableReportTemplateList.size() > 0) {
			for (PrintableReportTemplate printableReportTemplate : getPrintableReportTemplate()) {
				printableItemsFields.addItem(printableReportTemplate);
				printableItemsFields.setItemCaption(printableReportTemplate, printableReportTemplate.getTitle());
			}
		} else {
			throw new Exception("No report generated");
		}
	}

    private void setupCopieFields() {
        copiesField = new TextField($("LabelsButton.numberOfCopies"));
        copiesField.setRequired(true);
        copiesField.setConverter(Integer.class);
    }

    private class ReportGeneratorButtonForm extends BaseForm<LabelParametersVO> {
        private ReportGeneratorButton parent;

        public ReportGeneratorButtonForm(LabelParametersVO viewObject, Serializable objectWithMemberFields, Field... fields) {
            super(viewObject, objectWithMemberFields, fields);
            parent = ReportGeneratorButton.this;
        }

		@Override
		protected void saveButtonClick(LabelParametersVO viewObject)
				throws ValidationException {
			getWindow().setContent(ReportGeneratorUtils.saveButtonClick(parent.factory, parent.collection, elements[0].getSchema().getCode(), (PrintableReportTemplate) parent.printableItemsFields.getValue(), (Integer) parent.copiesField.getConvertedValue(), getIdsFromRecordVO()));
		}

        @Override
        protected void cancelButtonClick(LabelParametersVO viewObject) {

        }

        private List<String> getIdsFromRecordVO(){
            List<String> ids = new ArrayList<>();
            for(RecordVO recordVO : parent.elements) {
                ids.add(recordVO.getId());
            }
            return ids;
        }
    }
}
