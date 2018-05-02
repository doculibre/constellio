package com.constellio.model.services.records.populators;

import static com.constellio.model.entities.schemas.Schemas.getSortMetadata;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.sort.StringSortFieldNormalizer;
import com.constellio.model.extensions.params.GetCaptionForRecordParams;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.FieldsPopulator;
import com.constellio.model.services.records.RecordProvider;

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
	public Map<String, Object> populateCopyfields(Metadata metadata, Object value) {

		Map<String, Object> fields = new HashMap<>();

		if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isSortable() && !metadata.isMultivalue()
				&& value != null) {
			try {
				Record referencedRecord = recordProvider.getRecord((String) value);

				if (referencedRecord != null) {
					for (Locale locale : referencedRecord.getCollectionInfo().getCollectionLocales()) {
						String captionForRecord = modelLayerFactory.getExtensions().forCollection(metadata.getCollection())
								.getCaptionForRecord(new GetCaptionForRecordParams(referencedRecord, types, locale));

						if (captionForRecord == null) {
							LOGGER.warn("Record '" + referencedRecord.getSchemaIdTitle() + "' has no caption");
						} else {
							Metadata sortMetadata = getSortMetadata(metadata);
							if (!locale.equals(referencedRecord.getCollectionInfo().getMainSystemLocale())) {
								sortMetadata = sortMetadata.getSecondaryLanguageField(locale.getLanguage());
							}

							if (!metadata.getDataStoreCode().equals(sortMetadata.getDataStoreCode())) {
								fields.put(sortMetadata.getDataStoreCode(), (Object) captionForRecord);
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			StringSortFieldNormalizer normalizer = metadata.getSortFieldNormalizer();
			Metadata sortField = metadata.getSortField();

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
				fields.put(sortField.getDataStoreCode(), normalizedValue);

			}

		}

		return fields;
	}
}
