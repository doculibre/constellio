package com.constellio.app.ui.framework.buttons.report;

import com.constellio.app.modules.rm.model.PrintableReport.PrintableReportTemplate;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.services.reports.printable.PrintableExtension;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.management.Report.PrintableReportListPossibleType;
import com.constellio.app.utils.ReportGeneratorUtils;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationException;
import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

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
	private PrintableReportListPossibleType currentSchema;
	private BaseView view;

	public ReportGeneratorButton(String caption, String windowCaption, BaseView view, AppLayerFactory factory,
								 String collection,
								 PrintableReportListPossibleType currentSchema) {
		super(caption, windowCaption, new WindowConfiguration(true, true, "75%", "90%"));
		this.factory = factory;
		this.collection = collection;
		this.currentSchema = currentSchema;
		this.view = view;
	}

	public ReportGeneratorButton(String caption, String windowCaption, BaseView view, AppLayerFactory factory,
								 String collection,
								 PrintableReportListPossibleType currentSchema, RecordVO... elements) {
		this(caption, windowCaption, view, factory, collection, currentSchema);
		this.setElements(elements);
	}

	public void setElements(RecordVO... elements) {
		if (this.currentSchema == null) {
			this.currentSchema = PrintableReportListPossibleType.getValue(elements[0].getSchema().getTypeCode().toUpperCase());
		}
		this.elements = elements;
	}

	@Override
	public boolean isVisible() {
		return !ReportGeneratorUtils
				.getPrintableReportTemplate(factory, collection, getSchemaFromRecords().getCode(), currentSchema).isEmpty();
	}

	private MetadataSchema getSchemaFromRecords() {
		MetadataSchema metadataSchema = null;
		if (this.elements != null && this.elements.length >= 1) {
			metadataSchema = factory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
					.getSchema(this.elements[0].getSchemaCode());
		}
		return metadataSchema;
	}

	@Override
	protected Component buildWindowContent() {
		try {
			this.getWindow().setResizable(true);
			layout = new VerticalLayout();
			//setupCopieFields();
			setupPrintableReportTemplateSelection();
			return new ReportGeneratorButtonForm(new LabelParametersVO(new LabelTemplate()), this,
					printableItemsFields);
		} catch (Exception e) {
			this.view.showErrorMessage($("ReportGeneratorButton.noReportConfigured"));
		}
		return null;
	}

	private List<PrintableReportTemplate> getPrintableReportTemplate() {
		return ReportGeneratorUtils
				.getPrintableReportTemplate(factory, collection, getSchemaFromRecords().getCode(), currentSchema);
	}

	private void setupPrintableReportTemplateSelection()
			throws Exception {
		printableItemsFields = new BaseComboBox();
		List<PrintableReportTemplate> printableReportTemplateList = getPrintableReportTemplate();
		if (printableReportTemplateList.size() > 0) {
			boolean first = true;
			for (PrintableReportTemplate printableReportTemplate : getPrintableReportTemplate()) {
				printableItemsFields.addItem(printableReportTemplate);
				printableItemsFields.setItemCaption(printableReportTemplate, printableReportTemplate.getTitle());
				if (first) {
					printableItemsFields.setValue(printableReportTemplate);
					first = false;
				}
			}
		} else {
			throw new Exception("No report generated");
		}
		printableItemsFields.setCaption($("ReportTabButton.selectTemplate"));
		printableItemsFields.addValidator(new Validator() {
			@Override
			public void validate(Object value)
					throws InvalidValueException {
				if (value == null) {
					throw new InvalidValueException($("ReportTabButton.invalidChoosenReport"));
				}
			}
		});
		printableItemsFields.requestRepaint();
		printableItemsFields.setNullSelectionAllowed(false);
		printableItemsFields.setValue(printableReportTemplateList.get(0));
	}

	private void setupCopieFields() {
		copiesField = new BaseTextField($("LabelsButton.numberOfCopies"));
		copiesField.setRequired(true);
		copiesField.setConverter(Integer.class);
	}

	private class ReportGeneratorButtonForm extends BaseForm<LabelParametersVO> {
		private ReportGeneratorButton parent;

		public ReportGeneratorButtonForm(LabelParametersVO viewObject, Serializable objectWithMemberFields,
										 Field... fields) {
			super(viewObject, objectWithMemberFields, fields);
			parent = ReportGeneratorButton.this;
		}

		@Override
		protected void saveButtonClick(LabelParametersVO viewObject)
				throws ValidationException {
			getWindow().setContent(
					ReportGeneratorUtils.saveButtonClick(parent.factory, parent.collection, elements[0].getSchema().getTypeCode(),
							(PrintableReportTemplate) parent.printableItemsFields.getValue(), 1, getIdsFromRecordVO(), null,
							view.getSessionContext().getCurrentLocale(), view.getSessionContext().getCurrentUser(), PrintableExtension.PDF));
		}

		@Override
		protected void cancelButtonClick(LabelParametersVO viewObject) {
			getWindow().close();
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
