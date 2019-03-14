package com.constellio.app.ui.framework.components;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.vaadin.data.Item;
import com.vaadin.ui.Field;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
		for (MetadataVO metadataVO : recordVO.getFormMetadatas()) {
			if(recordVO.getMetadataCodes().contains(metadataVO.getCode())) {

				Field<?> field = formFieldFactory.build(recordVO, metadataVO);
				if (field != null) {
					if (!isVisibleField(metadataVO, recordVO)) {
						field.setVisible(false);
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
