package com.constellio.app.ui.framework.buttons.report;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.reports.JasperPdfGenerator;
import com.constellio.app.modules.rm.services.reports.XmlReportGenerator;
import com.constellio.app.modules.rm.services.reports.parameters.XmlReportGeneratorParameters;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleView;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Created by Marco on 2017-07-10.
 */
public class ReportGeneratorButton extends WindowButton {
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

	public ReportGeneratorButton(String caption, String windowCaption, BaseView view, AppLayerFactory factory, String collection,
			PrintableReportListPossibleView currentSchema) {
		super(caption, windowCaption);
		this.factory = factory;
		this.collection = collection;
		this.currentSchema = currentSchema;
		this.view = view;
	}

	public ReportGeneratorButton(String caption, String windowCaption, BaseView view, AppLayerFactory factory, String collection,
			PrintableReportListPossibleView currentSchema, RecordVO... elements) {
		this(caption, windowCaption, view, factory, collection, currentSchema);
		this.setElements(elements);
	}

	public void setElements(RecordVO... elements) {
		this.currentSchema = PrintableReportListPossibleView.getValue(elements[0].getSchema().getTypeCode());
		this.elements = elements;
	}

	private MetadataSchema getSchemaFromRecords() {
		MetadataSchema metadataSchema = null;
		if (this.elements != null && this.elements.length >= 1) {
			metadataSchema = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
					.getSchema(this.elements[0].getSchema().getCode());
		}
		return metadataSchema;
	}

	private List<PrintableReportTemplate> getPrintableReportTemplate() {
		List<PrintableReportTemplate> printableReportTemplateList = new ArrayList<>();
		MetadataSchemasManager metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchema printableReportSchemaType = metadataSchemasManager.getSchemaTypes(collection)
				.getSchemaType(Printable.SCHEMA_TYPE).getCustomSchema(PrintableReport.SCHEMA_NAME);
		LogicalSearchCondition conditionCustomSchema = from(printableReportSchemaType)
				.where(printableReportSchemaType.get(PrintableReport.REPORT_SCHEMA)).isEqualTo(getSchemaFromRecords().getCode());
		LogicalSearchCondition conditionSchemaType = from(printableReportSchemaType)
				.where(printableReportSchemaType.getMetadata(PrintableReport.REPORT_TYPE)).isEqualTo(currentSchema.toString());
		List<Record> records = factory.getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery(
				from(printableReportSchemaType).whereAllConditions(conditionCustomSchema, conditionSchemaType)));
		for (Record record : records) {
			printableReportTemplateList.add(new PrintableReportTemplate(record.getId(), record.getTitle(),
					record.<Content>get(printableReportSchemaType.getMetadata(PrintableReport.JASPERFILE))));
		}
		return printableReportTemplateList;
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

	private void setupPrintableReportTemplateSelection()
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
			try {
				IOServicesFactory ioServicesFactory = parent.factory.getModelLayerFactory().getIOServicesFactory();
				ContentManager contentManager = parent.factory.getModelLayerFactory().getContentManager();
				XmlReportGeneratorParameters xmlGeneratorParameters = new XmlReportGeneratorParameters(
						(Integer) parent.copiesField.getConvertedValue());
				xmlGeneratorParameters.setElementWithIds(elements[0].getSchema().getCode(), getIdsFromRecordVO());
				XmlReportGenerator xmlReportGenerator = new XmlReportGenerator(parent.factory, parent.collection,
						xmlGeneratorParameters);
				JasperPdfGenerator jasperPdfGenerator = new JasperPdfGenerator(xmlReportGenerator);
				PrintableReportTemplate value = (PrintableReportTemplate) parent.printableItemsFields.getValue();
				InputStream inputStream = contentManager
						.getContentInputStream(value.getJasperFile().getCurrentVersion().getHash(),
								value.getJasperFile().getId());
				File jasperFile = ioServicesFactory.newIOServices().newTemporaryFile("jasper.jasper");
				FileUtils.copyInputStreamToFile(inputStream, jasperFile);
				String title =
						elements[0].getSchema().getCode() + "_report_" + ISODateTimeFormat.dateTime().print(new LocalDateTime())
								+ ".pdf";
				File generatedJasperFile = jasperPdfGenerator.createPDFFromXmlAndJasperFile(jasperFile, title);
				VerticalLayout newLayout = new VerticalLayout();
				newLayout.addComponents(new LabelViewer(generatedJasperFile, title));
				newLayout.setWidth("100%");
				getWindow().setContent(newLayout);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void cancelButtonClick(LabelParametersVO viewObject) {

		}

		private List<String> getIdsFromRecordVO() {
			List<String> ids = new ArrayList<>();
			for (RecordVO recordVO : parent.elements) {
				ids.add(recordVO.getId());
			}
			return ids;
		}
	}
}
