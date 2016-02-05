package com.constellio.app.services.schemas.bulkImport.data;

import com.constellio.app.services.schemas.bulkImport.BulkImportProgressionListener;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.app.services.schemas.bulkImport.BulkImportResults;

import java.util.List;

public interface ImportServices {
    BulkImportResults bulkImport(ImportDataProvider importDataProvider, BulkImportProgressionListener progressionListener, User user, List<String> collections);
}
