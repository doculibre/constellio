package com.constellio.app.services.schemas.bulkImport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;
import com.constellio.data.utils.BatchBuilderIterator;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.XmlUserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.users.UserServices;

public class UserImportServices implements ImportServices {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserImportServices.class);
	private static final int DEFAULT_BATCH_SIZE = 100;
	private int batchSize;
	private UserServices userServices;
	private int currentElement;

	public UserImportServices(ModelLayerFactory modelLayerFactory) {
		this(modelLayerFactory, DEFAULT_BATCH_SIZE);
	}

	public UserImportServices(ModelLayerFactory modelLayerFactory, int batchSize) {
		this.batchSize = batchSize;
		userServices = modelLayerFactory.newUserServices();
	}

	void importUser(List<String> collections, UserCredential userCredential) {
		userCredential = userCredential.withCollections(collections);
		userServices.addUpdateUserCredential(userCredential);
	}

	@Override
	public BulkImportResults bulkImport(ImportDataProvider importDataProvider, BulkImportProgressionListener progressionListener,
			User user, List<String> collections)
			throws RecordsImportServicesRuntimeException {
		currentElement = 0;
		importDataProvider.initialize();
		BulkImportResults importResults = new BulkImportResults();
		try {
			bulkImport(importResults, importDataProvider, collections);
			return importResults;
		} catch (Exception e) {
			LOGGER.warn(e.toString(), e);
			importResults.add(new ImportError("element" + currentElement, e.getMessage()));
			return importResults;
		} finally {
			importDataProvider.close();
		}
	}

	int bulkImport(BulkImportResults importResults, ImportDataProvider importDataProvider, List<String> collections) {
		int skipped = 0;
		Iterator<ImportData> importDataIterator = importDataProvider.newDataIterator("user");
		Iterator<List<ImportData>> importDataBatches = new BatchBuilderIterator<>(importDataIterator, batchSize);

		while (importDataBatches.hasNext()) {
			try {
				List<ImportData> batch = importDataBatches.next();
				skipped += importBatch(importResults, batch, collections);
			} catch (Exception e) {
				skipped++;
				LOGGER.warn(e.toString(), e);
				importResults.add(new ImportError("element" + currentElement, e.getMessage()));
			}
		}
		return skipped;
	}

	private int importBatch(BulkImportResults importResults, List<ImportData> batch, List<String> collections) {
		int skipped = 0;
		for (ImportData toImport : batch) {
			buildUser(importResults, toImport, collections);
		}
		return skipped;
	}

	void buildUser(BulkImportResults importResults, ImportData toImport, List<String> collections) {
		currentElement++;
		String username = (String) toImport.getFields().get("username");
		String firstName = (String) toImport.getFields().get("firstName");
		String lastName = (String) toImport.getFields().get("lastName");
		String email = (String) toImport.getFields().get("email");
		List<String> globalGroups = (List<String>) toImport.getFields().get("globalGroups");
		UserCredentialStatus userCredentialStatus;
		String status = (String) toImport.getFields().get("status");
		if (status.equals("p")) {
			userCredentialStatus = UserCredentialStatus.PENDING;
		} else if (status.equals("s")) {
			userCredentialStatus = UserCredentialStatus.SUSPENDED;
		} else if (status.equals("d")) {
			userCredentialStatus = UserCredentialStatus.DELETED;
		} else {
			userCredentialStatus = UserCredentialStatus.ACTIVE;
		}
		UserCredential userCredential;
		Object systemAdmin = toImport.getFields().get("systemAdmin");
		if (systemAdmin != null) {
			boolean systemAdminBoolean = Boolean.valueOf((String) systemAdmin);
			Map<String, LocalDateTime> tokens = new HashMap<>();
			userCredential = new XmlUserCredential(username, firstName, lastName, email, null, systemAdminBoolean, globalGroups,
					collections, tokens, userCredentialStatus);
		} else {
			userCredential = new XmlUserCredential(username, firstName, lastName, email, globalGroups, collections,
					userCredentialStatus);
		}
		try {
			userServices.addUpdateUserCredential(userCredential);
		} catch (Exception e) {
			LOGGER.warn(e.toString(), e);
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof RecordServicesException.ValidationException) {
				String message = ((RecordServicesException.ValidationException) cause).getErrors().toErrorsSummaryString();
				importResults.add(new ImportError(username, message));
			} else {
				importResults.add(new ImportError(username, e.getMessage()));
			}
		}

	}

}
