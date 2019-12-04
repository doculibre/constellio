package com.constellio.app.modules.rm.migrations.records;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.schemas.Schemas;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RMDocumentMigrationTo9_0 extends RecordMigrationScript {

	private RMSchemasRecordsServices rm;

	public RMDocumentMigrationTo9_0(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public String getSchemaType() {
		return Document.SCHEMA_TYPE;
	}

	@Override
	public void migrate(Record record) {
		Document document = rm.wrapDocument(record);
		Content content = document.getContent();
		if (content != null) {
			document.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, true);
		}
	}

	@Override
	public void afterLastMigratedRecord() {
	}
}
