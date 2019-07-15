package com.constellio.app.modules.restapi.core.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum SchemaTypes {
	DOCUMENT("documents"),
	FOLDER("folders");

	private static final Set<SchemaTypes> ALLOWED_SCHEMA_TYPES = Sets.newHashSet(DOCUMENT, FOLDER);

	@Getter
	private final String resource;

	public static boolean contains(SchemaTypes schemaType) {
		return ALLOWED_SCHEMA_TYPES.contains(schemaType);
	}
}
