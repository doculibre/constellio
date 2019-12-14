package com.constellio.model.services.records.populators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.sort.StringSortFieldNormalizer;
import com.constellio.model.extensions.params.GetCaptionForRecordParams;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.FieldsPopulator;
import com.constellio.model.services.records.RecordProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SortFieldsPopulator extends SeparatedFieldsPopulator implements FieldsPopulator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SortFieldsPopulator.class);
	ModelLayerFactory modelLayerFactory;
	RecordProvider recordProvider;

	public SortFieldsPopulator(MetadataSchemaTypes types, boolean fullRewrite, ModelLayerFactory modelFactory,
							   RecordProvider recordProvider) {
		super(types, fullRewrite);
		this.modelLayerFactory = modelFactory;
		this.recordProvider = recordProvider;
	}

	@Override
	protected boolean isPopulatedForAllLocales(Metadata metadata) {
		if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isSortable() && !metadata.isMultivalue()) {
			MetadataSchemaType targettedSchemaType = types.getSchemaType(metadata.getReferencedSchemaType());
			return targettedSchemaType.getDefaultSchema().hasMultilingualMetadatas();
		} else {
			return metadata.isMultiLingual() || metadata.isSameLocalCode(Schemas.CAPTION);
		}

	}

	@Override
	public Map<String, Object> populateCopyfields(Metadata metadata, Object value, Locale locale) {

		Map<String, Object> fields = new HashMap<>();

		if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isSortable() && !metadata.isMultivalue()
			&& value != null) {
			try {
				Record referencedRecord = recordProvider.getRecord((String) value);

				if (referencedRecord != null) {
					String captionForRecord = modelLayerFactory.getExtensions().forCollection(metadata.getCollection())
							.getCaptionForRecord(new GetCaptionForRecordParams(referencedRecord, types, locale));

					if (captionForRecord == null) {
						LOGGER.warn("Record '" + referencedRecord.getSchemaIdTitle() + "' has no caption");
					} else {
						Metadata sortMetadata = metadata.getSortMetadata();
						if (!locale.equals(types.getCollectionInfo().getMainSystemLocale())) {
							sortMetadata = sortMetadata.getSecondaryLanguageField(locale.getLanguage());
						}

						if (!metadata.getDataStoreCode().equals(sortMetadata.getDataStoreCode())) {
							populateNormalizedValue(metadata, sortMetadata, captionForRecord, fields);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Metadata sortMetadata = metadata.getSortMetadata();
			if (!locale.equals(types.getCollectionInfo().getMainSystemLocale())) {
				sortMetadata = sortMetadata.getSecondaryLanguageField(locale.getLanguage());
			}

			populateNormalizedValue(metadata, sortMetadata, value, fields);
		}

		return fields;
	}

	private void populateNormalizedValue(Metadata metadata, Metadata sortMetadata,
								  Object value, Map<String, Object> fields) {
		StringSortFieldNormalizer normalizer = metadata.getSortFieldNormalizer();
		if (normalizer != null) {
			Object normalizedValue;
			if (value == null) {
				normalizedValue = normalizer.normalizeNull();
			} else {
				normalizedValue = normalizer.normalize((String) value);
			}
			if (normalizedValue == null) {
				normalizedValue = "";
			}
			fields.put(sortMetadata.getDataStoreCode(), normalizedValue);
		}
	}
}
