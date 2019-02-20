package com.constellio.app.services.sip.record;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class DefaultRecordZipPathProvider implements RecordPathProvider {
	private TaxonomiesManager taxonomiesManager;
	private MetadataSchemasManager metadataSchemasManager;
	private RecordServices recordServices;
	private SchemasRecordsServices schemasRecordsServices;

	public DefaultRecordZipPathProvider(String collection, ModelLayerFactory modelLayerFactory) {
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public String getPath(Record record) {

		String parent = null;
		String pathIdentifier = record.getId();

		Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(record.getCollection(), record.getTypeCode());

		if (taxonomy != null) {
			parent = record.getParentId();
		} else if (Authorization.SCHEMA_TYPE.equals(record.getTypeCode())) {
			parent = schemasRecordsServices.wrapSolrAuthorizationDetails(record).getTarget();
		}

		StringBuilder path = new StringBuilder();
		if (parent == null) {
			path.append("/data/");

			if (taxonomy != null) {
				path.append(taxonomy.getCode()).append("/");

			} else if (record.getTypeCode().startsWith("ddv")) {
				path.append("valueLists/").append(record.getTypeCode()).append("/");

			} else {
				path.append(record.getTypeCode()).append("/");
			}

		} else {
			path.append(getPath(recordServices.getDocumentById(parent))).append("/");
		}

		path.append(record.getTypeCode()).append("-");

		MetadataSchema schema = metadataSchemasManager.getSchemaTypeOf(record).getDefaultSchema();

		//		if (schema.hasMetadataWithCode("code") && schema.getMetadata("code").isDefaultRequirement()) {
		//			path.append(record.get(schema.getMetadata("code")));
		//
		//		} else if (User.SCHEMA_TYPE.equals(record.getTypeCode())) {
		//			path.append(record.<String>get(schema.get(User.USERNAME)));
		//
		//
		//		} else {
		path.append(pathIdentifier);
		//		}

		return path.toString();
	}
}
