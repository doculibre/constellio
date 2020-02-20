package com.constellio.app.services.extensions;

import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationBeforeSaveEvent;
import com.constellio.model.extensions.events.records.RecordInModificationBeforeSaveEvent;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.model.frameworks.validation.Validator.METADATA_CODE;
import static com.constellio.model.frameworks.validation.Validator.METADATA_LABEL;
import static com.constellio.model.frameworks.validation.Validator.RECORD;

public class AppRecordExtension extends RecordExtension {
	public static final String METADATA_VALUE_DOESNT_RESPECT_MAX_LENGTH = "metadataValueDoesntRespectMaxLength";

	private AppLayerFactory appLayerFactory;
	private String collection;
	private SchemasDisplayManager schemasDisplayManager;

	public AppRecordExtension(AppLayerFactory appLayerFactory, String collection) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		;
	}

	@Override
	public void recordInCreationBeforeSave(RecordInCreationBeforeSaveEvent event) {
		Record recordToValidate = event.getRecord();

		recordMetadataWithMaxlenghtValidation(event.getValidationErrors(), recordToValidate);
	}

	@Override
	public void recordInModificationBeforeSave(RecordInModificationBeforeSaveEvent event) {
		Record recordToValidate = event.getRecord();

		recordMetadataWithMaxlenghtValidation(event.getValidationErrors(), recordToValidate);
	}

	private void recordMetadataWithMaxlenghtValidation(ValidationErrors validationErrors,
													   Record recordToValidate) {
		MetadataSchemasManager metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();

		MetadataSchema metadataSchema = metadataSchemasManager.getSchemaTypes(collection).getSchema(recordToValidate.getSchemaCode());

		for (Metadata currentMetadata : metadataSchema.getMetadatas()) {
			if (currentMetadata.getMaxLength() != null) {
				MetadataDisplayConfig metadataDisplayConfig = schemasDisplayManager.getMetadata(collection, currentMetadata.getCode());
				if (metadataDisplayConfig.getInputType() != MetadataInputType.RICHTEXT) {
					Object value = recordToValidate.get(currentMetadata);
					if (currentMetadata.isMultivalue()) {
						for (String currentValue : (Collection<String>) value) {
							if (StringUtils.isNotBlank(currentValue) && currentMetadata.getMaxLength() < currentValue.length()) {
								addMaxLenghtValidationErrors(recordToValidate.getId(), validationErrors, METADATA_VALUE_DOESNT_RESPECT_MAX_LENGTH, currentMetadata);
							}
						}
					} else {
						String currentValue = (String) value;

						if (StringUtils.isNotBlank(currentValue) && currentMetadata.getMaxLength() < currentValue.length()) {
							addMaxLenghtValidationErrors(recordToValidate.getId(), validationErrors, METADATA_VALUE_DOESNT_RESPECT_MAX_LENGTH, currentMetadata);
						}
					}
				}
			}
		}
	}

	private void addMaxLenghtValidationErrors(String recordId, ValidationErrors validationErrors, String errorCode,
											  Metadata metadata) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(RECORD, recordId);
		parameters.put(METADATA_CODE, metadata.getCode());
		parameters.put(METADATA_LABEL, metadata.getLabelsByLanguageCodes());
		parameters.put("maxLength", metadata.getMaxLength());

		validationErrors.add(getClass(), errorCode, parameters);
	}
}
