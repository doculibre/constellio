package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.constellio.model.services.migrations.ConstellioEIMConfigs.PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS;

public class ResetSmbDocumentParsedContentScript extends ScriptWithLogOutput {
	public ResetSmbDocumentParsedContentScript(AppLayerFactory appLayerFactory) {
		super(appLayerFactory, "Content", "ResetSmbDocumentParsedContentScript");
	}

	@Override
	protected void execute() throws Exception {
		final int maxParsedContentSize = getMaxParsedContentSizeInBytes();

		for (String collection : appLayerFactory
				.getCollectionsManager().getCollectionCodesExcludingSystem()) {
			final ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);

			onCondition(LogicalSearchQueryOperators.from(es.connectorSmbDocument.schemaType()).returnAll())
					.modifyingRecordsWithImpactHandling(new ConditionnedActionExecutorInBatchBuilder.RecordScript() {
															@Override
															public void modifyRecord(Record record) {
																ConnectorSmbDocument document = es.wrapConnectorSmbDocument(record);
																String content = document.getParsedContent();
																if (content != null) {
																	try {
																		int numberOfBytes = content.getBytes("UTF-8").length;
																		if (numberOfBytes > maxParsedContentSize) {																			;
																			document.setParsedContent(new String(Arrays.copyOfRange(content.getBytes("UTF-8"), 0, maxParsedContentSize)));
																			outputLogger.appendToFile("Reduced size of : " + document.getURL() + "\n");
																			outputLogger.appendToFile("from " + numberOfBytes/1024 + " KB to " + maxParsedContentSize/1024 + " KB\n");
																		}
																	} catch (UnsupportedEncodingException e) {
																		e.printStackTrace();
																	}
																}
															}
														}
					);
		}
	}

	protected int getMaxParsedContentSizeInBytes() {
		return ((int)modelLayerFactory.getSystemConfigurationsManager().getValue(PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS)) * 1024;
	}
}
