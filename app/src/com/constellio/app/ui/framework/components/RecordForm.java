/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.framework.components;

import static com.constellio.app.ui.i18n.i18n.$;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.items.RecordVOItem;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.vaadin.data.Item;
import com.vaadin.ui.Field;

@SuppressWarnings("serial")
public abstract class RecordForm extends BaseForm<RecordVO> {

	public static final String STYLE_FIELD = "metadata-field";

	public RecordForm(RecordVO record) {
		this(record, new MetadataFieldFactory());
	}

	public RecordForm(final RecordVO recordVO, MetadataFieldFactory metadataFieldFactory) {
		this(recordVO, new RecordFieldFactory(metadataFieldFactory));
	}

	public RecordForm(final RecordVO recordVO, RecordFieldFactory formFieldFactory) {
		super(recordVO, buildFields(recordVO, formFieldFactory));
	}

	private static List<FieldAndPropertyId> buildFields(RecordVO recordVO, RecordFieldFactory formFieldFactory) {
		List<FieldAndPropertyId> fieldsAndPropertyIds = new ArrayList<FieldAndPropertyId>();
		for (MetadataVO metadataVO : recordVO.getMetadatas()) {
			Field<?> field = formFieldFactory.build(recordVO, metadataVO);
			if (field != null) {
				field.addStyleName(STYLE_FIELD);
				field.addStyleName(STYLE_FIELD + "-" + metadataVO.getCode());
				fieldsAndPropertyIds.add(new FieldAndPropertyId(field, metadataVO));
			}
		}
		return fieldsAndPropertyIds;
	}

	@Override
	protected Item newItem(RecordVO viewObject) {
		return new RecordVOItem(viewObject);
	}

	protected List<MetadataVO> getMetadataList() {
		return getViewObject().getMetadatas();
	}

	protected Field<?> getField(String metadataCode) {
		MetadataVO metadata = getViewObject().getMetadataOrNull(metadataCode);
		return metadata != null ? getField(metadata) : null;
	}

	protected Field<?> getField(MetadataVO metadata) {
		Field<?> match = null;
		for (Field<?> field : fields) {
			Object propertyId = fieldGroup.getPropertyId(field);
			if (metadata.equals(propertyId)) {
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
			String metadataCode = validationError.getParameters()
					.get(com.constellio.model.frameworks.validation.Validator.METADATA_CODE);
			if (metadataCode != null) {
				MetadataVO metadata = viewObject.getMetadataOrNull(metadataCode);
				if (metadata != null) {
					Field<?> field = getField(metadata);
					if (field != null) {
						// We add this validator so that an error message appears next to the field
						field.addValidator(new BackendValidator(errorMessage));
						if (firstFieldWithError == null) {
							firstFieldWithError = field;
						}
					} else {
						globalErrorMessages.add(errorMessage);
					}
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
