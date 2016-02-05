package com.constellio.app.services.schemas.bulkImport;

import com.constellio.model.entities.schemas.Schemas;

public class Resolver {

	String metadata;

	String value;

	public String getMetadata() {
		return metadata;
	}

	public String getValue() {
		return value;
	}

	public static Resolver toResolver(String strValue) {
		Resolver resolver = new Resolver();
		int indexOfFirstColon = strValue.indexOf(":");
		if (indexOfFirstColon != -1) {
			resolver.metadata = strValue.substring(0, indexOfFirstColon);
			resolver.value = strValue.substring(indexOfFirstColon + 1);
		} else {
			resolver.metadata = Schemas.LEGACY_ID.getLocalCode();
			resolver.value = strValue;
		}
		return resolver;
	}

}
