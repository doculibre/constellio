package com.constellio.app.modules.rm.migrations.records;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class RMEmailMigrationTo7_7_1 extends RecordMigrationScript {

	private ContentDao contentDao;
	private RMSchemasRecordsServices rm;
	private static final String EMAIL_CONTENT_FOLDER = "emailContentBackup";
	private MetadataSchemasManager metadataSchemasManager;
	private String collection;

	public RMEmailMigrationTo7_7_1(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.contentDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
	}

	@Override
	public String getSchemaType() {
		return Document.SCHEMA_TYPE;
	}

	@Override
	public void migrate(Record record) {
		InputStream stream = null;

		try {
			if (record.getSchemaCode().equals(Email.SCHEMA)) {
				Email email = rm.wrapEmail(record);
				String emailContent = email.getEmailContent();
				if (emailContent != null) {
					stream = new ByteArrayInputStream(emailContent.getBytes());
					contentDao.add(EMAIL_CONTENT_FOLDER + "/" + record.getId(), stream);
				}

				email.setEmailContent(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void afterLastMigratedRecord() {
		metadataSchemasManager.modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				if (types.hasSchemaType(Document.SCHEMA_TYPE) && types.getSchemaType(Document.SCHEMA_TYPE).hasSchema(Email.SCHEMA)) {
					MetadataSchemaBuilder emailSchema = types.getSchema(Email.SCHEMA);
					if (emailSchema.hasMetadata(Email.EMAIL_CONTENT)) {
						emailSchema.deleteMetadataWithoutValidation(Email.EMAIL_CONTENT);
					}
				}
			}
		});
	}
}
