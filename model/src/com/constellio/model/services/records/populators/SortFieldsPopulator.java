package com.constellio.model.services.records.populators;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.sort.StringSortFieldNormalizer;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.FieldsPopulator;

public class SortFieldsPopulator extends SeparatedFieldsPopulator implements FieldsPopulator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SortFieldsPopulator.class);
	ModelLayerFactory modelLayerFactory;

	public SortFieldsPopulator(MetadataSchemaTypes types, boolean fullRewrite, ModelLayerFactory modelFactory) {
		super(types, fullRewrite);
		this.modelLayerFactory = modelFactory;
	}

	@Override
	public Map<String, Object> populateCopyfields(Metadata metadata, Object value) {

		Map<String, Object> fields = new HashMap<>();
		if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isSortable() && !metadata.isMultivalue()
				&& value != null) {
			try {
				Record referencedRecord = modelLayerFactory.newRecordServices().getDocumentById((String) value);
				String captionForRecord = modelLayerFactory.getExtensions().forCollection(metadata.getCollection())
						.getCaptionForRecord(referencedRecord, null);

				String sortDataStoreCode = Schemas.getSortMetadata(metadata).getDataStoreCode();
				if (!metadata.getDataStoreCode().equals(sortDataStoreCode)) {
					fields.put(sortDataStoreCode, (Object) captionForRecord);
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
