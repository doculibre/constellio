package com.constellio.app.modules.rm.migrations.records;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.schemas.Schemas;
import org.apache.commons.io.FilenameUtils;

public class RMDocumentMigrationTo8_1_0_42 extends RecordMigrationScript {

	private ContentDao contentDao;
	private RMSchemasRecordsServices rm;

	public RMDocumentMigrationTo8_1_0_42(String collection, AppLayerFactory appLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		this.contentDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();
	}

	@Override
	public String getSchemaType() {
		return Document.SCHEMA_TYPE;
	}

	@Override
	public void migrate(Record record) {
		Document document = rm.wrapDocument(record);
		Content content = document.getContent();
		if (content != null && isRequiringJpegConversion(content)) {
			document.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, true);
		}

	}

	private boolean isRequiringJpegConversion(Content content) {
		String filename = content.getCurrentVersion().getFilename();
		String ext = FilenameUtils.getExtension(filename).toLowerCase();
		boolean isTiffImage = ext.equals("tif") || ext.equals("tiff");

		String hash = content.getCurrentVersion().getHash();
		return isTiffImage && !contentDao.isDocumentExisting(hash + ".jpegConversion");
	}

	@Override
	public void afterLastMigratedRecord() {
	}
}
