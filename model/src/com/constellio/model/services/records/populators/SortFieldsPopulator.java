package com.constellio.model.services.records.populators;

import static java.util.Collections.singletonMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.entities.schemas.sort.StringSortFieldNormalizer;
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

		if(metadata.getType() == MetadataValueType.REFERENCE && metadata.isSortable() && !metadata.isMultivalue() && value != null) {
			try {
				Record referencedRecord = modelLayerFactory.newRecordServices().getDocumentById((String) value);
				String captionForRecord = modelLayerFactory.getExtensions().forCollection(metadata.getCollection()).getCaptionForRecord(referencedRecord);

				String sortDataStoreCode = Schemas.getSortMetadata(metadata).getDataStoreCode();
				if(!metadata.getDataStoreCode().equals(sortDataStoreCode)) {
					return singletonMap(sortDataStoreCode, (Object) captionForRecord);
				} else {
					return Collections.emptyMap();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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
			return singletonMap(sortField.getDataStoreCode(), normalizedValue);

		} else {
			return Collections.emptyMap();
		}
	}
}
