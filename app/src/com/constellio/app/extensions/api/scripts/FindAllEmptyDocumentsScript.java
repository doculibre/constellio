package com.constellio.app.extensions.api.scripts;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class FindAllEmptyDocumentsScript extends ScriptWithLogOutput {

	public FindAllEmptyDocumentsScript(AppLayerFactory appLayerFactory) {
		super(appLayerFactory, "Content", "List all empty documents");
	}

	@Override
	protected void execute() throws Exception {
		try {
			File file = new File(new FoldersLocator().getWorkFolder(), "emptyDocuments.csv");
			int count = 0;
			for (String collection : appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem()) {
				outputLogger.info("Collection " + collection);
				outputLogger.info("--------------------------------");
				final RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);

				LogicalSearchQuery query = new LogicalSearchQuery(from(rm.documentSchemaType()).returnAll())
						.sortDesc(Schemas.MODIFIED_ON);
				Iterator<Record> recordIterator = searchServices.recordsIterator(query, 1000);
				while (recordIterator.hasNext()) {
					Document document = rm.wrapDocument(recordIterator.next());
					if (!document.hasContent()) {
						continue;
					}

					if (document.getContent().getCurrentVersion().getLength() == 0) {
						outputLogger.info("Found an empty document : " + document.getId() + " | " + document.getTitle() +
										  " | " + document.getContent().getCurrentVersion().getFilename() + " | " +
										  document.getModifiedOn());
						String createdBy = null;
						try {
							createdBy = rm.getUser(document.getCreatedBy()).getUsername();
						} catch (Exception ignored) {
						}
						String modifiedBy = null;
						try {
							modifiedBy = rm.getUser(document.getModifiedBy()).getUsername();
						} catch (Exception ignored) {
						}
						String line = document.getId() + ";" + document.getTitle() + ";" + collection + ";" +
									  document.getCreatedOn() + ";" + createdBy + ";" +
									  document.getModifiedOn() + ";" + modifiedBy + System.lineSeparator();
						Files.write(file.toPath(), line.getBytes(StandardCharsets.UTF_8), CREATE, APPEND);
						count++;
					}
				}
			}
			outputLogger.info("--------------------------------");
			outputLogger.info("Found " + count + " empty documents");
		} catch (Exception e) {
			outputLogger.warn("Error " + ExceptionUtils.getStackTrace(e));
		}
	}
}
