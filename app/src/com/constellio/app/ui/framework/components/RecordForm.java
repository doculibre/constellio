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
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.SchemaVOUtils;
import com.constellio.model.entities.Language;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.data.Item;
import com.vaadin.ui.Field;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.app.ui.i18n.i18n.$;

@SuppressWarnings("serial")
public abstract class RecordForm extends BaseForm<RecordVO> {

	public static final String STYLE_FIELD = "metadata-field";

	private RecordFieldFactory formFieldFactory;

	public RecordForm(RecordVO record) {
		this(record, new MetadataFieldFactory());
	}

	public RecordForm(final RecordVO recordVO, MetadataFieldFactory metadataFieldFactory) {
		this(recordVO, new RecordFieldFactory(metadataFieldFactory));
	}

	public RecordForm(final RecordVO recordVO, List<FieldAndPropertyId> fieldsAndPropertyIds,
					  RecordFieldFactory formFieldFactory) {
		super(recordVO, fieldsAndPropertyIds);
		this.formFieldFactory = formFieldFactory;
	}

	public RecordForm(final RecordVO recordVO, RecordFieldFactory formFieldFactory) {
		super(recordVO, buildFields(recordVO, formFieldFactory));
		this.formFieldFactory = formFieldFactory;
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

		if (propertyId instanceof MetadataVO) {
			Layout wrappedField = wrapFieldWithHelpMessage(((MetadataVO) propertyId), field);
			if (((MetadataVO) propertyId).isForceHidden()) {
				hiddenLayout.addComponent(wrappedField);
			} else {
				formLayout.addComponent(wrappedField);
			}
		} else {
			super.addFieldToLayout(field, fieldLayout);
		}
	}

	private Layout wrapFieldWithHelpMessage(MetadataVO metadataVO, Field<?> field) {
		I18NHorizontalLayout layout = new I18NHorizontalLayout();
		String metadataHelp = metadataVO.getHelpMessage(ConstellioUI.getCurrentSessionContext().getCurrentLocale());
		IconButton helpIcon = new MouseOverHelpIcon(metadataHelp);

		helpIcon.setVisible(!metadataHelp.isEmpty());

		layout.setSizeFull();
		layout.addComponents(field, helpIcon);
		layout.setExpandRatio(field, 1);
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
					orderedTabCaptions.add(tabCaption);
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
}
