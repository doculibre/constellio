package com.constellio.model.extensions.events.schemas;

import com.constellio.model.entities.records.wrappers.User;

public interface SchemaEvent {
	String getSchemaCode();

	User getUser();
}
