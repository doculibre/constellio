package com.constellio.app.services.actionDisplayManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MenuDisplayListBySchemaType implements Serializable {
	private Map<String, MenuDisplayList> actionDisplayListByCodeMap;

	public MenuDisplayListBySchemaType() {
		actionDisplayListByCodeMap = new HashMap<>();
	}

	protected void setListForSchemaType(String schemaType, MenuDisplayList actionDisplayList) {
		this.actionDisplayListByCodeMap.put(schemaType, actionDisplayList);
	}

	public MenuDisplayList getActionDisplayList(String schemaType) {
		return this.actionDisplayListByCodeMap.get(schemaType);
	}

	public int getNumberOfSchemaTypeRegistered() {
		return this.actionDisplayListByCodeMap.size();
	}
}
