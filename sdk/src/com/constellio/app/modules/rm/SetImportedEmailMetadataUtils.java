package com.constellio.app.modules.rm;

import static com.constellio.app.utils.ScriptsUtils.startLayerFactoriesWithoutBackgroundThreads;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.ActionExecutorInBatch;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class SetImportedEmailMetadataUtils {
	
	private static final Logger LOGGER = Logger.getLogger(SetImportedEmailMetadataUtils.class);

	static int BATCH_SIZE = 5000;

	static String currentCollection;
	static AppLayerFactory appLayerFactory;
	static ModelLayerFactory modelLayerFactory;
	static SearchServices searchServices;
	static RecordServices recordServices;
	static MetadataSchemasManager schemasManager;
	static ContentManager contentManager;
	static RMSchemasRecordsServices rm;

	private static void startBackend() {
		//Only enable this line to run in production
		appLayerFactory = startLayerFactoriesWithoutBackgroundThreads();

		//Only enable this line to run on developer workstation
		//appLayerFactory = SDKScriptUtils.startApplicationWithoutBackgroundProcessesAndAuthentication();
	}

	private static LogicalSearchQuery getQuery() {
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(currentCollection);
		MetadataSchema emailSchema = types.getSchema(Email.SCHEMA);
		
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition condition = from(emailSchema).returnAll();
		query.setCondition(condition);
		return query;
	}

	private static void runScriptForCurrentCollection()
			throws Exception {

		new ActionExecutorInBatch(searchServices, "Set imported email metadata task", BATCH_SIZE) {

			@Override
			public void doActionOnBatch(List<Record> records) {
				Transaction transaction = new Transaction();
				transaction.setSkippingRequiredValuesValidation(true);
				transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
				
				for (Record record : records) {
					Email email = rm.wrapEmail(record);
					try {
						List<String> originalAttachmentsList = email.getEmailAttachmentsList();
						List<String> newAttachmentsList = new ArrayList<>();
						for (String attachmentFilename : originalAttachmentsList) {
							if (!attachmentFilename.equals("null")) {
								attachmentFilename = attachmentFilename.replace("null, ", "");
								String newAttachmentFilename = MimeUtility.decodeText(attachmentFilename);
								newAttachmentsList.add(newAttachmentFilename);
							}
						}

						if (!originalAttachmentsList.equals(newAttachmentsList)) {
							System.err.println("Conversion : " + originalAttachmentsList + " > " + newAttachmentsList);
							email.setEmailAttachmentsList(newAttachmentsList);
							transaction.update(record);
						}
					} catch (Throwable t) {
						LOGGER.error("Error while reading email content", t);
					}
				}

				try {
					recordServices.execute(transaction);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}

		}.execute(getQuery());

	}

	public static void main(String argv[])
			throws Exception {

		RecordPopulateServices.LOG_CONTENT_MISSING = false;

		startBackend();

		modelLayerFactory = appLayerFactory.getModelLayerFactory();
		searchServices = modelLayerFactory.newSearchServices();
		recordServices = modelLayerFactory.newRecordServices();
		schemasManager = modelLayerFactory.getMetadataSchemasManager();
		contentManager = modelLayerFactory.getContentManager();

		List<String> collections = modelLayerFactory.getCollectionsListManager().getCollectionsExcludingSystem();
		for (String collection : collections) {
			currentCollection = collection;
			rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			runScriptForCurrentCollection();
		}

	}

}
