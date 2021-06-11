package com.constellio.app.ui.framework.buttons.report;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.model.labelTemplate.LabelTemplate;
import com.constellio.app.modules.rm.reports.factories.labels.LabelsReportParameters;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.reports.JasperReportServices;
import com.constellio.app.modules.rm.services.reports.printable.PrintableGeneratorParams;
import com.constellio.app.modules.rm.services.reports.xml.XMLDataSourceType;
import com.constellio.app.modules.rm.services.reports.xml.legacy.LabelXmlGenerator;
import com.constellio.app.modules.rm.ui.components.Dimensionnable;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.LabelParametersVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.buttons.WindowButton;
import com.constellio.app.ui.framework.components.BaseForm;
import com.constellio.app.ui.framework.components.LabelViewer;
import com.constellio.app.ui.framework.components.ReportViewer;
import com.constellio.app.ui.framework.components.fields.BaseComboBox;
import com.constellio.app.ui.framework.components.fields.BaseTextField;
import com.constellio.app.ui.framework.reports.NewReportWriterFactory;
import com.constellio.app.ui.framework.reports.ReportWriter;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Factory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

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

	private UserVO userVO;

	public LabelButtonV2(String caption, String windowsCaption,
						 Factory<List<LabelTemplate>> customLabelTemplatesFactory,
						 Factory<List<LabelTemplate>> defaultLabelTemplatesFactory, AppLayerFactory factory,
						 String collection, UserVO userVO) {
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
		this.userVO = userVO;

	}

	public LabelButtonV2(String caption, String windowsCaption,
						 Factory<List<LabelTemplate>> customLabelTemplatesFactory,
						 Factory<List<LabelTemplate>> defaultLabelTemplatesFactory, AppLayerFactory factory,
						 String collection, UserVO userVO, RecordVO... elements) {
		this(caption, windowsCaption, customLabelTemplatesFactory, defaultLabelTemplatesFactory, factory, collection, userVO);
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
			RecordVO recordVO = getRecordVoFromId(id, schemaType, sessionContext);
			if (recordVO != null) {
				recordVOS.add(recordVO);
			}
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

		startPositionField = new BaseComboBox($("LabelsButton.startPosition"));
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

		formatField = new BaseComboBox($("LabelsButton.labelFormat"));
		formatField.setRequired(true);

		setItemsForFormatFields(listOfAllTemplates);

		//We don't want to put too much stuff in the buildWindowContent. So we create a new class.
		formatField.addValueChangeListener(new TemplateValueChangeListener());
	}

	private void setupCopieFields() {

		copiesField = new BaseTextField($("LabelsButton.numberOfCopies"));
		copiesField.setRequired(true);
		copiesField.setConverter(Integer.class);

		startAndCopiesLayout = new HorizontalLayout(startPositionField, copiesField);
		startAndCopiesLayout.setSpacing(true);
	}

	private List<? extends Dimensionnable> getCustomTemplateForCurrentType() {
		if (this.elements == null && this.schemaType == null) {
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
		sortListOfAllTemplates();
		for (Dimensionnable template : listOfAllTemplates) {
			formatField.addItem(template);
			//Check the captions of the current template.
			String itemCaption = template instanceof PrintableLabel ? ((PrintableLabel) template).getTitle(i18n.getLocale()) : $(((LabelTemplate) template).getName());
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
		Record record = recordServices.get(id);
		return record != null ? this.recordToVOBuilder.build(record, RecordVO.VIEW_MODE.DISPLAY, sessionContext) : null;
	}

	private void sortListOfAllTemplates() {
		Collections.sort(listOfAllTemplates, new Comparator<Dimensionnable>() {
			@Override
			public int compare(Dimensionnable o1, Dimensionnable o2) {
				String caption1 = ((PrintableLabel) o1).getTitle(i18n.getLocale());
				String caption2 = ((PrintableLabel) o2).getTitle(i18n.getLocale());
				return StringUtils.lowerCase(caption1).compareTo(StringUtils.lowerCase(caption2));
			}
		});
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
				PrintableLabel selectedTemplateAsPrintableLabel = ((PrintableLabel) selectedTemplate);
				JasperReportServices jasperReportServices = new JasperReportServices(collection, factory);
				PrintableGeneratorParams params = PrintableGeneratorParams.builder()
						.XMLDataSourceType(XMLDataSourceType.LABEL)
						.numberOfCopies(Integer.parseInt(copiesField.getValue().trim()))
						.startingPosition((Integer) startPositionField.getValue())
						.printableId(selectedTemplateAsPrintableLabel.getId())
						.recordIds(getRecordIdsFromElements(elements))
						.locale(getLocale())
						.username(userVO.getUsername())
						.build();
				try (InputStream inputStream = jasperReportServices.generatePrintable(params)) {
					String titleOfthePdfFile = LabelXmlGenerator.escapeForXmlTag(selectedTemplateAsPrintableLabel.getTitle()) + ".pdf";
					layout = new LabelViewer(inputStream, titleOfthePdfFile, factory.getModelLayerFactory().getIOServicesFactory().newIOServices());
				}
			}
			return layout;
		}

		private List<String> getRecordIdsFromElements(RecordVO... recordVOS) {
			List<String> recordIds = new ArrayList<>();
			for (RecordVO recordVO : recordVOS) {
				recordIds.add(recordVO.getId());
			}
			return recordIds;
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
