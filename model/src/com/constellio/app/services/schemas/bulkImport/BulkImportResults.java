package com.constellio.app.services.schemas.bulkImport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BulkImportResults {

	private List<ImportError> importErrors = new ArrayList<>();
	private long correctlyImportedCount;

	public BulkImportResults() {

	}

	public List<String> getInvalidIds() {
		List<String> invalidIds = new ArrayList<>();
		for (ImportError importError : importErrors) {
			invalidIds.add(importError.getInvalidElementId());
		}
		return invalidIds;
	}

	public void add(ImportError importError) {
		this.importErrors.add(importError);
	}

	public List<ImportError> getImportErrors() {
		return Collections.unmodifiableList(importErrors);
	}

	public long getCorrectlyImportedCount() {
		return correctlyImportedCount;
	}

	public void inc() {
		correctlyImportedCount++;
	}

	public void inc(int size) {
		this.correctlyImportedCount +=
				size;
	}
}
