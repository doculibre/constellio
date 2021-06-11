package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.utils.MimeTypes;

public class MarkTiffWithoutPreviewForPreviewConversionScript extends ScriptWithLogOutput {
	
	public MarkTiffWithoutPreviewForPreviewConversionScript(AppLayerFactory appLayerFactory) {
		super(appLayerFactory, "Content", "Generate missing previews for TIFF files");
	}

	@Override
	protected void execute() throws Exception {
		for (String collection : appLayerFactory
				.getCollectionsManager().getCollectionCodesExcludingSystem()) {
			final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

			onCondition(LogicalSearchQueryOperators.from(rm.documentSchemaType()).returnAll())
					.modifyingRecordsWithImpactHandling(new ConditionnedActionExecutorInBatchBuilder.RecordScript() {
					@Override
					public void modifyRecord(Record record) {
						Document document = rm.wrapDocument(record);
						Content content = document.getContent();
						if (content != null) {
							String hash = content.getCurrentVersion().getHash();
							String filename = content.getCurrentVersion().getFilename();
							String mimeType = content.getCurrentVersion().getMimetype();
							ContentDao contentDao = getContentDao();

							if (mimeType.equals(MimeTypes.MIME_IMAGE_TIFF) && !contentDao.isDocumentExisting(hash + ".jpegConversion")) {
								document.set(Schemas.MARKED_FOR_PREVIEW_CONVERSION, true);
								outputLogger.appendToFile("Marked for preview conversion  : " + filename + "\n");
							}	
						}
					}
				}
			);
		}
	}

	public ContentDao getContentDao() {
		return modelLayerFactory.getDataLayerFactory().getContentsDao();
	}
}
