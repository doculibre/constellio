package com.constellio.app.services.importExport.records;

import java.util.ArrayList;
import java.util.List;

public class RecordExportOptions {

	boolean exportValueLists;
	boolean exportTaxonomies;

	List<String> exportedSchemaTypes = new ArrayList<>();

	public List<String> getExportedSchemaTypes() {
		return exportedSchemaTypes;
	}

	public RecordExportOptions setExportedSchemaTypes(List<String> exportedSchemaTypes) {
		this.exportedSchemaTypes = exportedSchemaTypes;
		return this;
	}

	public boolean isExportValueLists() {
		return exportValueLists;
	}

	public RecordExportOptions setExportValueLists(boolean exportValueLists) {
		this.exportValueLists = exportValueLists;
		return this;
	}

	public boolean isExportTaxonomies() {
		return exportTaxonomies;
	}

	public RecordExportOptions setExportTaxonomies(boolean exportTaxonomies) {
		this.exportTaxonomies = exportTaxonomies;
		return this;
	}
}
