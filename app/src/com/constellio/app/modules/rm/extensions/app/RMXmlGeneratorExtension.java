package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.XmlGeneratorExtension;
import com.constellio.app.api.extensions.params.ExtraMetadataToGenerateOnReferenceParams;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

// Use XMLDataSourceGeneratorFactory instead
@Deprecated
public class RMXmlGeneratorExtension extends XmlGeneratorExtension {
	private String collection;
	private AppLayerFactory appLayerFactory;

	public RMXmlGeneratorExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void getExtraMetadataToGenerateOnReference(
			ExtraMetadataToGenerateOnReferenceParams extraMetadataToGenerateOnReferenceParams) {

		Record record = extraMetadataToGenerateOnReferenceParams.getRecord();

		if (record.getTypeCode().equals(ContainerRecord.SCHEMA_TYPE)) {
			MetadataSchemaType schemaType = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypeOf(record);

			Metadata metadata = schemaType.getDefaultSchema().getMetadata(ContainerRecord.LOCALIZATION);
			String recordValue = record.get(metadata);

			Map<String, String> valueByMetadata = extraMetadataToGenerateOnReferenceParams.getValueByMetadata();

			String localisation = valueByMetadata.get(ContainerRecord.LOCALIZATION);

			if (StringUtils.isNotBlank(recordValue)) {
				if (localisation == null) {
					localisation = recordValue;
				} else {
					localisation += ", " + recordValue;
				}

				valueByMetadata.put(ContainerRecord.LOCALIZATION, localisation);
			}
		}
	}
}
