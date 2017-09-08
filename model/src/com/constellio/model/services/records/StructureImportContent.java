package com.constellio.model.services.records;

public class StructureImportContent implements ImportContent {

	String serializedStructure;

	public StructureImportContent(String serializedStructure) {
		this.serializedStructure = serializedStructure;
	}

	public String getSerializedStructure() {
		return serializedStructure;
	}
}
