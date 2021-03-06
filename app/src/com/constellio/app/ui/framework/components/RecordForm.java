package com.constellio.app.ui.framework.components;

import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.buttons.IconButton;
import com.constellio.app.ui.framework.components.layouts.I18NHorizontalLayout;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.app.ui.pages.base.SchemaPresenterUtils;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.SchemaVOUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.data.Item;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class RecordForm extends BaseForm<RecordVO> {

	public static final String STYLE_FIELD = "metadata-field";

	protected RecordVO recordVO;
	private RecordFieldFactory formFieldFactory;
	private Map<Field<?>, Layout> fieldLayoutMap;
	private Map<Field<?>, Object> fieldValue;
	private SchemaPresenterUtils schemaPresenterUtils;

	public RecordForm(RecordVO record, ConstellioFactories constellioFactories) {
		this(record, new MetadataFieldFactory(), constellioFactories);
	}

	public RecordForm(final RecordVO recordVO, MetadataFieldFactory metadataFieldFactory,
					  ConstellioFactories constellioFactories) {
		this(recordVO, new RecordFieldFactory(metadataFieldFactory), constellioFactories);
	}

	public RecordForm(final RecordVO recordVO, List<FieldAndPropertyId> fieldsAndPropertyIds,
					  RecordFieldFactory formFieldFactory, ConstellioFactories constellioFactories) {
		super(recordVO, fieldsAndPropertyIds);
		this.formFieldFactory = formFieldFactory;

		for (FieldAndPropertyId fieldAndPropertyId : fieldsAndPropertyIds) {
			addFieldValueToMap(fieldAndPropertyId.field, fieldAndPropertyId.field.getValue());
		}
		this.recordVO = recordVO;
		this.schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchemaCode(), constellioFactories, ConstellioUI.getCurrentSessionContext());
	}

	public RecordForm(final RecordVO recordVO, RecordFieldFactory formFieldFactory,
					  ConstellioFactories constellioFactories) {
		super(recordVO, buildFields(recordVO, formFieldFactory));
		this.formFieldFactory = formFieldFactory;

		for (Field field : getFields()) {
			addFieldValueToMap(field, field.getValue());
		}

		this.recordVO = recordVO;
		this.schemaPresenterUtils = new SchemaPresenterUtils(recordVO.getSchemaCode(), constellioFactories, ConstellioUI.getCurrentSessionContext());
	}

	public Layout getFieldLayout(Field<?> field) {
		return fieldLayoutMap.get(field);
	}

	private static List<FieldAndPropertyId> buildFields(RecordVO recordVO, RecordFieldFactory formFieldFactory) {
		List<FieldAndPropertyId> fieldsAndPropertyIds = new ArrayList<>();
		List<MetadataVO> hiddenFields = recordVO.getFormHiddenMetadatas();

		for (MetadataVO metadataVO : recordVO.getFormMetadatas()) {
			if (recordVO.getMetadataCodes().contains(metadataVO.getCode())) {
				Field<?> field = formFieldFactory.build(recordVO, metadataVO);
				if (field != null) {
					if (!isVisibleField(metadataVO, recordVO)
						|| !SchemaVOUtils.isMetadataNotPresentInList(metadataVO, recordVO.getExcludedMetadataCodeList())) {
						field.setVisible(false);
					}
					if (hiddenFields.contains(metadataVO)) {
						metadataVO.setForceHidden(true);
					}
					if (metadataVO.isUnmodifiable() && recordVO.isSaved()) {
						field.setReadOnly(true);
					}
					field.addStyleName(STYLE_FIELD);
					field.addStyleName(STYLE_FIELD + "-" + metadataVO.getCode());
					fieldsAndPropertyIds.add(new FieldAndPropertyId(field, metadataVO));
				}
			}
		}

		return fieldsAndPropertyIds;
	}

	@Override
	protected void addFieldToLayout(Field<?> field, VerticalLayout fieldLayout) {
		Object propertyId = fieldGroup.getPropertyId(field);

		Layout wrappedField = wrapFieldWithHelpMessage(((MetadataVO) propertyId), field);
		addFieldLayoutToMap(field, wrappedField);

		if (((MetadataVO) propertyId).isForceHidden()) {
			hiddenLayout.addComponent(wrappedField);
		} else {
			fieldLayout.addComponent(wrappedField);
		}
	}

	public SaveAction showConfirmationMessage() {
		try {
			fieldGroup.commit();
		} catch (FieldGroup.CommitException e) {
			return SaveAction.undefined;
		}
		if (recordVO.isSaved()) {
			extraActionBeforeComparingOldAndNewRecord(recordVO);

			Record record = recordVO.getRecord().getCopyOfOriginalRecord();
			fillRecordUsingRecordVO(record, recordVO, true);

			ForceCancelSaveOfFormParams forceCancelSaveOfFormParams = new ForceCancelSaveOfFormParams(record);
			forceCancelSaveOfForm(forceCancelSaveOfFormParams);

			if (!forceCancelSaveOfFormParams.isForceCancelSave() && isDirty(record)) {
				return SaveAction.save;
			} else {
				return SaveAction.cancelSave;
			}
		}

		return SaveAction.undefined;
	}

	protected boolean isDirty(Record record) {
		return record.isDirty() ||
			   getFields().stream()
					   .anyMatch(field -> field instanceof Dirtyable && ((Dirtyable) field).isDirty());
	}

	public void forceCancelSaveOfForm(ForceCancelSaveOfFormParams forceCancelSaveOfFormParams) {

	}

	@Getter
	public class ForceCancelSaveOfFormParams {
		private Record record;
		private boolean forceCancelSave;

		public ForceCancelSaveOfFormParams(Record record) {
			this.record = record;
			this.forceCancelSave = false;
		}

		public void doNotShowConfirmationMessage() {
			this.forceCancelSave = true;
		}
	}

	public void extraActionBeforeComparingOldAndNewRecord(RecordVO recordVO) {

	}

	private void addFieldValueToMap(Field<?> field, Object value) {
		if (fieldValue == null) {
			fieldValue = new HashMap<>();
		}

		fieldValue.put(field, value);
	}

	private void addFieldLayoutToMap(Field<?> field, Layout wrappedField) {
		if (fieldLayoutMap == null) {
			fieldLayoutMap = new HashMap<>();
		}

		fieldLayoutMap.put(field, wrappedField);
	}


	private Layout wrapFieldWithHelpMessage(MetadataVO metadataVO, Field<?> field) {
		I18NHorizontalLayout layout = new I18NHorizontalLayout();
		String metadataHelp = metadataVO.getHelpMessage(ConstellioUI.getCurrentSessionContext().getCurrentLocale());
		IconButton helpIcon = new MouseOverHelpIcon(metadataHelp);

		helpIcon.setVisible(metadataHelp != null ? !metadataHelp.isEmpty() : false);

		layout.setSpacing(false);
		layout.setWidth("100%");
		layout.setHeight("100%");
		layout.addComponents(field, helpIcon);
		layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
		layout.setExpandRatio(field, 1);

		layout.setVisible(field.isVisible());

		return layout;
	}

	private static boolean isVisibleField(MetadataVO metadataVO, RecordVO recordVO) {
		return ConstellioFactories.getInstance().getAppLayerFactory().getExtensions()
				.forCollection(recordVO.getSchema().getCollection())
				.isMetadataEnabledInRecordForm(recordVO, metadataVO);
	}

	protected RecordFieldFactory getFormFieldFactory() {
		return formFieldFactory;
	}

	@Override
	protected Item newItem(RecordVO viewObject) {
		return new RecordVOItem(viewObject);
	}

	protected List<MetadataVO> getMetadataList() {
		return getViewObject().getMetadatas();
	}

	public Field<?> getField(String metadataCode) {
		MetadataVO metadata = getViewObject().getMetadataOrNull(metadataCode);
		return metadata != null ? getField(metadata) : null;
	}

	protected Field<?> getField(MetadataVO metadata) {
		Field<?> match = null;
		for (Field<?> field : fields) {
			Object propertyId = fieldGroup.getPropertyId(field);
			if (metadata.isSameLocalCode(propertyId)) {
				match = field;
				break;
			}
		}
		return match;
	}

	@Override
	protected String getTabCaption(Field<?> field, Object propertyId) {
		String tabCaption;
		if (propertyId instanceof MetadataVO) {
			MetadataVO metadataVO = (MetadataVO) propertyId;
			tabCaption = metadataVO.getMetadataGroup();
		} else {
			tabCaption = null;
		}
		return tabCaption;
	}

	@Override
	protected String getSchemaCode(Object propertyId) {
		if (propertyId instanceof MetadataVO) {
			MetadataVO metadataVO = (MetadataVO) propertyId;
			return metadataVO.getSchema().getCode();
		}
		return null;
	}

	@Override
	protected List<String> getOrderedTabCaptions(RecordVO recordVO) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		Locale currentLocale = sessionContext.getCurrentLocale();

		MetadataSchemaVO schemaVO = recordVO.getSchema();
		String collection = schemaVO.getCollection();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaVO.getCode());
		List<String> orderedTabCaptions = new ArrayList<>();
		SchemasDisplayManager displayManager = ConstellioFactories.getInstance().getAppLayerFactory().getMetadataSchemasDisplayManager();
		SchemaTypeDisplayConfig typeConfig = displayManager.getType(collection, schemaTypeCode);
		Map<String, Map<Language, String>> groups = typeConfig.getMetadataGroup();
		for (String group : groups.keySet()) {
			Map<Language, String> groupLabels = groups.get(group);
			for (Language language : groupLabels.keySet()) {
				if (language.getLocale().equals(currentLocale)) {
					String tabCaption = groupLabels.get(language);
					if (!orderedTabCaptions.contains(tabCaption)) {
						orderedTabCaptions.add(tabCaption);
					}
				}
			}
		}
		return orderedTabCaptions;
	}

	@Override
	protected void showBackendValidationException(ValidationErrors validationErrors) {
		Set<String> globalErrorMessages = new HashSet<String>();
		Field<?> firstFieldWithError = null;
		for (ValidationError validationError : validationErrors.getValidationErrors()) {
			String errorMessage = $(validationError);
			Object metadataCode = validationError.getParameters()
					.get(com.constellio.model.frameworks.validation.Validator.METADATA_CODE);
			if (metadataCode != null) {
				MetadataVO metadata = viewObject.getMetadataOrNull((String) metadataCode);
				if (metadata != null) {
					Field<?> field = getField(metadata);
					if (field != null) {
						// We add this validator so that an error message appears next to the field
						field.addValidator(new BackendValidator(errorMessage));
						if (firstFieldWithError == null) {
							firstFieldWithError = field;
						}
					}
					globalErrorMessages.add(errorMessage);
				} else {
					globalErrorMessages.add(errorMessage);
				}
			} else {
				globalErrorMessages.add(errorMessage);
			}
		}

		if (!globalErrorMessages.isEmpty()) {
			StringBuilder globalErrorMessagesSB = new StringBuilder();
			for (String globalErrorMessage : globalErrorMessages) {
				if (globalErrorMessagesSB.length() != 0) {
					globalErrorMessagesSB.append("<br />");
				}
				globalErrorMessagesSB.append(globalErrorMessage);
			}
			showErrorMessage(globalErrorMessagesSB.toString());
		} else if (firstFieldWithError != null) {
			firstFieldWithError.focus();
		}
	}

	@Override
	protected boolean isActivatedByConfigAndNeedConfirmation() {
		if (ConstellioFactories.getInstance().getAppLayerFactory().getModelLayerFactory().getSystemConfigs()
					.isAskForConfirmationBeforeDeleteOrEdit()
			&& this.getViewObject().isSaved()) {
			return true;
		} else {
			return false;
		}
	}

	protected final void fillRecordUsingRecordVO(Record record, RecordVO recordVO, boolean newMinorEmpty) {
		schemaPresenterUtils.fillRecordUsingRecordVO(record, recordVO, newMinorEmpty);
	}
}
