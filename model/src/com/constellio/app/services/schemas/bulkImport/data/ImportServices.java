package com.constellio.app.services.schemas.bulkImport.data;

import java.util.List;

import com.constellio.app.services.schemas.bulkImport.BulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.BulkImportResults;
import com.constellio.model.entities.records.wrappers.User;

public interface ImportServices {
	BulkImportResults bulkImport(ImportDataProvider importDataProvider, BulkImportProgressionListener progressionListener,
			User user, List<String> collections);
}
