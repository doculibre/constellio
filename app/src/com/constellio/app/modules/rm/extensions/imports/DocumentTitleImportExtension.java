package com.constellio.app.modules.rm.extensions.imports;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.services.schemas.validators.metadatas.IllegalCharactersValidator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class DocumentTitleImportExtension extends RecordImportExtension {
	private RMSchemasRecordsServices rm;
	AppLayerFactory appLayerFactory;
	String collection;

	public DocumentTitleImportExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.rm = new RMSchemasRecordsServices(collection, appLayerFactory);

	}


	@Override
	public String getDecoratedSchemaType() {
		return Document.SCHEMA_TYPE;
	}

	@Override
	public void build(BuildParams event) {
		Document document = rm.wrapDocument(event.getRecord());
		if (shouldSynchronizeFilename(event)) {
			synchronizeContentFilenameToDocumentTitle(document);
		}
		super.build(event);
	}

	private boolean shouldSynchronizeFilename(BuildParams event) {
		return event.getImportDataOptions().isSynchronizeFilename() && isUpdatingExistingDocument(event);
	}

	private boolean isUpdatingExistingDocument(BuildParams event) {
		return event.getRecord().getRecordDTO() != null;
	}

	private void synchronizeContentFilenameToDocumentTitle(Document document) {
		if (document.getContent() == null) {
			return;
		}
		String currentTitle = document.getTitle();
		String currentContentFilename = document.getContent().getCurrentVersion().getFilename();
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(currentContentFilename));
		String newContentFilename;
		if (currentTitle.endsWith("." + extension) || StringUtils.isBlank(extension)) {
			newContentFilename = currentTitle;
		} else {
			newContentFilename = currentTitle + "." + extension;
		}

		ConfigProvider configProvider = appLayerFactory.getModelLayerFactory().newConfigProvider();
		if (IllegalCharactersValidator.isValid(newContentFilename, configProvider)) {
			if (!document.getSchema().getMetadata(Schemas.TITLE_CODE).getPopulateConfigs().isAddOnly()) {
				document.getContent().renameCurrentVersion(newContentFilename);
			}
		}

	}
}
