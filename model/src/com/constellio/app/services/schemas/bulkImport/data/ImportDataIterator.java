package com.constellio.app.services.schemas.bulkImport.data;

import java.util.Iterator;

public interface ImportDataIterator extends Iterator<ImportData> {

	ImportDataOptions getOptions();

	void close();
}

