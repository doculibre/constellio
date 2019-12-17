package com.constellio.model.services.records.populators;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.FieldsPopulator;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.PREFERRING;

public abstract class SeparatedFieldsPopulator implements FieldsPopulator {

	boolean fullRewrite;
	MetadataSchemaTypes types;

	protected SeparatedFieldsPopulator(
			MetadataSchemaTypes types, boolean fullRewrite) {
		this.types = types;
		this.fullRewrite = fullRewrite;
	}

	@Override
	public Map<String, Object> populateCopyfields(MetadataSchema schema, Record record) {
		Map<String, Object> fields = new HashMap<>();

		List<Metadata> metadatas = record.getModifiedMetadatas(types);
		if (fullRewrite) {
			metadatas = schema.getMetadatas();
		}

		for (Metadata modifiedMetadata : metadatas) {
			Object mainLanguageValue = record.get(modifiedMetadata);
			populateCopyFields(fields, modifiedMetadata, mainLanguageValue, record.getCollectionInfo().getMainSystemLocale());

			if (isPopulatedForAllLocales(modifiedMetadata)) {
				for (String secondarySystemLanguageCode : record.getCollectionInfo().getSecondaryCollectionLanguesCodes()) {
					Locale secondarySystemLocale = Language.withCode(secondarySystemLanguageCode).getLocale();
					Object secondaryLanguageValue = record.get(modifiedMetadata, secondarySystemLocale, PREFERRING);
					populateCopyFields(fields, modifiedMetadata, secondaryLanguageValue, secondarySystemLocale);
				}
			}
		}

		return fields;
	}

	protected boolean isPopulatedForAllLocales(Metadata metadata) {
		return metadata.isMultiLingual();
	}

	private void populateCopyFields(Map<String, Object> fields, Metadata modifiedMetadata, Object value,
									Locale locale) {
		Map<String, Object> values = populateCopyfields(modifiedMetadata, value, locale);
		for (Map.Entry<String, Object> entry : values.entrySet()) {
			//			if (entry.getValue() != null) {
			fields.put(entry.getKey(), entry.getValue());
			//			} else {
			//throw new RecordImplException_PopulatorReturnedNullValue(this, entry.getKey());
			//			}

		}
	}

	abstract Map<String, Object> populateCopyfields(Metadata metadata, Object value, Locale locale);
}
