package com.constellio.app.services.schemas.bulkImport.data;

import java.util.List;

import com.constellio.app.services.schemas.bulkImport.BulkImportParams;
import com.constellio.app.services.schemas.bulkImport.BulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.BulkImportResults;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServicesRuntimeException;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;

public interface ImportServices {
	BulkImportResults bulkImport(ImportDataProvider importDataProvider, BulkImportProgressionListener progressionListener,
			User user, List<String> collections)
			throws ValidationException;

	BulkImportResults bulkImport(ImportDataProvider importDataProvider,
			final BulkImportProgressionListener bulkImportProgressionListener,
			final User user, List<String> collections, BulkImportParams params)
			throws ValidationException;
}
