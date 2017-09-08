package com.constellio.app.services.importExport.records;

import com.constellio.model.extensions.behaviors.RecordExtension;

import java.util.ArrayList;
import java.util.List;

public class RecordExportOptions {

	boolean exportValueLists;
	boolean isForSameSystem = false;

	List<String> exportedSchemaTypes = new ArrayList<>();

	public List<String> getExportedSchemaTypes() {
		return exportedSchemaTypes;
	}

	public boolean isForSameSystem() {
		return isForSameSystem;
	}

	public RecordExportOptions setForSameSystem(boolean forSameSystem) {
		isForSameSystem = forSameSystem;
		return this;
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

}
