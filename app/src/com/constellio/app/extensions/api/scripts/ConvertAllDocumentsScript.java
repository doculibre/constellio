package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

import static java.util.Arrays.asList;

public class ConvertAllDocumentsScript extends ScriptWithLogOutput {
	public ConvertAllDocumentsScript(AppLayerFactory appLayerFactory) {
		super(appLayerFactory, "Content", "ConvertAllDocuments");
	}

	@Override
	protected void execute() throws Exception {
		final ContentManager contentManager = modelLayerFactory.getContentManager();
		final ContentDao contentDao = modelLayerFactory.getDataLayerFactory().getContentsDao();

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

																	if (contentDao.isDocumentExisting(hash + ".preview")) {
																		contentDao.delete(asList(hash + ".preview"));
																	}

																	document.setMarkedForPreviewConversion(true);
																}
															}
														}
					);
		}

		contentManager.convertPendingContentForPreview();
	}
}
