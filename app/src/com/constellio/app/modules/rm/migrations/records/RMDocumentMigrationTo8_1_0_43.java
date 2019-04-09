package com.constellio.app.modules.rm.migrations.records;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.utils.ImageUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordMigrationScript;
import com.constellio.model.entities.schemas.Schemas;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.util.Collections;

@Slf4j
public class RMDocumentMigrationTo8_1_0_43 extends RecordMigrationScript {

	private ContentDao contentDao;
	private RMSchemasRecordsServices rm;

	public RMDocumentMigrationTo8_1_0_43(String collection, AppLayerFactory appLayerFactory) {
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
		if (content != null && isRequiringResizing(content)) {
			document.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, true);

			String hash = content.getCurrentVersion().getHash();
			if (contentDao.isDocumentExisting(hash + ".jpegConversion")) {
				contentDao.delete(Collections.singletonList(hash + ".jpegConversion"));
			}
		}
	}

	private boolean isRequiringResizing(Content content) {
		try {
			File file = contentDao.getFileOf(content.getCurrentVersion().getHash());
			if (!content.getCurrentVersion().getMimetype().startsWith("image/")) {
				return false;
			}
			Dimension dimension = ImageUtils.getImageDimension(file);
			return ImageUtils.isImageOversized(dimension.getHeight());
		} catch (Exception e) {
			log.error("Failed to get dimension of file : " + content.getCurrentVersion().getFilename(), e);
			return false;
		}
	}


	@Override
	public void afterLastMigratedRecord() {
	}
}
