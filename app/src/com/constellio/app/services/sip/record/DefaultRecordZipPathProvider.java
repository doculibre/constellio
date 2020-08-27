package com.constellio.app.services.sip.record;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;

public class DefaultRecordZipPathProvider implements RecordPathProvider {

	public static final String UKNOWN_USER = "user-unknown";

	private TaxonomiesManager taxonomiesManager;
	private MetadataSchemasManager metadataSchemasManager;
	private RecordServices recordServices;
	private ModelLayerFactory modelLayerFactory;

	public DefaultRecordZipPathProvider(ModelLayerFactory modelLayerFactory) {
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.modelLayerFactory = modelLayerFactory;
	}

	@Override
	public String getPath(Record record) {

		String parent = null;
		String pathIdentifier = record.getId();
		boolean classifyInUnknownUserIfNullParent = false;

		Taxonomy taxonomy = taxonomiesManager.getTaxonomyFor(record.getCollection(), record.getTypeCode());

		if (taxonomy != null) {
			MetadataSchema schema = modelLayerFactory.getMetadataSchemasManager().getSchemaOf(record);
			parent = record.getParentId(schema);

		} else if (Authorization.SCHEMA_TYPE.equals(record.getTypeCode())) {
			parent = schemasOf(record).wrapSolrAuthorizationDetails(record).getTarget();

		} else if (UserFolder.SCHEMA_TYPE.equals(record.getTypeCode())) {
			UserFolder userFolder = schemasOf(record).wrapUserFolder(record);
			parent = userFolder.getParent() == null ? userFolder.getUser() : userFolder.getParent();
			classifyInUnknownUserIfNullParent = true;

		} else if (UserDocument.SCHEMA_TYPE.equals(record.getTypeCode())) {
			UserDocument userDocument = schemasOf(record).wrapUserDocument(record);
			parent = userDocument.getUserFolder() == null ? userDocument.getUser() : userDocument.getUserFolder();
			classifyInUnknownUserIfNullParent = true;

		} else if (TemporaryRecord.SCHEMA_TYPE.equals(record.getTypeCode())) {
			parent = record.get(Schemas.CREATED_BY);
			classifyInUnknownUserIfNullParent = true;

		}

		StringBuilder path = new StringBuilder();
		if (parent == null) {
			path.append("/data/");

			if (classifyInUnknownUserIfNullParent) {
				path.append(UKNOWN_USER).append("/");

			} else if (taxonomy != null) {
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

		path.append(pathIdentifier);

		return path.toString();
	}

	private SchemasRecordsServices schemasOf(Record record) {
		return new SchemasRecordsServices(record.getCollection(), modelLayerFactory);
	}
}
